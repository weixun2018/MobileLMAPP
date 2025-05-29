"""对话历史管理模块，用于记录和处理对话历史"""

import json
from langchain_core.messages import HumanMessage, AIMessage, SystemMessage, trim_messages
from ..config.config import Config


class MessageHistory:
    """消息历史管理类，处理当前对话历史"""

    def __init__(self):
        self.messages = []
        self.k = Config.MAX_HISTORY_ROUNDS  # 保存最近一轮完整对话

        # 添加系统消息
        self.messages.insert(0, SystemMessage(content=Config.SYSTEM_MESSAGE))

    def add_user_message(self, message):
        """添加用户消息"""
        self.messages.append(HumanMessage(content=message))
        self._trim_history()

    def add_ai_message(self, message):
        """添加AI消息"""
        self.messages.append(AIMessage(content=message))
        self._trim_history()
        
    def _trim_history(self):
        """裁剪历史消息，只保留系统消息和最近k轮对话"""
        # 保留系统消息
        system_messages = [msg for msg in self.messages if isinstance(msg, SystemMessage)]
        
        # 获取用户和AI消息（非系统消息）
        non_system_messages = [msg for msg in self.messages if not isinstance(msg, SystemMessage)]
        
        # 如果非系统消息超过2*k条（k轮完整对话 = k个用户消息 + k个AI消息）
        if len(non_system_messages) > 2 * self.k:
            # 只保留最近的2*k条非系统消息
            non_system_messages = non_system_messages[-2 * self.k:]
            
        # 重新组合消息列表
        self.messages = system_messages + non_system_messages

    def get_trimmed_messages(self):
        """获取经过trim处理的消息列表"""
        return trim_messages(
            self.messages,
            token_counter=len,  # 使用消息数量而非token数
            max_tokens=self.k * 2,  # 一轮对话包含一个用户消息和一个AI消息
            strategy="last",  # 保留最后的消息
            start_on="human",  # 确保历史从人类消息开始
            include_system=True,  # 保留系统消息(如果有)
            allow_partial=False,  # 不允许部分对话(确保问答对完整)
        )

    def to_string(self):
        """将消息历史转换为字符串形式"""
        context = []
        # 只获取非系统消息
        non_system_messages = [msg for msg in self.messages if not isinstance(msg, SystemMessage)]
        for msg in non_system_messages:
            if isinstance(msg, HumanMessage):
                context.append(f"用户: {msg.content}")
            elif isinstance(msg, AIMessage):
                context.append(f"助手: {msg.content}")
        return "\n".join(context)

    def clear(self):
        """清空消息历史"""
        self.messages = [SystemMessage(content=Config.SYSTEM_MESSAGE)]
        self.previous_qa_pair = None
