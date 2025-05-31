"""配置管理模块，包含所有系统配置信息"""

import os


class Config:
    """配置管理类，负责存储和提供配置信息"""

    # 项目根目录
    ROOT_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

    # 数据目录
    DATA_DIR = os.path.join(ROOT_DIR, "data")
    USER_DATA_DIR = os.path.join(DATA_DIR, "user")
    MEMORY_DB_DIR = os.path.join(DATA_DIR, "memory")

    # 确保目录存在
    os.makedirs(USER_DATA_DIR, exist_ok=True)
    os.makedirs(MEMORY_DB_DIR, exist_ok=True)

    # 文件路径
    USER_DATA_FILE = os.path.join(USER_DATA_DIR, "user_profile.json")

    # 角色提示模板
    ROLE_PROMPT = {
        "USER_PROFILE_ANALYZER_SYSTEM": """
        你是一个用户画像提取专家。你的任务是从用户输入中提取所有能够确定的用户信息。
        请认真区分用户本人和其他人物关系，分析用户输入内容，自行判断并提取所有相关的用户属性信息。
        1. 基本信息：根据用户输入，根据实际内容灵活提取任何与用户相关的个人信息等。
        2. 事件：提取用户提及的事件，根据事件内容自行判断相关属性并提取包括事件的经过描述等。

        分析结果必须以JSON格式返回，不要有任何额外文字说明。
        返回格式严格按照：{'基本信息': {}, '事件': {}}
        """,
        "USER_PROFILE_ANALYZER_USER": """
        请分析以下用户输入，提取所有能得到的用户信息：
        {user_input}
        """,
        "CLUE_EXTRACTOR_SYSTEM": """
        你是一个精准线索提取专家。你的任务是根据已知信息，拆分生成1-3条能帮助回答用户当前问题的线索。
        1. 每条线索是一个完整的句子。
        2. 线索应简洁明了，每条不超过10个字。
        3. 每行一条线索，只需列出线索纯文本，不要添加标号以及任何解释文字。
        4. 无法提取线索则返回原问题。
        """,
        "CLUE_EXTRACTOR_USER": """
        ## 用户画像:
        {user_profile}

        ## 用户输入:
        {user_input}

        请提取能够帮助回答用户问题的关键线索：
        """,
        "RESPONSE_GENERATOR_SYSTEM": """
        你是一个智能助手。你的任务是根据对话上下文、相关记忆、核心线索，为用户提供友好、专业、符合用户需求的回答。
        回答应该：
        1. 针对用户的具体情况和需求
        2. 考虑用户的背景和历史信息
        3. 保持连贯性和上下文相关性
        """,
        "RESPONSE_GENERATOR_USER": """
        ## 对话上下文
        {context}

        ## 相关记忆
        {memories}

        ## 核心线索
        {clues}

        ## 当前问题
        {user_input}

        请根据以上信息，为用户提供个性化、准确、有深度的回答。注意保持语气友好，回答要有针对性且符合用户的背景知识水平。
        """,
    }

    # 模型配置
    MODEL_NAME = "openbmb/MiniCPM3-4B"
    EMBEDDING_MODEL_NAME = "BAAI/bge-small-zh-v1.5"  # 中文embedding模型

    # 对话配置
    MAX_HISTORY_ROUNDS = 3  # 上下文对话轮数，仅保存最近3轮对话
    SYSTEM_MESSAGE = "你是ai助手，请根据用户输入，给出最合适的回答。"

    # 生成参数
    MAX_NEW_TOKENS = 96
    TEMPERATURE = 0.5
    TOP_P = 0.9

    # 记忆配置
    MEMORY_RETRIEVAL_TOP_K = 5  # 记忆检索时返回的最相关记忆数量
    SIMILARITY_THRESHOLD = 0.6  # 余弦相似度阈值，仅保留相似度高于此值的记忆

    # 性能优化配置
    EMBEDDING_BATCH_SIZE = 4  # 嵌入向量批处理大小
    EMBEDDING_CACHE_SIZE = 64  # 嵌入向量缓存大小
    MAX_CLUES_TO_PROCESS = 3  # 最多处理的线索数量
    MEMORY_UPDATE_FREQUENCY = 3  # 每处理多少次用户输入更新一次工作记忆
    COLLECTION_DISTANCE_TYPE = "cosine"  # 向量距离计算方式

    # 显存管理配置
    MEMORY_CLEAR_FREQUENCY = 8  # 每处理多少次用户输入清理一次显存
    MEMORY_CLEAR_ENABLED = True  # 是否启用显存清理
