"""模型接口模块，负责与语言模型的交互"""

import torch
import time
from transformers import AutoModelForCausalLM, AutoTokenizer, AutoModel
from config.config import Config

class ModelInterface:
    """模型接口类，封装与模型的交互"""

    def __init__(self):
        # 初始化模型和分词器
        self.tokenizer = AutoTokenizer.from_pretrained(Config.MODEL_NAME, trust_remote_code=True)
        self.model = AutoModelForCausalLM.from_pretrained(Config.MODEL_NAME, trust_remote_code=True)

        # 初始化embedding模型
        self.embedding_tokenizer = AutoTokenizer.from_pretrained(Config.EMBEDDING_MODEL_NAME)
        self.embedding_model = AutoModel.from_pretrained(Config.EMBEDDING_MODEL_NAME)

        self.device = "cuda" if torch.cuda.is_available() else "cpu"
            
        self.model = self.model.to(self.device)
        self.embedding_model = self.embedding_model.to(self.device)

        # 添加嵌入向量缓存
        self.embedding_cache = {}

        # 请求计数器
        self.request_counter = 0

    def generate_response(self, prompt):
        """使用模型生成回复"""
        # 增加请求计数器
        self.request_counter += 1

        # 编码输入
        model_inputs = self.tokenizer.apply_chat_template(
            prompt, return_tensors="pt", add_generation_prompt=True
        ).to(self.device)

        # 生成回复
        output = self.model.generate(
            model_inputs,
            max_new_tokens=Config.MAX_NEW_TOKENS,
            temperature=Config.TEMPERATURE,
            top_p=Config.TOP_P,
        )

        # 解码回复
        full_response = self.tokenizer.batch_decode(output, skip_special_tokens=True)[0]

        # 提取assistant响应
        response = self.extract_assistant_response(full_response)

        # 检查是否需要清理内存
        self._check_and_clear_memory()

        return response

    def get_embedding(self, text):
        """获取文本的嵌入向量"""
        # 增加请求计数器
        self.request_counter += 1

        # 先检查缓存
        if text in self.embedding_cache:
            return self.embedding_cache[text]

        # 编码文本
        inputs = self.embedding_tokenizer(
            text, return_tensors="pt", padding=True, truncation=True, max_length=512
        ).to(self.device)

        # 获取嵌入向量
        with torch.no_grad():
            outputs = self.embedding_model(**inputs)
            # 使用平均池化作为句子表示
            embeddings = self._mean_pooling(outputs, inputs["attention_mask"])
            # 规范化嵌入向量
            embeddings = torch.nn.functional.normalize(embeddings, p=2, dim=1)

        # 转换为numpy数组
        embedding = embeddings[0].cpu().numpy().tolist()

        # 添加到缓存
        self.embedding_cache[text] = embedding

        # 控制缓存大小
        if len(self.embedding_cache) > Config.EMBEDDING_CACHE_SIZE:
            # 删除最早添加的缓存项
            self.embedding_cache.pop(next(iter(self.embedding_cache)))

        # 检查是否需要清理内存
        self._check_and_clear_memory()

        return embedding

    def get_embeddings_batch(self, texts):
        """批量获取嵌入向量"""
        # 增加请求计数器
        self.request_counter += 1

        if not texts:
            return []

        # 先检查哪些文本已经缓存
        result_embeddings = []
        uncached_texts = []
        uncached_indices = []

        for i, text in enumerate(texts):
            if text in self.embedding_cache:
                # 如果已缓存，直接使用
                result_embeddings.append(self.embedding_cache[text])
            else:
                # 否则加入待处理列表
                uncached_texts.append(text)
                uncached_indices.append(i)

        # 如果所有文本都已缓存，直接返回
        if not uncached_texts:
            return result_embeddings

        # 分批处理未缓存的文本
        batch_embeddings = []
        for i in range(0, len(uncached_texts), Config.EMBEDDING_BATCH_SIZE):
            batch_texts = uncached_texts[i:i+Config.EMBEDDING_BATCH_SIZE]

            # 编码文本
            inputs = self.embedding_tokenizer(
                batch_texts,
                return_tensors="pt",
                padding=True,
                truncation=True,
                max_length=512
            ).to(self.device)

            # 获取嵌入向量
            with torch.no_grad():
                outputs = self.embedding_model(**inputs)
                # 使用平均池化
                embeddings = self._mean_pooling(outputs, inputs["attention_mask"])
                # 规范化
                embeddings = torch.nn.functional.normalize(embeddings, p=2, dim=1)

            # 将每个嵌入向量添加到结果列表
            for j, emb in enumerate(embeddings):
                embedding = emb.cpu().numpy().tolist()
                batch_embeddings.append(embedding)

                # 添加到缓存
                self.embedding_cache[uncached_texts[i+j]] = embedding

        # 控制缓存大小
        while len(self.embedding_cache) > Config.EMBEDDING_CACHE_SIZE:
            # 删除最早添加的缓存项
            self.embedding_cache.pop(next(iter(self.embedding_cache)))

        # 将未缓存的嵌入向量按原始顺序插入结果列表
        final_embeddings = [None] * len(texts)
        for i, emb in enumerate(result_embeddings):
            final_embeddings[i] = emb

        for i, idx in enumerate(uncached_indices):
            final_embeddings[idx] = batch_embeddings[i]

        # 检查是否需要清理内存
        self._check_and_clear_memory()

        return final_embeddings

    def _check_and_clear_memory(self):
        """检查是否达到清理内存的频率，并在需要时清理内存"""
        if not Config.MEMORY_CLEAR_ENABLED:
            return

        if self.request_counter >= Config.MEMORY_CLEAR_FREQUENCY:
            print("达到内存清理频率，开始清理内存...")

            # 清理PyTorch缓存
            if torch.cuda.is_available():
                # 移动模型到CPU
                self.model = self.model.cpu()
                self.embedding_model = self.embedding_model.cpu()

                # 清空CUDA缓存
                torch.cuda.empty_cache()
                time.sleep(3)
                # 再次移回GPU
                self.model = self.model.to(self.device)
                self.embedding_model = self.embedding_model.to(self.device)
                
                print("MPS内存清理完成")

            # 重置计数器
            self.request_counter = 0

    def get_embedding_dimension(self):
        """获取嵌入向量的维度"""
        # 使用一个简单的文本获取嵌入向量，然后返回其维度
        sample_text = "维度测试"
        embedding = self.get_embedding(sample_text)
        return len(embedding)

    def _mean_pooling(self, model_output, attention_mask):
        """平均池化操作，用于获取句子嵌入"""
        token_embeddings = model_output.last_hidden_state
        input_mask_expanded = attention_mask.unsqueeze(-1).expand(token_embeddings.size()).float()
        return torch.sum(token_embeddings * input_mask_expanded, 1) / torch.clamp(
            input_mask_expanded.sum(1), min=1e-9
        )

    @staticmethod
    def extract_assistant_response(full_response):
        """从完整回复中提取最后一个assistant的回复内容"""
        # 查找最后一个assistant标记
        assistant_marker = "assistant"
        if assistant_marker in full_response:
            # 提取最后一个assistant标记后的内容
            parts = full_response.split(assistant_marker)
            response = parts[-1].strip()
            # 移除可能的前缀冒号和空格
            if response.startswith(":"):
                response = response[1:].strip()
        else:
            response = full_response
            
        # 去除可能的角色前缀
        prefixes = ["助手:", "助手：", "AI:", "AI："]
        for prefix in prefixes:
            if response.startswith(prefix):
                response = response[len(prefix):].strip()
                break

        return response 