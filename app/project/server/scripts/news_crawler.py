# -*- coding: utf-8 -*-
# 导入必要的依赖
# - requests: HTTP 请求库
# - BeautifulSoup: HTML 解析工具
# - json: 数据序列化
# - datetime: 时间处理
# - sys: 系统功能
# - codecs: 编码处理
import requests
from bs4 import BeautifulSoup
import json
from datetime import datetime
import sys
import codecs

# 设置标准输出和错误输出的编码
# 确保中文内容正确显示
# 避免编码相关的错误
sys.stdout = codecs.getwriter('utf-8')(sys.stdout.buffer)
sys.stderr = codecs.getwriter('utf-8')(sys.stderr.buffer)

def crawl_psychology_news():
    # 目标网站 URL
    # 中国心理学网新闻页面
    url = "http://psy.china.com.cn/node_1013423.htm"
    
    # 设置请求头
    # 模拟浏览器访问
    # 避免被网站拒绝访问
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
    }

    try:
        # 发送 HTTP 请求
        # 记录请求过程
        print(f"Fetching URL: {url}", file=sys.stderr)
        response = requests.get(url, headers=headers)
        response.encoding = 'utf-8'  # 确保中文正确解码
        print(f"Response status code: {response.status_code}", file=sys.stderr)
        
        # 检查响应状态
        # 非 200 状态码表示请求失败
        if response.status_code != 200:
            print(f"Error: HTTP {response.status_code}", file=sys.stderr)
            return []

        # 解析 HTML 内容
        # 使用 BeautifulSoup 提取新闻信息
        soup = BeautifulSoup(response.text, 'html.parser')
        
        # 初始化新闻列表
        # 存储提取的新闻数据
        news_list = []
        
        # 查找新闻列表容器
        # 定位包含新闻的 div 元素
        list_text = soup.find('div', class_='list_text2')
        print(f"Found list_text2 div: {bool(list_text)}", file=sys.stderr)
        
        # 提取新闻数据
        # 限制获取前 10 条新闻
        if list_text:
            items = list_text.find_all('li')
            print(f"Found {len(items)} news items", file=sys.stderr)
            for item in items[:10]:  # 获取前 10 条新闻
                link = item.find('a')
                date = item.find('span')
                if link and date:
                    news_list.append({
                        'title': link.text.strip(),
                        'url': link['href'],
                        'date': date.text.strip()
                    })
        
        return news_list
    except Exception as e:
        # 错误处理
        # 记录详细错误信息
        print(f"Error crawling news: {str(e)}", file=sys.stderr)
        return []

# 主程序入口
# 执行爬虫并输出结果
if __name__ == "__main__":
    news = crawl_psychology_news()
    if not news:
        print("No news items found", file=sys.stderr)
    else:
        print(f"Found {len(news)} news items", file=sys.stderr)
    # 输出 JSON 格式的新闻数据
    print(json.dumps(news, ensure_ascii=False, indent=2)) 