'''
调用fastapi接口,实现一个简单的记忆能力评估器
'''
import requests
from typing import List, Dict
from rich.console import Console
from rich.table import Table
import os


class MemoryEvaluator:
    def __init__(self, test_cases: List[Dict]):
        self.test_cases = test_cases
        self.api_url = "http://127.0.0.1:8000/chat"
        self.console = Console()

    def _call_llm(self, message: str) -> str:
        """调用LLM API"""
        try:
            payload = {"message": message}
            response = requests.post(self.api_url, json=payload)
            response.raise_for_status()
            return response.json()["response"]
        except Exception as e:
            self.console.print(f"API调用错误: {str(e)}", style="red")
            return ""

    def _calculate_score(self, response: str, test_step: Dict) -> Dict:
        """计算得分 - 仅使用关键词匹配"""
        response_lower = response.lower()
        keywords = test_step["keywords"]

        # 统计找到的关键词
        found_keywords = []
        missing_keywords = []
        for kw in keywords:
            if kw.lower() in response_lower:
                found_keywords.append(kw)
            else:
                missing_keywords.append(kw)

        # 计算得分
        score = len(found_keywords) / len(keywords)

        return {
            "total_score": round(score, 2),
            "details": {
                "found_keywords": found_keywords,
                "missing_keywords": missing_keywords,
                "coverage": f"{len(found_keywords)}/{len(keywords)}",
            },
        }

    def evaluate_case(self, test_case: Dict) -> Dict:
        """评估单个测试用例"""
        results = []

        for step in test_case["context_chain"]:
            if "expected_answer" in step:
                # 这是一个记忆测试问题
                response = self._call_llm(step["input"])

                # 评分
                score_result = self._calculate_score(response, step)
                results.append(
                    {
                        "question": step["input"],
                        "expected": step["expected_answer"],
                        "response": response,
                        "score": score_result["total_score"],
                        "score_details": score_result["details"],
                        "memory_turns": step.get("memory_turns", 0),
                    }
                )
            else:
                # 这是上下文构建阶段
                self._call_llm(step["input"])

        return {"name": test_case["name"], "results": results}

    def run_evaluation(self) -> Dict:
        """运行所有测试用例"""
        all_results = []
        total_score = 0
        total_questions = 0

        for test_case in self.test_cases:
            case_result = self.evaluate_case(test_case)
            all_results.append(case_result)

            for result in case_result["results"]:
                total_score += result["score"]
                total_questions += 1

        return {
            "average_score": round(total_score / total_questions, 2),
            "detailed_results": all_results,
        }

    def print_report(self, results: Dict):
        """打印评估报告"""
        self.console.print("\n记忆能力评估报告", style="bold blue")
        self.console.print(
            f"总体平均得分: {results['average_score']:.2%}\n", style="green"
        )

        # 创建文件内容
        file_content = ["记忆能力评估报告", f"总体平均得分: {results['average_score']:.2%}\n"]

        for case_result in results["detailed_results"]:
            self.console.print(f"测试用例: {case_result['name']}", style="bold yellow")
            file_content.append(f"测试用例: {case_result['name']}")

            table = Table(show_header=True, header_style="bold magenta")
            table.add_column("问题")
            table.add_column("期望答案")
            table.add_column("实际回答")
            table.add_column("得分")
            table.add_column("关键词统计")
            table.add_column("记忆轮次")

            for result in case_result["results"]:
                score_style = "green" if result["score"] >= 0.8 else "red"

                # 格式化关键词统计
                details = f"覆盖率: {result['score_details']['coverage']}\n"
                if result["score_details"]["found_keywords"]:
                    details += f"找到: {', '.join(result['score_details']['found_keywords'])}\n"
                if result["score_details"]["missing_keywords"]:
                    details += f"缺失: {', '.join(result['score_details']['missing_keywords'])}"

                table.add_row(
                    result["question"],
                    result["expected"],
                    result["response"],
                    f"{result['score']:.0%}",
                    details,
                    str(result["memory_turns"]),
                    style=score_style,
                )

                # 添加到文件内容
                file_content.extend([
                    f"问题: {result['question']}",
                    f"期望答案: {result['expected']}",
                    f"实际回答: {result['response']}",
                    f"得分: {result['score']:.0%}",
                    f"关键词统计: {details}",
                    f"记忆轮次: {result['memory_turns']}",
                    ""
                ])

            self.console.print(table)
            self.console.print()
            file_content.append("-" * 80 + "\n")

        # 将结果写入文件
        current_dir = os.path.dirname(os.path.abspath(__file__))
        with open(os.path.join(current_dir, "score.txt"), "w", encoding="utf-8") as f:
            f.write("\n".join(file_content))


if __name__ == "__main__":
    from memory_test_cases import memory_test_cases

    evaluator = MemoryEvaluator(memory_test_cases)
    results = evaluator.run_evaluation()
    evaluator.print_report(results)
