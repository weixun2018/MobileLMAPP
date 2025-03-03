'''
使用本地模型结合中转api与fastapi本地实现一个简单的对话接口
'''
import sqlite3
import requests
from typing import Dict, List, Tuple, Optional
from langgraph.graph import Graph, StateGraph
from dataclasses import dataclass, field
from langchain.schema import AIMessage, HumanMessage
from langchain_core.messages import BaseMessage
from rich.console import Console
from rich.theme import Theme
from fastapi import FastAPI
from pydantic import BaseModel
import uvicorn
from threading import local
import torch
from transformers import AutoModelForCausalLM, AutoTokenizer

# 创建线程本地存储
thread_local = local()

# 初始化rich console
custom_theme = Theme(
    {"info": "cyan", "warning": "yellow", "error": "red", "success": "green"}
)
console = Console(theme=custom_theme)

# 初始化自定义API配置
API_URL = "http://97.64.20.113:3002/v1/chat/completions"
API_KEY = "sk-FXpbI0siWTH7THyLpwLya7Yh2nBeAK6uOCyX7QFrnsAJhfiM"
API_HEADERS = {"Content-Type": "application/json", "Authorization": f"Bearer {API_KEY}"}

# 初始化HuggingFace模型
device = "cuda" if torch.cuda.is_available() else "cpu"
model_path = "openbmb/MiniCPM3-4B"  # 可以根据需要更换模型
tokenizer = AutoTokenizer.from_pretrained(model_path, trust_remote_code=True)
model = AutoModelForCausalLM.from_pretrained(
    model_path, torch_dtype=torch.float16, device_map="auto", trust_remote_code=True
)


def get_db_connection():
    """获取当前线程的数据库连接"""
    if not hasattr(thread_local, "connection"):
        thread_local.connection = sqlite3.connect("chat_memory.db")
    return thread_local.connection


def init_db():
    """初始化数据库表"""
    conn = get_db_connection()
    cursor = conn.cursor()

    tables = {
        "chat_history": """
            CREATE TABLE IF NOT EXISTS chat_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                human_message TEXT,
                ai_message TEXT,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """,
        "chat_summary": """
            CREATE TABLE IF NOT EXISTS chat_summary (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                summary TEXT,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """,
    }
    for sql in tables.values():
        cursor.execute(sql)
    conn.commit()


def generate_text_with_api(prompt, history=None):
    """使用API生成文本（用于总结和重构）"""
    if history is None:
        history = []

    messages = [
#         {
#             "role": "system",
#             "content": """你是一个有用的中文AI助手。请注意：
# 1. 始终使用用户的第一人称视角回答
# 2. 不要使用"你"来指代用户，应该使用"我"
# 3. 回答要完整包含所有相关信息
# 4. 对于时间相关的问题，要特别注意完整性和准确性
# 5. 在整合多条信息时，确保不遗漏任何关键细节""",
#         }
    ]

    for msg in history:
        messages.append(msg)
    messages.append({"role": "user", "content": prompt})

    try:
        response = requests.post(
            API_URL,
            headers=API_HEADERS,
            json={
                "model": "gpt-4o-mini",
                "messages": messages,
                "max_tokens": 1024,
                "temperature": 0.7,
                "top_p": 0.9,
            },
        )
        response.raise_for_status()
        return response.json()["choices"][0]["message"]["content"]
    except Exception as e:
        console.print(f"API调用错误: {str(e)}", style="error")
        return f"抱歉，发生了错误: {str(e)}"


def generate_text_with_model(prompt):
    """使用HuggingFace模型生成文本（用于最终回复）"""
    try:
        inputs = tokenizer(prompt, return_tensors="pt").to(device)
        with torch.no_grad():
            outputs = model.generate(
                **inputs,
                max_new_tokens=1024,
                do_sample=True,
                temperature=0.7,
                top_p=0.9,
                repetition_penalty=1.1,
            )
        response = tokenizer.decode(
            outputs[0][inputs.input_ids.shape[1] :], skip_special_tokens=True
        )
        return response.strip()
    except Exception as e:
        console.print(f"模型生成错误: {str(e)}", style="error")
        return f"抱歉，生成回复时发生错误: {str(e)}"


