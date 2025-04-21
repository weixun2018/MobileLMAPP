"""
Author: knull-cc
Date: 2024-10-05
Description: This script crawls a webpage to extract links 
from a Q&A section based on a keyword search.
"""

from DrissionPage import Chromium, ChromiumOptions
import pandas as pd

co = ChromiumOptions()
co.headless()
tab = Chromium(co).latest_tab
tab.get("https://static.xinli001.com/msite/index.html#/search?keyword=大四")
tab.ele('x://li[@data-label="问答"]').click()

for i in range(1, 300):
    print(f'Currently on page {i}')
    tab.scroll.to_bottom()
    tab.wait(0.4)

links = [[i+1,item.attr('href')] for i,item in enumerate(tab.eles('x://li[@class="answer-item"]/a'))]
csv_file = 'links_2.csv'
df = pd.DataFrame(links, columns=['ID', 'Link'])
df = df.drop_duplicates(subset=['Link'])
df.to_csv(csv_file, index=False)
print(f'CSV file has been saved as {csv_file}')