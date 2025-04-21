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
        print(f"开始获取URL: {url}", file=sys.stderr)
        response = requests.get(url, headers=headers)
        response.encoding = 'utf-8'  # 确保中文正确解码
        print(f"响应状态码: {response.status_code}", file=sys.stderr)
        
        # 检查响应状态
        # 非 200 状态码表示请求失败
        if response.status_code != 200:
            print(f"错误: HTTP {response.status_code}", file=sys.stderr)
            return []

        # 解析 HTML 内容
        # 使用 BeautifulSoup 提取新闻信息
        soup = BeautifulSoup(response.text, 'html.parser')
        
        # 初始化新闻列表
        # 存储提取的新闻数据
        news_list = []
        
        # 查找所有的新闻li元素
        news_items = soup.select('li b a')  # 使用更精确的选择器
        print(f"找到 {len(news_items)} 个新闻项", file=sys.stderr)
        
        for link in news_items[:10]:  # 获取前10条新闻
            try:
                if not link.has_attr('href'):
                    continue
                
                # 获取标题和链接
                title = link.get_text(strip=True)
                news_url = link['href']
                if not news_url.startswith('http'):
                    news_url = 'http://psy.china.com.cn' + news_url
                
                # 获取日期
                date_span = link.find_parent('li').find('span')
                publish_date = date_span.get_text(strip=True) if date_span else ''
                
                print(f"处理新闻: {title}", file=sys.stderr)
                
                news_list.append({
                    'title': title,
                    'url': news_url,
                    'link': news_url,
                    'publishDate': publish_date
                })
                
                print(f"成功处理新闻: {title}", file=sys.stderr)
                
            except Exception as e:
                print(f"处理新闻项时出错: {str(e)}", file=sys.stderr)
                continue
        
        print(f"成功获取 {len(news_list)} 条新闻", file=sys.stderr)
        return news_list
    except Exception as e:
        # 错误处理
        # 记录详细错误信息
        print(f"爬取新闻时出错: {str(e)}", file=sys.stderr)
        return []

# 主程序入口
# 执行爬虫并输出结果
if __name__ == "__main__":
    news = crawl_psychology_news()
    if not news:
        print("未找到新闻", file=sys.stderr)
    else:
        print(f"Found {len(news)} news items", file=sys.stderr)
    # 输出 JSON 格式的新闻数据
    print(json.dumps(news, ensure_ascii=False, indent=2)) 