class MessageStore:
    """消息存储类"""

    def __init__(self):
        self.batch_size = 8
        init_db()

    def save_messages(self, messages: List[Tuple[str, str]]):
        """批量保存消息"""
        conn = get_db_connection()
        conn.executemany(
            "INSERT INTO chat_history (human_message, ai_message) VALUES (?, ?)",
            messages,
        )
        conn.commit()

    def check_and_generate_summary(self):
        """检查是否需要生成新的总结"""
        conn = get_db_connection()
        total_messages = conn.execute("SELECT COUNT(*) FROM chat_history").fetchone()[0]

        if total_messages % self.batch_size == 0:
            console.print(
                f"触发背景压缩,当前消息总数：{total_messages}", style="warning"
            )
            self._generate_new_summary()

    def _generate_new_summary(self):
        """用上一次背景和最近对话生成新的背景"""
        conn = get_db_connection()
        last_summary = conn.execute(
            "SELECT summary FROM chat_summary ORDER BY timestamp DESC LIMIT 1"
        ).fetchone()

        recent_messages = conn.execute(
            """
            SELECT human_message, ai_message FROM chat_history
            WHERE timestamp > (
                SELECT COALESCE(MAX(timestamp), '1970-01-01') FROM chat_summary
            )
            ORDER BY timestamp ASC
            LIMIT ?
            """,
            (self.batch_size,),
        ).fetchall()

        messages_text = "\n".join(
            [f"human: {msg[0]}\nai: {msg[1]}" for msg in recent_messages]
        )

        prompt = f"""
        ### 你是一位总结专家,请结合背景和近期对话内容,给出新的背景。请注意以下要点：
            1. 保留重要的上下文信息和关键细节
            2. 总结要有逻辑性和连贯性
            3. 确保总结能够帮助理解整个对话的发展脉络
            4. 突出对话中的重要主题和关键结论
            5. 保持总结的简洁性,避免冗余信息
            6. 如果有多个话题,需要清晰地区分不同话题
            7. 注意保留时间顺序和因果关系

        ### 现有背景：
        {last_summary[0] if last_summary else ''}

        ### 最近对话：
        {messages_text}

        ### 请提供一个整合的新背景,确保新背景充分传达对话的关键信息："""

        summary = generate_text_with_api(prompt)  # 使用API进行总结

        conn.execute("INSERT INTO chat_summary (summary) VALUES (?)", (summary,))
        conn.commit()

    def load_recent_messages(self, limit: int = 5) -> List[str]:
        """加载最近消息"""
        conn = get_db_connection()
        messages = list(
            reversed(
                conn.execute(
                    "SELECT human_message, ai_message, timestamp FROM chat_history ORDER BY timestamp DESC LIMIT ?",
                    (limit,),
                ).fetchall()
            )
        )
        return [f"human: {msg[0]} (时间: {msg[2]})\nai: {msg[1]}" for msg in messages]

    def get_latest_summary(self) -> Optional[str]:
        """获取最新的聊天背景总结"""
        conn = get_db_connection()
        summary = conn.execute(
            "SELECT summary FROM chat_summary ORDER BY timestamp DESC LIMIT 1"
        ).fetchone()
        return summary[0] if summary else None

    def get_recent_messages(self) -> Optional[str]:
        """获取最近的batch_size条对话记录"""
        recent_messages = self.load_recent_messages(self.batch_size)
        if recent_messages:
            return "\n".join(recent_messages)
        return None


class PersistentMemory:
    """持久化记忆类"""

    def __init__(self):
        self.messages = []
        self.store = MessageStore()
        self._load_recent_messages()

    def _load_recent_messages(self):
        """加载最近消息"""
        for message in self.store.load_recent_messages():
            human_msg, ai_msg = message.split("\nai: ")
            human_content = human_msg.replace("human: ", "").split(" (时间: ")[0]
            ai_content = ai_msg.split(" (时间: ")[0] if " (时间: " in ai_msg else ai_msg
            self.messages.extend(
                [HumanMessage(content=human_content), AIMessage(content=ai_content)]
            )

    def add_messages(self, messages: List[BaseMessage]):
        """批量添加消息"""
        self.messages.extend(messages)
        message_pairs = []
        for i in range(0, len(messages), 2):
            if i + 1 < len(messages):
                human_msg = messages[i]
                ai_msg = messages[i + 1]
                if isinstance(human_msg, HumanMessage) and isinstance(
                    ai_msg, AIMessage
                ):
                    message_pairs.append((human_msg.content, ai_msg.content))
        self.store.save_messages(message_pairs)
        self.store.check_and_generate_summary()


