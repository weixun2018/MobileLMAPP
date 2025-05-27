"""用户画像管理模块，负责用户信息的提取和管理"""

import os
import json
import logging
from src.config.config import Config

# 配置日志
logging.basicConfig(
    level=logging.INFO, format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)


class UserProfileManager:
    """用户画像管理类，专注于结构化属性的提取和管理"""

    @staticmethod
    def load_data():
        """从本地文件加载用户数据"""
        if os.path.exists(Config.USER_DATA_FILE):
            try:
                with open(Config.USER_DATA_FILE, "r", encoding="utf-8") as f:
                    return json.load(f)
            except json.JSONDecodeError:
                print(f"读取文件{Config.USER_DATA_FILE}失败，创建新的用户数据")
        return {"基本信息": {}, "事件": []}

    @staticmethod
    def save_data(user_data):
        """保存用户数据到本地文件"""
        with open(Config.USER_DATA_FILE, "w", encoding="utf-8") as f:
            json.dump(user_data, f, ensure_ascii=False, indent=2)
        print(f"用户数据已保存到 {Config.USER_DATA_FILE}")

    @staticmethod
    def is_valid_value(value):
        """检查值是否有效"""
        return value not in [None, "", "未知", "null", "None", "未提供", "未提及"]

    @staticmethod
    def update_data(current_data, new_data):
        """更新用户数据，合并新信息"""
        if not new_data:
            return current_data

        try:
            # 更新基本信息（包括个人特征和兴趣爱好）
            for key, value in new_data.get("基本信息", {}).items():
                if key not in current_data["基本信息"] and UserProfileManager.is_valid_value(value):
                    current_data["基本信息"][key] = value
        except KeyError:
            # 如果没有基本信息字典，则创建一个
            current_data["基本信息"] = new_data.get("基本信息", {})

        try:
            # 如果新数据中有事件字典，添加到列表
            if isinstance(new_data.get("事件"), dict) and new_data.get("事件"):
                # 过滤掉事件中的无效值
                filtered_event = {
                    k: v
                    for k, v in new_data["事件"].items()
                    if UserProfileManager.is_valid_value(v)
                }
                current_data["事件"].append(filtered_event)
        except KeyError:
            # 如果没有事件列表，则创建一个
            current_data["事件"] = (
                [new_data.get("事件")]
                if isinstance(new_data.get("事件"), dict) and new_data.get("事件")
                else []
            )

        return current_data

    @staticmethod
    def extract_dict_from_response(response):
        """从模型响应中提取字典数据"""
        try:
            # 直接尝试将回复解析为JSON
            return json.loads(response.replace("'", '"'))
        except json.JSONDecodeError:
            # 如果直接解析失败，尝试清理回复内容
            cleaned_response = response.strip()
            # 查找第一个{和最后一个}之间的内容
            start = cleaned_response.find("{")
            end = cleaned_response.rfind("}") + 1

            if start >= 0 and end > start:
                try:
                    dict_str = cleaned_response[start:end]
                    return json.loads(dict_str.replace("'", '"'))
                except json.JSONDecodeError:
                    print("提取的字典格式不正确，无法解析")

            print("未能从响应中提取字典")
            return None
