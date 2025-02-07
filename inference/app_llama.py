import tkinter as tk
from tkinter import scrolledtext, messagebox
import requests
import json
import threading
import myConfig

class ChatApp:
    def __init__(self, root):
        self.root = root
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
        user_input = self.prompt_entry.get()
        if not user_input:
            return

        # 显示用户输入并清空输入框
        self._update_display(f"<｜User｜>{user_input}\n", "user")
        self.prompt_entry.delete(0, tk.END)

        # 在后台线程中处理请求, 保证页面不会卡死，不能操作
        threading.Thread(target=self._process_request, args=(user_input,), daemon=True).start()

    def _process_request(self, user_input):
        prompt = f"{self.history}<｜User｜>{user_input}<｜Assistant｜>"
        data = {
            "prompt": prompt,
            "stream": True
        }

        try:
            response = requests.post("http://127.0.0.1:8080/completions", json=data, stream=True)
            response.raise_for_status()

            ai_response = ""
            for line in response.iter_lines():
                if line:
                    decoded_line = line.decode('utf-8')
                    # tmp = decoded_line.get("data", "false")
                    # print(tmp)
                    print(decoded_line)
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

        except requests.exceptions.RequestException as e:
            self.root.after(0, lambda: messagebox.showerror("错误", f"请求失败: {e}"))

    def _update_display(self, text, tag=None):
        """线程安全地更新聊天记录"""
        self.root.after(0, lambda: self._thread_safe_display_update(text, tag))

    def _thread_safe_display_update(self, text, tag):
        """在主线程执行UI更新"""
        self.history_text.config(state=tk.NORMAL)
        self.history_text.insert(tk.END, text, tag)
        self.history_text.config(state=tk.DISABLED)
        self.history_text.yview(tk.END)

# 创建主窗口
if __name__ == "__main__":
    root = tk.Tk()
    app = ChatApp(root)
    root.mainloop()