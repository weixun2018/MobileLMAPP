"""主程序模块，整合所有组件并提供统一的接口"""

import json
import time
from models.model_interface import ModelInterface
from managers.message_history import MessageHistory
from managers.user_profile import UserProfileManager
from managers.memory import MemoryManager
from config.config import Config


class ResponseProcessor:
    """响应处理类，负责处理用户输入并生成回答"""

    def __init__(self):
        self.model_interface = ModelInterface()
        self.conversation_history = MessageHistory()
        self.user_profile_manager = UserProfileManager()
        self.memory_manager = MemoryManager(self.model_interface)

    def analyze_user_info(self, user_input):
        """提取用户个人信息"""
        # 使用提示模板
        user_content = Config.ROLE_PROMPT["USER_PROFILE_ANALYZER_USER"].format(
            user_input=user_input
        )

        prompt = [
            {"role": "system", "content": Config.ROLE_PROMPT["USER_PROFILE_ANALYZER_SYSTEM"]},
            {"role": "user", "content": user_content},
        ]

        # 生成回复
        response = self.model_interface.generate_response(prompt)
        print(f"用户画像分析结果: {response}")

        return self.user_profile_manager.extract_dict_from_response(response)

    def extract_clues(self, user_input, user_profile):
        """从用户输入和用户画像中提取关键线索"""
        # 格式化用户画像
        user_profile_str = json.dumps(user_profile, ensure_ascii=False)

        # 使用提示模板
        user_content = Config.ROLE_PROMPT["CLUE_EXTRACTOR_USER"].format(
            user_profile=user_profile_str, user_input=user_input
        )

        prompt = [
            {"role": "system", "content": Config.ROLE_PROMPT["CLUE_EXTRACTOR_SYSTEM"]},
            {"role": "user", "content": user_content},
        ]

        # 生成回复
        response = self.model_interface.generate_response(prompt)
        print(f"提取的线索: {response}")

        return response

    def generate_response(self, user_input, user_profile, clues, context, relevant_memories):
        """生成最终回答"""
        # 格式化各部分信息
        user_profile_str = json.dumps(user_profile, ensure_ascii=False)
        memories_str = self.memory_manager.format_memories_for_context(relevant_memories)

        # 使用提示模板
        user_content = Config.ROLE_PROMPT["RESPONSE_GENERATOR_USER"].format(
            user_profile=user_profile_str,
            memories=memories_str,
            clues=clues,
            context=context,
            user_input=user_input,
        )
        print(f"user_content: {user_content}")
        prompt = [
            {"role": "system", "content": Config.ROLE_PROMPT["RESPONSE_GENERATOR_SYSTEM"]},
            {"role": "user", "content": user_content},
        ]

        # 生成回复
        response = self.model_interface.generate_response(prompt)
        return response

    def process_user_input(self, user_input):
        """整合完整流程处理用户输入

        Args:
            user_input: 用户输入的消息
        """
        # 获取当前对话上下文（不包含本轮用户提问）
        context = self.conversation_history.to_string()
        
        # 添加用户消息到对话历史
        self.conversation_history.add_user_message(user_input)

        # 步骤1: 提取用户属性信息（结构化属性）
        user_data = self.user_profile_manager.load_data()
        new_user_info = self.analyze_user_info(user_input)
        if new_user_info:
            user_data = self.user_profile_manager.update_data(user_data, new_user_info)
            self.user_profile_manager.save_data(user_data)

        # 步骤2: 提取答案线索（仅结合用户画像和输入）
        clues = self.extract_clues(user_input, user_data)

        # 步骤3: 使用线索检索相关记忆（从记忆集合中）
        relevant_memories = self.memory_manager.retrieve_relevant_memories_by_clues(clues)
        print(f"relevant_memories: {relevant_memories}")

        # 步骤4: 生成回答（整合所有信息）
        response = self.generate_response(user_input, user_data, clues, context, relevant_memories)

        # 步骤5: 更新对话历史和记忆系统（长期）
        self.conversation_history.add_ai_message(response)
        self.memory_manager.add_memory(user_input, response)
        return response


def main():
    """主程序入口"""
    # 创建响应处理器
    processor = ResponseProcessor()

    print("智能助手已启动，输入'退出'结束对话")
    while True:
        user_input = input("用户: ")
        if user_input.lower() in ["退出", "exit", "quit"]:
            print("系统已退出，谢谢使用！")
            break

        start_time = time.time()
        # 使用默认配置生成响应
        response = processor.process_user_input(user_input)
        end_time = time.time()

        print(f"AI: {response}")
        print(f"总处理时间: {end_time - start_time:.2f} 秒")


if __name__ == "__main__":
    main()
