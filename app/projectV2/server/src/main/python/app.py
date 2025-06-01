
from flask import Flask, jsonify, make_response
import news_crawler
import logging
import json  # 需要导入 json 模块

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
app.config['JSON_AS_ASCII'] = False  # 关键设置：禁用 ASCII 编码

@app.route('/crawl', methods=['POST'])
def crawl_news():
    try:
        logger.info("收到爬虫请求")
        # 调用爬虫逻辑
        raw_news = news_crawler.execute()
        logger.info(f"爬取到 {len(raw_news)} 条新闻")
        
        # 方法1：使用 jsonify 并设置配置
        # return jsonify(raw_news)
        
        # 方法2：手动创建 UTF-8 编码的响应（更可靠）
        response = make_response(
            json.dumps(raw_news, ensure_ascii=False, indent=2)
        )
        response.headers['Content-Type'] = 'application/json; charset=utf-8'
        return response
        
    except Exception as e:
        logger.exception("爬虫执行失败")
        return jsonify({
            "status": "error",
            "message": str(e)
        }), 500

@app.route('/health', methods=['GET'])
def health_check():
    return jsonify({"status": "healthy"}), 200

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000)