@dataclass
class ChatState:
    """对话状态类"""

    current_message: str = ""  # 当前用户输入
    rephrased_query: Optional[str] = None  # 重构后的查询
    memory: Optional[PersistentMemory] = None  # 记忆系统

    def __post_init__(self):
        if self.memory is None:
            self.memory = PersistentMemory()


def rephrase_query(state: ChatState) -> ChatState:
    """重构查询，将上下文信息整合到查询中"""
    if not state.memory.messages:
        state.rephrased_query = state.current_message
        return state

    console.print("正在整合上下文信息...", style="info")

    summary = state.memory.store.get_latest_summary()
    recent_messages = state.memory.store.get_recent_messages()

    prompt = f"""作为一个专注于对话理解和信息整合的AI助手，请帮我将用户的当前问题与历史上下文整合，生成一个包含完整信息的新问题。

### 对话背景：
{summary or "无对话摘要"}

### 近期对话：
{recent_messages or "无近期对话"}

### 用户当前问题：
{state.current_message}

### 任务要求：
1. 首先检查近期对话中是否已经包含了问题的答案
2. 检查所有历史记录中是否包含相关信息
3. 整合后的问题应该包含所有必要的历史信息和已知答案
4. 不要改变问题的本质，只是补充必要的上下文
5. 确保包含所有关键细节，不遗漏任何重要信息

### 整合原则：
- 优先提取近期对话中的相关答案作为已知信息
- 对于询问个人信息的问题，要完整引用之前提到的相关信息
- 如果问题涉及年龄、姓名等个人信息，要完整保留这些信息
- 避免使用代词，使用具体的名字和信息
- 保持问题的原始意图，只是补充必要的上下文信息

### 输出要求：
- 直接输出整合后的问题，不需要任何解释或说明
- 如果找到相关答案，以"已知[相关答案]，[用户问题]"的格式输出
- 严格按照用户的原始问题整合，不要自行添加或修改问题的意图
- 保持用户的第一人称视角"""

    state.rephrased_query = generate_text_with_api(prompt)  # 使用API进行重构
    console.print(f"整合后的问题：{state.rephrased_query}", style="info")
    return state


def generate_response(state: ChatState) -> ChatState:
    """生成回复"""
    prompt = f"""{state.rephrased_query}"""

    console.print("AI正在思考...", style="info")
    response = generate_text_with_model(prompt)  # 使用本地模型生成最终回复

    messages_to_add = [
        HumanMessage(content=state.current_message),
        AIMessage(content=response),
    ]
    state.memory.add_messages(messages_to_add)

    return state


def create_chat_graph() -> Graph:
    """创建对话工作流"""
    workflow = StateGraph(ChatState)

    nodes = {
        "rephrase_query": rephrase_query,
        "generate_response": generate_response,
    }

    for name, func in nodes.items():
        workflow.add_node(name, func)

    workflow.set_entry_point("rephrase_query")
    workflow.add_edge("rephrase_query", "generate_response")
    workflow.set_finish_point("generate_response")

    return workflow.compile()


class ChatRequest(BaseModel):
    message: str


class ChatResponse(BaseModel):
    response: str


app = FastAPI()


@app.post("/chat")
async def chat_endpoint(request: ChatRequest) -> ChatResponse:
    try:
        # 初始化对话图和记忆系统
        graph = create_chat_graph()
        memory = PersistentMemory()

        # 创建对话状态并执行
        state = ChatState(
            current_message=request.message,
            memory=memory,
        )
        final_state = graph.invoke(state)

        # 获取AI回复
        if memory.messages and isinstance(memory.messages[-1], AIMessage):
            return ChatResponse(response=memory.messages[-1].content)
        else:
            return ChatResponse(response="抱歉，我现在无法正确回应。请再说一遍。")

    except Exception as e:
        console.print(f"错误: {str(e)}", style="error")
        return ChatResponse(response=f"发生错误: {str(e)}")


if __name__ == "__main__":
    import nest_asyncio
    import threading

    nest_asyncio.apply()

    # 初始化数据库
    init_db()

    def run_server():
        config = uvicorn.Config(app, host="0.0.0.0", port=8000, log_level="info")
        server = uvicorn.Server(config)
        server.run()

    # 在后台线程中启动服务器
    server_thread = threading.Thread(target=run_server, daemon=True)
    server_thread.start()

    # 等待服务器启动
    import time

    time.sleep(2)

    # 打印服务器状态
    console.print("服务器已在后台启动...", style="success")