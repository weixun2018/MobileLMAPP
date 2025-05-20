"""记忆管理模块，负责对话历史的语义检索"""

import os
import json
import uuid
import time
import chromadb
from datetime import datetime
from chromadb.errors import NotFoundError
from config.config import Config


class MemoryManager:
    """记忆管理类，统一管理对话历史的语义检索"""

    def __init__(self, model_interface):
        self.model_interface = model_interface

        # 确保ChromaDB目录存在
        os.makedirs(Config.MEMORY_DB_DIR, exist_ok=True)

        # 初始化ChromaDB客户端
        self.client = chromadb.PersistentClient(path=Config.MEMORY_DB_DIR)

        # 获取嵌入向量的维度
        self.embedding_dimension = self.model_interface.get_embedding_dimension()
        print(f"使用嵌入维度: {self.embedding_dimension}")

        # 获取或创建统一记忆集合
        self.memory_collection = self._get_or_create_collection("memory")

        # 记忆查询缓存
        self.memory_cache = {}

    def _get_or_create_collection(self, name):
        """获取或创建ChromaDB集合"""
        try:
            collection = self.client.get_collection(name=name)
            print(f"获取已有{name}集合")
            return collection
        except NotFoundError:
            print(f"创建新的{name}集合")
            return self.client.create_collection(
                name=name, metadata={"hnsw:space": Config.COLLECTION_DISTANCE_TYPE}
            )

    def _recreate_collections(self):
        """重新创建记忆集合（仅在必要时使用）"""
        # 删除现有集合
        try:
            self.client.delete_collection("memory")
            print("已删除原有记忆集合")
        except NotFoundError:
            pass

        # 创建新集合
        self.memory_collection = self.client.create_collection(
            name="memory", metadata={"hnsw:space": Config.COLLECTION_DISTANCE_TYPE}
        )
        print(f"已创建新的记忆集合，向量维度: {self.embedding_dimension}")

    def add_memory(self, user_input):
        """添加新记忆"""
        # 生成唯一ID
        memory_id = str(uuid.uuid4())

        # 构建记忆内容
        memory_content = {"user_input": user_input, "timestamp": datetime.now().isoformat()}

        # 序列化记忆为文本
        memory_text = json.dumps(memory_content, ensure_ascii=False)

        # 获取记忆的嵌入向量
        embedding = self.model_interface.get_embedding(memory_text)

        # 添加到统一记忆集合
        self.memory_collection.add(
            ids=[memory_id],
            embeddings=[embedding],
            metadatas=[{"timestamp": memory_content["timestamp"]}],
            documents=[memory_text],
        )
        print(f"添加记忆到集合: {memory_id}")

    def retrieve_relevant_memories_by_clues(self, clues, top_k=None):
        """使用线索检索相关记忆，每条线索单独检索，但限制处理的线索数量"""
        start_time = time.time()
        if top_k is None:
            top_k = Config.MEMORY_RETRIEVAL_TOP_K

        # 将线索拆分为单独的条目
        clue_list = [clue.strip() for clue in clues.strip().split("\n") if clue.strip()]

        if not clue_list:
            print("没有有效的线索条目")
            return []

        # 限制处理的线索数量，防止过多查询导致性能问题
        if len(clue_list) > Config.MAX_CLUES_TO_PROCESS:
            print(f"线索过多，限制处理前 {Config.MAX_CLUES_TO_PROCESS} 条")
            clue_list = clue_list[: Config.MAX_CLUES_TO_PROCESS]

        print(f"处理 {len(clue_list)} 条线索进行检索")
        for i, clue in enumerate(clue_list):
            print(f"线索 {i+1}: {clue}")

        # 存储所有检索结果
        all_memories = {}  # 使用字典去重

        # 批量获取所有线索的嵌入向量
        clue_embeddings = self.model_interface.get_embeddings_batch(clue_list)

        # 对每条线索进行检索
        for i, clue_embedding in enumerate(clue_embeddings):
            try:
                clue = clue_list[i]

                # 从缓存中查找结果
                cache_key = clue[:50]  # 使用线索的前50个字符作为缓存键
                if cache_key in self.memory_cache:
                    print(f"使用缓存结果: {cache_key}")
                    for memory_obj in self.memory_cache[cache_key]:
                        # 只添加符合相似度阈值的记忆
                        memory_similarity = 1 - memory_obj.get("distance", 1.0)
                        if memory_similarity >= Config.SIMILARITY_THRESHOLD:
                            memory_id = f"{memory_obj.get('timestamp', '')}_{memory_obj.get('user_input', '')[:20]}"
                            all_memories[memory_id] = memory_obj
                        else:
                            print(
                                f"缓存记忆相似度 ({memory_similarity:.4f}) 低于阈值 ({Config.SIMILARITY_THRESHOLD})，已过滤"
                            )
                    continue

                # 从记忆集合中检索
                # 检索比需要的多一些结果，以便在相似度过滤后仍有足够的结果
                larger_top_k = min(top_k * 3, 20)  # 检索更多结果以便过滤
                print(f"检索 {larger_top_k} 条记忆，以便在应用相似度过滤后有足够的结果...")

                # 从记忆集合中检索
                results = self.memory_collection.query(
                    query_embeddings=[clue_embedding], n_results=larger_top_k
                )

                # 处理检索结果
                memory_results = []
                filtered_count = 0

                if results["documents"] and len(results["documents"][0]) > 0:
                    for j in range(len(results["documents"][0])):
                        try:
                            doc = results["documents"][0][j]
                            distance = results["distances"][0][j] if "distances" in results else 1.0

                            # 由于ChromaDB使用的是余弦距离，需要将其转换为余弦相似度
                            # 在余弦距离中，0表示完全相似，2表示完全不相似
                            # 我们将其转换为余弦相似度，1表示完全相似，-1表示完全不相似
                            similarity = 1 - distance

                            # 只有相似度高于阈值的记忆才会被保留
                            if similarity < Config.SIMILARITY_THRESHOLD:
                                print(
                                    f"记忆 {j+1} 相似度 ({similarity:.4f}) 低于阈值 ({Config.SIMILARITY_THRESHOLD})，已过滤"
                                )
                                filtered_count += 1
                                continue

                            memory_obj = json.loads(doc)
                            memory_id = f"{memory_obj.get('timestamp', '')}_{memory_obj.get('user_input', '')[:20]}"

                            # 添加距离和相似度信息
                            memory_obj["distance"] = distance
                            memory_obj["similarity"] = similarity
                            memory_results.append(memory_obj)

                            # 如果这个记忆已经存在，保留距离更小的版本
                            if (
                                memory_id not in all_memories
                                or all_memories[memory_id]["distance"] > distance
                            ):
                                all_memories[memory_id] = memory_obj
                        except json.JSONDecodeError:
                            print(f"无法解析记忆内容: {results['documents'][0][j]}")

                    print(
                        f"根据相似度阈值 {Config.SIMILARITY_THRESHOLD} 过滤掉了 {filtered_count} 条记忆"
                    )

                # 将结果保存到缓存
                if memory_results:
                    self.memory_cache[cache_key] = memory_results
                    # 限制缓存大小
                    if len(self.memory_cache) > Config.EMBEDDING_CACHE_SIZE:
                        # 删除最早添加的缓存项
                        oldest_key = next(iter(self.memory_cache))
                        del self.memory_cache[oldest_key]

            except Exception as e:
                print(f"检索线索 '{clue_list[i]}' 时出错: {e}")
                continue

        # 将所有记忆转换为列表并按照相关性排序
        memories_list = list(all_memories.values())
        memories_list.sort(key=lambda x: x.get("distance", 1.0))

        end_time = time.time()
        print(
            f"记忆检索耗时: {end_time - start_time:.2f} 秒，找到 {len(memories_list)} 条相似度高于阈值的相关记忆"
        )

        # 限制返回数量
        return memories_list[:top_k]

    def format_memories_for_context(self, memories):
        """将记忆格式化为上下文字符串"""
        if not memories:
            return ""

        formatted_memories = []
        for memory in memories:
            try:
                # 尝试解析记忆内容
                if isinstance(memory, str):
                    memory_obj = json.loads(memory)
                else:
                    memory_obj = memory

                user_input = memory_obj.get("user_input", "")
                ai_response = memory_obj.get("ai_response", "")
                timestamp = memory_obj.get("timestamp", "")
                similarity = memory_obj.get("similarity", "未知")

                # 格式化为可读文本
                if timestamp:
                    try:
                        dt = datetime.fromisoformat(timestamp)
                        formatted_time = dt.strftime("%Y-%m-%d %H:%M")
                    except ValueError:
                        formatted_time = timestamp
                else:
                    formatted_time = "未知时间"

                memory_text = f"[{formatted_time}] 用户: {user_input}\n助手: {ai_response}"
                formatted_memories.append(memory_text)

                print(
                    f"添加记忆到上下文: 相似度 {similarity}, 时间 {formatted_time}, 用户输入: {user_input[:30]}..."
                )
            except (json.JSONDecodeError, KeyError) as e:
                print(f"格式化记忆时出错: {e}, 内容: {memory}")

        return "\n\n".join(formatted_memories)
