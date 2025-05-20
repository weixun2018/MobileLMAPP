"""对话评估测试"""

import os
import json
import sys

# 添加项目根目录到Python路径
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from rich.table import Table
from rich.console import Console
from typing import List, Dict
from datetime import datetime
from src.app import ResponseProcessor


memory_test_cases = [
    {
        "name": "基础信息记忆测试",
        "context_chain": [
            # 设置基础信息
            {
                "input": "我叫张三，今年28岁，是一名数据科学家，在字节跳动工作，月薪5万，住在上海浦东",
                "expected_response": "好的，已了解你的基本信息",
            },
            {
                "input": "我的爱好是打篮球和弹吉他，毕业于清华大学，有一只叫小白的猫",
                "expected_response": "好的，已了解你的个人信息",
            },
            # 10轮基础信息测试
            {
                "input": "我叫什么名字？",
                "keywords": ["张三"],
            },
            {
                "input": "我多大年纪？",
                "keywords": ["28"],
            },
            {
                "input": "我的职业是什么？",
                "keywords": ["数据科学家"],
            },
            {
                "input": "我在哪里工作？",
                "keywords": ["字节"],
            },
            {
                "input": "我的月薪是多少？",
                "keywords": ["5万"],
            },
            {
                "input": "我住在哪里？",
                "keywords": ["上海", "浦东"],
            },
            {
                "input": "我有什么爱好？",
                "keywords": ["篮球", "吉他"],
            },
            {
                "input": "我养了什么宠物？它叫什么名字？",
                "keywords": ["小白", "猫"],
            },
            {
                "input": "我是哪个学校毕业的？",
                "keywords": ["清华"],
            },
            {
                "input": "我的基本情况是什么？",
                "keywords": ["张三", "28", "数据科学家", "字节跳动"],
            },
        ],
    },
    {
        "name": "时间序列记忆测试",
        "context_chain": [
            # 设置时间安排
            {
                "input": "周一的安排：上午9点产品会议，下午2点见客户，晚上7点团建",
                "expected_response": "好的，已记录周一的安排",
            },
            {
                "input": "周二的安排：上午10点述职，下午3点培训，晚上6点部门会",
                "expected_response": "好的，已记录周二的安排",
            },
            {
                "input": "周三的安排：上午11点面试，下午4点评审会，晚上8点加班",
                "expected_response": "好的，已记录周三的安排",
            },
            # 10轮时间安排测试
            {
                "input": "周一上午几点开会？是什么会议？",
                "keywords": ["9点", "产品"],
            },
            {
                "input": "周一下午什么安排？",
                "keywords": ["2点", "客户"],
            },
            {
                "input": "周一晚上什么安排？具体时间？",
                "keywords": ["7点", "团建"],
            },
            {
                "input": "周二上午是什么安排？具体时间？",
                "keywords": ["10点", "述职"],
            },
            {
                "input": "周二下午具体什么安排？具体时间？",
                "keywords": ["3点", "培训"],
            },
            {
                "input": "周二晚上具体什么安排？具体时间？",
                "keywords": ["6点", "部门会"],
            },
            {
                "input": "周三上午具体什么安排？具体时间？",
                "keywords": ["11点", "面试"],
            },
            {
                "input": "周三下午具体什么安排？具体时间？",
                "keywords": ["4点", "评审"],
            },
            {
                "input": "周三晚上具体什么安排？具体时间？",
                "keywords": ["8点", "加班"],
            },
            {
                "input": "周一全天的安排是什么？具体时间？",
                "keywords": ["9点", "2点", "7点", "产品", "客户", "团建"],
            },
        ],
    },
    {
        "name": "项目信息记忆测试",
        "context_chain": [
            # 设置项目信息
            {
                "input": "项目基本信息：AI助手项目，预算500万，15人团队，使用Python和TensorFlow开发，预计6个月完成",
                "expected_response": "好的，已了解项目基本信息",
            },
            {
                "input": "团队构成：3个前端(React)，5个后端(Python)，4个算法(TensorFlow)，2个测试，1个产品",
                "expected_response": "好的，已了解团队构成",
            },
            {
                "input": "项目进度：需求分析完成90%，架构设计完成80%，前端完成60%，后端完成45%，算法完成30%，整体完成50%",
                "expected_response": "好的，已了解项目进度",
            },
            # 10轮项目信息测试
            {
                "input": "这是什么项目？",
                "keywords": ["AI", "助手"],
            },
            {
                "input": "项目预算多少？",
                "keywords": ["500万"],
            },
            {
                "input": "团队有多少人？",
                "keywords": ["15"],
            },
            {
                "input": "使用什么技术栈？",
                "keywords": ["Python", "TensorFlow"],
            },
            {
                "input": "项目预计多久完成？",
                "keywords": ["6个月"],
            },
            {
                "input": "前端团队情况如何？",
                "keywords": ["3", "前端", "React"],
            },
            {
                "input": "后端和算法团队各有多少人？",
                "keywords": ["5", "后端", "4", "算法"],
            },
            {
                "input": "需求分析和架构设计的进度？",
                "keywords": ["90%", "80%"],
            },
            {
                "input": "目前开发进度如何？",
                "keywords": ["60%", "45%", "30%"],
            },
            {
                "input": "整体项目完成度是多少？",
                "keywords": ["50%"],
            },
        ],
    },
    {
        "name": "人物关系记忆测试",
        "context_chain": [
            # 设置人物关系
            {
                "input": "团队成员：张三是项目负责人，李四负责前端，王五负责后端，赵六负责算法",
                "expected_response": "好的，已了解团队分工",
            },
            {
                "input": "工作职责：张三负责项目管理，李四负责用户界面，王五负责接口开发，赵六负责模型训练",
                "expected_response": "好的，已了解工作职责",
            },
            {
                "input": "项目进展：李四完成了登录界面，王五完成了用户接口，赵六优化了模型性能",
                "expected_response": "好的，已了解项目进展",
            },
            # 10轮人物关系测试
            {
                "input": "谁是项目负责人？",
                "keywords": ["张三", "负责人"],
            },
            {
                "input": "李四负责什么工作？",
                "keywords": ["李四", "前端", "界面"],
            },
            {
                "input": "后端开发是谁负责的？",
                "keywords": ["王五", "后端"],
            },
            {
                "input": "赵六在团队中是什么角色？",
                "keywords": ["赵六", "算法", "模型"],
            },
            {
                "input": "张三具体负责什么？",
                "keywords": ["张三", "项目管理"],
            },
            {
                "input": "前端开发的进展如何？",
                "keywords": ["李四", "登录界面"],
            },
            {
                "input": "王五完成了什么工作？",
                "keywords": ["王五", "用户接口"],
            },
            {
                "input": "赵六在项目中做了什么？",
                "keywords": ["赵六", "模型", "性能"],
            },
            {
                "input": "谁负责用户界面开发？",
                "keywords": ["李四", "界面"],
            },
            {
                "input": "接口开发是谁负责的？",
                "keywords": ["王五", "接口"],
            },
        ],
    },
    {
        "name": "情感记忆测试",
        "context_chain": [
            {
                "input": "我特别喜欢吃川菜，但是不能吃太辣",
                "expected_response": "好的，已了解您的饮食偏好",
            },
            {
                "input": "我害怕坐飞机，但是经常需要出差",
                "expected_response": "好的，已了解您的出行情况",
            },
            {
                "input": "我最讨厌开长会，特别是下午的会议",
                "expected_response": "好的，已了解您的工作偏好",
            },
            # 情感相关测试
            {
                "input": "我对什么食物有特别的偏好？",
                "keywords": ["川菜", "辣"],
            },
            # 补充情感记忆测试
            {
                "input": "我最喜欢的运动是什么？最讨厌的运动是什么？",
                "keywords": ["篮球", "跑步"],
            },
            {
                "input": "我对加班的态度是怎样的？",
                "keywords": ["不喜欢", "接受"],
            },
        ],
    },
]


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
    def __init__(self, test_cases: List[Dict], memory_system=None):
        self.test_cases = test_cases
        self.console = Console()
        self.memory_system = memory_system or MemoRAG()
        # 创建结果目录
        self.results_dir = "data/evaluation_results"
        os.makedirs(self.results_dir, exist_ok=True)
        # 初始化结果数据结构
        self.evaluation_results = {
            "timestamp": datetime.now().isoformat(),
            "test_cases": [],
            "overall_score": 0.0,
        }

    def _call_llm(self, message: str) -> str:
        """调用LLM获取回复"""
        try:
            return self.memory_system.chat(message)
        except Exception as e:
            self.console.print(f"调用错误: {str(e)}", style="red")
            return ""

    def _calculate_score(self, response: str, test_step: Dict) -> Dict:
        """计算得分 - 使用关键词匹配"""
        response_lower = response.lower()
        keywords = test_step.get("keywords", [])

        if not keywords:
            # 如果没有关键词，尝试使用expected_answer字段进行简单匹配
            return {
                "score": 1.0 if response else 0.0,
                "details": {"found_keywords": [], "missing_keywords": [], "coverage": "不适用"},
            }

        # 关键词匹配得分
        found_keywords = []
        missing_keywords = []
        for kw in keywords:
            if kw.lower() in response_lower:
                found_keywords.append(kw)
            else:
                missing_keywords.append(kw)

        keyword_score = len(found_keywords) / len(keywords) if keywords else 0

        return {
            "score": round(keyword_score, 2),
            "details": {
                "found_keywords": found_keywords,
                "missing_keywords": missing_keywords,
                "coverage": f"{len(found_keywords)}/{len(keywords)}",
            },
        }

    def evaluate_case(self, test_case: Dict) -> Dict:
        """评估单个测试用例"""
        results = []
        case_score = 0.0
        total_questions = 0

        self.console.print(f"\n正在测试: {test_case['name']}", style="blue")

        for step_idx, step in enumerate(test_case["context_chain"]):
            if "keywords" in step:
                # 这是一个测试问题
                self.console.print(f"\n问题 [{step_idx+1}]: {step['input']}", style="yellow")
                response = self._call_llm(step["input"])
                self.console.print(f"回答: {response}", style="cyan")

                # 评分
                score_result = self._calculate_score(response, step)

                # 计算本用例得分
                case_score += score_result["score"]
                total_questions += 1

                results.append(
                    {
                        "question": step["input"],
                        "expected": step.get("expected_answer", "关键词检测"),
                        "response": response,
                        "score": score_result["score"],
                        "details": score_result["details"],
                    }
                )
            else:
                # 这是上下文设置
                self.console.print(f"设置上下文 [{step_idx+1}]: {step['input']}", style="dim")
                response = self._call_llm(step["input"])
                self.console.print(f"回答: {response}", style="dim cyan")

        # 计算本测试用例的平均分
        avg_case_score = case_score / total_questions if total_questions > 0 else 0

        return {
            "name": test_case["name"],
            "results": results,
            "score": round(avg_case_score, 2),
            "total_questions": total_questions,
        }

    def run_evaluation(self) -> Dict:
        """运行所有测试用例"""
        all_results = []
        total_score = 0
        total_questions = 0

        for test_case in self.test_cases:
            case_result = self.evaluate_case(test_case)
            all_results.append(case_result)

            total_score += case_result["score"] * case_result["total_questions"]
            total_questions += case_result["total_questions"]

        overall_score = round(total_score / total_questions, 2) if total_questions > 0 else 0

        # 保存到结果数据结构
        self.evaluation_results["test_cases"] = all_results
        self.evaluation_results["overall_score"] = overall_score

        # 保存结果
        self._save_results()

        return {"average_score": overall_score, "detailed_results": all_results}

    def _save_results(self):
        """保存评估结果到文件"""
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        result_file = f"data/evaluation_results/memory_test_results_{timestamp}.json"

        with open(result_file, "w", encoding="utf-8") as f:
            json.dump(self.evaluation_results, f, ensure_ascii=False, indent=2)

        self.console.print(f"\n评估结果已保存到: {result_file}", style="bold green")

    def print_report(self, results: Dict):
        """打印评估报告"""
        self.console.print("\n记忆能力评估报告", style="bold blue")
        self.console.print(f"总体平均得分: {results['average_score']:.2%}\n", style="green")

        # 创建测试用例概览表
        overview_table = Table(show_header=True, header_style="bold blue")
        overview_table.add_column("测试用例")
        overview_table.add_column("问题数量")
        overview_table.add_column("平均得分")

        for case_result in results["detailed_results"]:
            score_style = (
                "green"
                if case_result["score"] >= 0.8
                else ("yellow" if case_result["score"] >= 0.5 else "red")
            )
            overview_table.add_row(
                case_result["name"],
                str(case_result["total_questions"]),
                f"{case_result['score']:.0%}",
                style=score_style,
            )

        self.console.print(overview_table)
        self.console.print()

        # 打印每个测试用例的详细结果
        for case_result in results["detailed_results"]:
            self.console.print(
                f"测试用例: {case_result['name']} (平均得分: {case_result['score']:.0%})",
                style="bold yellow",
            )

            if not case_result["results"]:
                self.console.print("无测试结果", style="red")
                continue

            table = Table(show_header=True, header_style="bold magenta")
            table.add_column("问题")
            table.add_column("期望检测")
            table.add_column("实际回答")
            table.add_column("得分")
            table.add_column("关键词统计")

            for result in case_result["results"]:
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
                    result["expected"],
                    result["response"][:100] + ("..." if len(result["response"]) > 100 else ""),
                    f"{result['score']:.0%}",
                    details,
                    style=score_style,
                )

            self.console.print(table)
            self.console.print()

    def filter_test_cases(self, skip=None, only=None):
        """根据名称过滤测试用例"""
        original_count = len(self.test_cases)

        if only:
            # 只保留指定的测试用例
            self.test_cases = [
                tc
                for tc in self.test_cases
                if any(name.lower() in tc["name"].lower() for name in only)
            ]
            self.console.print(
                f"根据 --only 参数过滤，保留 {len(self.test_cases)}/{original_count} 个测试用例",
                style="yellow",
            )

        if skip:
            # 排除指定的测试用例
            self.test_cases = [
                tc
                for tc in self.test_cases
                if not any(name.lower() in tc["name"].lower() for name in skip)
            ]
            self.console.print(
                f"根据 --skip 参数过滤，保留 {len(self.test_cases)}/{original_count} 个测试用例",
                style="yellow",
            )

        if not self.test_cases:
            self.console.print("警告：过滤后没有剩余测试用例", style="bold red")


if __name__ == "__main__":
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
    parser = argparse.ArgumentParser(description="运行简化版记忆评估测试")
    parser.add_argument("--skip", nargs="+", help="跳过指定的测试用例")
    parser.add_argument("--only", nargs="+", help="只运行指定的测试用例")
    parser.add_argument("--list", action="store_true", help="列出所有可用的测试用例")
    parser.add_argument("--output", type=str, help="指定结果输出目录", default="evaluation_results")
    args = parser.parse_args()

    # 创建控制台对象
    console = Console()

    try:
        # 创建评估器实例 - 不传递filtered_test_cases参数，让MemoryEvaluator使用默认的测试用例
        console.print("正在初始化评估器...", style="blue")
        evaluator = MemoryEvaluator(test_cases=memory_test_cases)

        # 根据命令行参数过滤测试用例
        if args.list:
            # 列出所有测试用例并退出
            console.print("可用的测试用例:", style="bold green")
            for test_case in evaluator.test_cases:
                console.print(f"- {test_case['name']}")
            sys.exit(0)

        if args.only:
            evaluator.filter_test_cases(only=args.only)

        if args.skip:
            evaluator.filter_test_cases(skip=args.skip)

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
