
class DeepSeek():
    def __init__(self):
        super().__init__()
        self.user = "<｜User｜>"
        self.ai = "<｜Assistant｜>"
        self.data = {
            "prompt": "",
            "stream": True,
            "n_predict": 256,
        }

class MiniCPM():
    def __init__(self):
        super().__init__()
        self.user = "<用户>"
        self.ai = "<AI>"
        self.data = data = {
            "model": "minicpm-4b-q4",
            "prompt": "",
            "n_predict": 256,
            "stream": True
        }

