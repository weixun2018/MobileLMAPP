"""
ChatApp - 一个简单的AI聊天应用程序

主要变量功能说明：
1. `root`: Tkinter的主窗口对象，用于构建GUI界面。
2. `history_text`: 用于显示聊天记录的滚动文本框。
3. `prompt_entry`: 用户输入框，用于输入聊天内容。
4. `send_button`: 发送按钮，用于触发发送请求。
5. `history`: 存储历史聊天记录的字符串。
6. `model`: 从`myConfig`模块中导入的AI模型配置, 包含 url 请求头中的必要参数信息， 以及特定的文本标识符，和结束标识

作者: 王雄
日期: 2025/2/15
"""
import time
import tkinter as tk
from tkinter import scrolledtext, messagebox
import requests
import json
import threading
import myConfig

class ChatApp:
    def __init__(self, root, model):
        """
        初始化ChatApp类，设置GUI界面和事件绑定。
        
        :param root: Tkinter的主窗口对象。
        """
        self.root = root
        self.model = model
        self.root.title("Chat with AI")

        # 创建滚动文本框用于显示历史聊天记录
        self.history_text = scrolledtext.ScrolledText(root, wrap=tk.WORD, width=60, height=20)
        self.history_text.pack(padx=10, pady=10)
        self.history_text.config(state=tk.DISABLED)  # 禁止用户编辑

        # 创建输入框和发送按钮
        self.prompt_entry = tk.Entry(root, width=60)
        self.prompt_entry.pack(padx=10, pady=5, side=tk.BOTTOM)
        self.send_button = tk.Button(root, text="Send", command=self.send_request)
        self.send_button.pack(padx=10, pady=5, side=tk.BOTTOM)

        # 初始化历史记录和颜色标签
        self.history = ""
        self.history_text.tag_configure("user", foreground="blue")
        self.history_text.tag_configure("ai", foreground="gray")
        self.root.bind('<Return>', lambda event: self.send_request())

    def send_request(self):
        """
        处理用户发送请求的逻辑。
        """
        user_input = self.prompt_entry.get()
        if not user_input:
            return

        # 显示用户输入并清空输入框
        self._update_display(f"用户: {user_input}\n", "user")
        self.prompt_entry.delete(0, tk.END)

        # 在后台线程中处理请求，保证页面不会卡死
        threading.Thread(target=self._process_request, args=(user_input,), daemon=True).start()

    def _process_request(self, user_input):
        """
        在后台线程中处理AI请求，获取AI的响应并更新UI。
        
        :param user_input: 用户输入的文本。
        """
        prompt = f"{self.history}{self.model.user}{user_input}{self.model.ai}"
        data = self.model.data
        data["prompt"] = prompt
        start_time = time.time()  # 开始计时

        try:
            response = self._send_api_request(data)
            ai_response = ""
            for line in response.iter_lines():
                if line:
                    decoded_line = line.decode('utf-8')
                    try:
                        response_data = json.loads(decoded_line[6:])
                        if "content" in response_data:
                            # 实时更新每个响应片段
                            self._update_display(response_data["content"], "ai")
                            ai_response += response_data["content"]

                        if response_data.get("stop", False):
                            self.history += ai_response
                            # 响应完成后换行
                            self._update_display("\n", "ai")
                            break
                    except json.JSONDecodeError as e:
                        print(f"JSON解析错误: {e}")

            # 计算生成文本的字数和耗时
            response_length = len(ai_response)
            elapsed_time = time.time() - start_time

            # 显示生成文本的字数和耗时
            self._update_display(f"生成文本字数: {response_length}\n", "ai")
            self._update_display(f"生成回答耗费时间: {elapsed_time:.2f}秒\n", "ai")
            
        except requests.exceptions.RequestException as e:
            self.root.after(0, lambda: messagebox.showerror("错误", f"请求失败: {e}"))

    def _send_api_request(self, data):
        """
        发送API请求并返回响应对象。
        
        :param data: 请求的数据。
        :return: 响应对象。
        """
        url = "http://127.0.0.1:8080/completions"
        response = requests.post(url, json=data, stream=True)
        response.raise_for_status()
        return response

    def _update_display(self, text, tag=None):
        """
        线程安全地更新聊天记录。
        
        :param text: 要显示的文本。
        :param tag: 文本的标签（用于颜色区分）。
        """
        self.root.after(0, lambda: self._thread_safe_display_update(text, tag))

    def _thread_safe_display_update(self, text, tag):
        """
        在主线程执行UI更新。
        
        :param text: 要显示的文本。
        :param tag: 文本的标签（用于颜色区分）。
        """
        self.history_text.config(state=tk.NORMAL)
        self.history_text.insert(tk.END, text, tag)
        self.history_text.config(state=tk.DISABLED)
        self.history_text.yview(tk.END)

# 创建主窗口
if __name__ == "__main__":
    root = tk.Tk()
    model = myConfig.MiniCPM()
    app = ChatApp(root, model=model)
    root.mainloop()