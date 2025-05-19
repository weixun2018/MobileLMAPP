# 提取jsonl文件中的对话内容，并转换为example格式
import json
import os
import re


def extract_conversations_from_jsonl(input_file):
    """从JSONL格式的文件中提取对话内容"""
    conversations = []
    try:
        with open(input_file, "r", encoding="utf-8") as f:
            for line in f:
                if line.strip():
                    try:
                        data = json.loads(line)
                        if "context" in data:
                            conversations.append(data)
                    except json.JSONDecodeError as e:
                        print(f"无法解析JSON行: {line[:50]}... 错误: {e}")
    except Exception as e:
        print(f"读取文件 {input_file} 时出错: {str(e)}")

    print(f"从 {input_file} 中提取了 {len(conversations)} 个对话")
    return conversations


def clean_content(text):
    """清理文本，移除可能的额外内容和特殊格式"""
    # 移除可能的问题提示和其他额外信息
    if "请记住以上全部对话记录，回答问题" in text:
        text = text.split("请记住以上全部对话记录，回答问题")[0].strip()

    # 移除其他可能的噪声
    text = re.sub(r'\\n["\n]+$', "", text)

    return text.strip()


def parse_conversation(context):
    """解析对话内容为对话列表"""
    # 初始化对话列表
    conversations = []

    # 查找所有对话块，不需要提取日期
    # 直接搜索用户和AI的对话对
    user_pattern = r"用户:(.*?)AI:"
    ai_pattern = r"AI:(.*?)(?=用户:|$)"

    # 找到所有用户输入
    user_matches = re.findall(user_pattern, context, re.DOTALL)
    # 找到所有AI回应
    ai_matches = re.findall(ai_pattern, context, re.DOTALL)

    # 确保用户和AI的对话对数量匹配
    pairs_count = min(len(user_matches), len(ai_matches))

    for i in range(pairs_count):
        user_content = clean_content(user_matches[i])
        ai_content = clean_content(ai_matches[i])

        if user_content and ai_content:
            conversations.append({"role": "user", "content": user_content})
            conversations.append({"role": "assistant", "content": ai_content})

    return conversations


def convert_to_example_format(conversations, output_dir, index):
    """将对话内容转换为example格式并保存到文件"""
    output_file = os.path.join(output_dir, f"example_{index}.json")

    if not conversations:
        print(f"没有对话内容可保存 (example_{index})")
        return False

    try:
        with open(output_file, "w", encoding="utf-8") as f:
            json.dump(conversations, f, ensure_ascii=False, indent=2)
        return True
    except Exception as e:
        print(f"保存到 {output_file} 时出错: {str(e)}")
        return False


def process_jsonl_to_examples(input_file, output_dir):
    """处理JSONL文件并转换为多个example文件"""
    # 确保输出目录存在
    os.makedirs(output_dir, exist_ok=True)

    # 提取所有对话
    conversations_data = extract_conversations_from_jsonl(input_file)

    success_count = 0
    for i, data in enumerate(conversations_data):
        # 解析对话内容
        conversations = parse_conversation(data["context"])

        # 只有当对话中有内容时才保存
        if conversations:
            # 转换并保存为example格式
            if convert_to_example_format(conversations, output_dir, i + 1):
                success_count += 1
                if success_count % 50 == 0:
                    print(f"已处理 {success_count} 个对话...")

    print(f"共转换 {success_count}/{len(conversations_data)} 个对话")


if __name__ == "__main__":
    # 设置输入和输出目录
    input_file = "data/small.jsonl"
    output_dir = "data/multi_turn_examples/extracted_examples"

    # 处理文件
    process_jsonl_to_examples(input_file, output_dir)
