"""记忆管理模块，负责对话历史的语义检索"""

import os
import json
import time
from datetime import datetime
import chromadb
from chromadb.errors import NotFoundError
from config.config import Config


class MemoryManager:
    """记忆管理类，统一管理对话历史的语义检索"""

    def __init__(self, model_interface):
        self.model_interface = model_interface

        # 确保ChromaDB目录存在
        os.makedirs(Config.MEMORY_DB_DIR, exist_ok=True)

        # 初始化ChromaDB客户端
        try:
            self.client = chromadb.PersistentClient(path=Config.MEMORY_DB_DIR)
            # 测试写入操作
            self._test_db_write_permission()
        except Exception as e:
            print(f"数据库初始化错误: {e}")
            print("尝试修复数据库权限...")

            # 尝试修复数据库文件权限
            if self._fix_db_permissions():
                # 重新尝试初始化客户端
                try:
                    self.client = chromadb.PersistentClient(path=Config.MEMORY_DB_DIR)
                    self._test_db_write_permission()
                    print("数据库权限修复成功，可以正常使用")
                except Exception as retry_error:
                    print(f"修复权限后仍然无法使用数据库: {retry_error}")
                    self._recreate_db_directory()
            else:
                # 如果无法修复权限，则重新创建数据库目录
                self._recreate_db_directory()

        # 获取嵌入向量的维度
        self.embedding_dimension = self.model_interface.get_embedding_dimension()
        print(f"使用嵌入维度: {self.embedding_dimension}")

        # 获取或创建统一记忆集合
        self.memory_collection = self._get_or_create_collection("memory")

        # 记忆查询缓存
        self.memory_cache = {}

    def _test_db_write_permission(self):
        """测试数据库写入权限"""
        try:
            # 创建一个临时集合以测试写入权限
            test_collection_name = f"test_write_{int(time.time())}"
            test_collection = self.client.create_collection(name=test_collection_name)
            # 添加一条测试数据
            test_collection.add(
                ids=["test_id"], documents=["测试写入权限"], embeddings=[[0.0] * 10]  # 临时嵌入向量
            )
            # 删除测试集合
            self.client.delete_collection(test_collection_name)
            print("数据库写入权限测试通过")
        except Exception as e:
            print(f"数据库写入权限测试失败: {e}")
            raise

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

    def add_memory(self, user_input, ai_response):
        """添加新记忆"""
        # 使用时间戳作为唯一ID
        memory_id = datetime.now().isoformat()

        # 构建记忆内容
        memory_content = {"用户": user_input, "助手": ai_response}

        # 序列化记忆为文本
        memory_text = json.dumps(memory_content, ensure_ascii=False)

        # 获取记忆的嵌入向量
        embedding = self.model_interface.get_embedding(memory_text)

        # 添加到统一记忆集合
        self.memory_collection.add(
            ids=[memory_id],
            embeddings=[embedding],
            metadatas=[{"type": "memory"}],
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
                            memory_id = memory_obj.get("timestamp", datetime.now().isoformat())
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
                            memory_id = memory_obj.get('timestamp', datetime.now().isoformat())

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

                user_input = memory_obj.get("用户", "")
                ai_response = memory_obj.get("助手", "")
                similarity = memory_obj.get("similarity", "未知")

                memory_text = f"用户: {user_input}\n助手: {ai_response}"
                formatted_memories.append(memory_text)

                print(
                    f"添加记忆到上下文: 相似度 {similarity}, 用户输入: {user_input[:30]}..."
                )
            except (json.JSONDecodeError, KeyError) as e:
                print(f"格式化记忆时出错: {e}, 内容: {memory}")

        return "\n\n".join(formatted_memories)

    def _fix_db_permissions(self):
        """尝试修复数据库文件权限"""
        try:
            db_file = os.path.join(Config.MEMORY_DB_DIR, "chroma.sqlite3")
            if os.path.exists(db_file):
                # 修改数据库文件权限为可读写
                os.chmod(db_file, 0o666)
                print(f"已修改数据库文件权限: {db_file}")

                # 修改数据库目录权限
                os.chmod(Config.MEMORY_DB_DIR, 0o755)
                print(f"已修改数据库目录权限: {Config.MEMORY_DB_DIR}")

                return True
            else:
                print(f"数据库文件不存在: {db_file}")
                return False
        except Exception as e:
            print(f"修复数据库权限失败: {e}")
            return False

    def _recreate_db_directory(self):
        """重新创建数据库目录"""
        print("尝试重新创建数据库目录...")
        # 备份旧数据库
        backup_dir = f"{Config.MEMORY_DB_DIR}_backup_{int(time.time())}"
        if os.path.exists(Config.MEMORY_DB_DIR):
            try:
                import shutil

                shutil.move(Config.MEMORY_DB_DIR, backup_dir)
                print(f"已将旧数据库备份到: {backup_dir}")
            except Exception as move_error:
                print(f"备份数据库失败: {move_error}")

        # 创建新的数据库目录
        os.makedirs(Config.MEMORY_DB_DIR, exist_ok=True)
        # 修改权限确保可写
        os.chmod(Config.MEMORY_DB_DIR, 0o755)
        print(f"创建了新的数据库目录: {Config.MEMORY_DB_DIR}")

        # 重新初始化客户端
        self.client = chromadb.PersistentClient(path=Config.MEMORY_DB_DIR)
