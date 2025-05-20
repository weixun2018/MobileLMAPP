"""多轮对话记忆评估测试"""

import os
import json
from rich.table import Table
from rich.console import Console
from typing import List, Dict
from datetime import datetime

from src.app import ResponseProcessor


file_name = "example_1"
with open(
    f"ending/multi_turn_examples/extracted_examples/{file_name}.json", "r", encoding="utf-8"
) as f:
    conversation_test_cases = json.load(f)


# 添加MemoRAG适配器类，将main.py中的功能与evaluate_simple.py对接
class MemoRAG:
    """适配器类，将ResponseProcessor的功能封装为evaluate_simple.py需要的接口"""

    def __init__(self):
        """初始化记忆系统，使用main.py中的ResponseProcessor"""
        self.processor = ResponseProcessor()

    def chat(self, message: str) -> str:
        """处理用户输入并返回回复，这是evaluate_simple.py需要的接口"""
        try:
            response = self.processor.process_user_input(message)
            return response
        except Exception as e:
            print(f"处理消息时出错: {str(e)}")
            return f"处理失败: {str(e)}"


class MemoryEvaluator:
    def __init__(self, conversation_data: List[Dict], memory_system=None):
        self.conversation_data = conversation_data
        self.console = Console()
        self.memory_system = memory_system or MemoRAG()
        # 创建结果目录
        self.results_dir = "evaluation_results"
        os.makedirs(self.results_dir, exist_ok=True)
        # 初始化结果数据结构
        self.evaluation_results = {
            "timestamp": datetime.now().isoformat(),
            "score_questions": [],
            "overall_score": 0.0,
        }

    def _call_llm(self, message: str) -> str:
        """调用LLM获取回复"""
        try:
            return self.memory_system.chat(message)
        except Exception as e:
            self.console.print(f"调用错误: {str(e)}", style="red")
            return ""

    def _calculate_score(self, response: str, key_words: List[str]) -> Dict:
        """计算得分 - 使用关键词匹配"""
        response_lower = response.lower()

        if not key_words:
            return {
                "score": 0.0,
                "details": {"found_keywords": [], "missing_keywords": [], "coverage": "不适用"},
            }

        # 关键词匹配得分
        found_keywords = []
        missing_keywords = []
        for kw in key_words:
            if kw.lower() in response_lower:
                found_keywords.append(kw)
            else:
                missing_keywords.append(kw)

        keyword_score = len(found_keywords) / len(key_words) if key_words else 0

        return {
            "score": round(keyword_score, 2),
            "details": {
                "found_keywords": found_keywords,
                "missing_keywords": missing_keywords,
                "coverage": f"{len(found_keywords)}/{len(key_words)}",
            },
        }

    def run_evaluation(self) -> Dict:
        """运行评估"""
        score_results = []
        total_score = 0
        total_questions = 0

        # 首先处理所有的user消息，建立对话历史
        self.console.print(f"\n开始评估对话记忆...", style="blue")

        for item in self.conversation_data:
            if item["role"] == "user":
                self.console.print(f"\n用户: {item['content']}", style="yellow")
                response = self._call_llm(item["content"])
                self.console.print(f"回答: {response}", style="cyan")
            elif item["role"] == "score":
                # 到了评分环节
                question = item["question"]
                key_words = item["key_words"]

                self.console.print(f"\n评分问题: {question}", style="magenta")
                response = self._call_llm(question)
                self.console.print(f"回答: {response}", style="cyan")

                # 评分
                score_result = self._calculate_score(response, key_words)

                # 计算得分
                total_score += score_result["score"]
                total_questions += 1

                result = {
                    "question": question,
                    "key_words": key_words,
                    "response": response,
                    "score": score_result["score"],
                    "details": score_result["details"],
                }

                score_results.append(result)

        # 计算总体平均分
        overall_score = round(total_score / total_questions, 2) if total_questions > 0 else 0

        # 保存结果
        self.evaluation_results["score_questions"] = score_results
        self.evaluation_results["overall_score"] = overall_score
        self._save_results()

        return {"average_score": overall_score, "detailed_results": score_results}

    def _save_results(self):
        """保存评估结果到文件"""
        result_file = os.path.join(self.results_dir, f"memory_test_results_{file_name}.json")

        with open(result_file, "w", encoding="utf-8") as f:
            json.dump(self.evaluation_results, f, ensure_ascii=False, indent=2)

        self.console.print(f"\n评估结果已保存到: {result_file}", style="bold green")

    def print_report(self, results: Dict):
        """打印评估报告"""
        self.console.print("\n记忆能力评估报告", style="bold blue")
        self.console.print(f"总体平均得分: {results['average_score']:.2%}\n", style="green")

        # 创建详细结果表格
        table = Table(show_header=True, header_style="bold magenta")
        table.add_column("问题")
        table.add_column("关键词")
        table.add_column("实际回答")
        table.add_column("得分")
        table.add_column("关键词统计")

        for result in results["detailed_results"]:
            score_style = (
                "green"
                if result["score"] >= 0.8
                else ("yellow" if result["score"] >= 0.5 else "red")
            )

            # 格式化关键词统计
            details = f"覆盖率: {result['details']['coverage']}\n"
            if result["details"]["found_keywords"]:
                details += f"找到: {', '.join(result['details']['found_keywords'])}\n"
            if result["details"]["missing_keywords"]:
                details += f"缺失: {', '.join(result['details']['missing_keywords'])}"

            table.add_row(
                result["question"],
                ", ".join(result["key_words"]),
                result["response"][:100] + ("..." if len(result["response"]) > 100 else ""),
                f"{result['score']:.0%}",
                details,
                style=score_style,
            )

        self.console.print(table)
        self.console.print()


if __name__ == "__main__":
    import sys
    import argparse
    import os
    from rich.console import Console

    # 过滤掉Jupyter传递的参数
    jupyter_args = [arg for arg in sys.argv if arg.startswith("-f") or ".json" in arg]
    for arg in jupyter_args:
        if arg in sys.argv:
            sys.argv.remove(arg)
        # 同时移除可能的下一个参数（如果是文件路径）
        if arg == "-f" and len(sys.argv) > 1:
            sys.argv.remove(sys.argv[1])

    # 解析命令行参数
    parser = argparse.ArgumentParser(description="运行记忆评估测试")
    parser.add_argument("--output", type=str, help="指定结果输出目录", default="evaluation_results")
    parser.add_argument(
        "--file",
        type=str,
        help="指定测试文件路径",
        default="ending/multi_turn_examples/example_1.json",
    )
    args = parser.parse_args()

    # 创建控制台对象
    console = Console()

    try:
        # 读取指定的测试文件
        if args.file and args.file != "ending/multi_turn_examples/example_1.json":
            with open(args.file, "r", encoding="utf-8") as f:
                conversation_test_cases = json.load(f)
                console.print(f"从 {args.file} 加载测试数据", style="blue")

        # 创建评估器实例
        console.print("正在初始化评估器...", style="blue")
        evaluator = MemoryEvaluator(conversation_data=conversation_test_cases)

        if args.output:
            evaluator.results_dir = args.output
            os.makedirs(args.output, exist_ok=True)

        # 运行评估
        console.print("开始运行评估...", style="bold blue")
        results = evaluator.run_evaluation()
        # 打印报告
        evaluator.print_report(results)

    except Exception as e:
        console.print(f"评估过程中发生错误: {str(e)}", style="bold red")
        import traceback

        console.print(traceback.format_exc(), style="red")
        sys.exit(1)
