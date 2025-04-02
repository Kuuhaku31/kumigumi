# import utils.utils as utils

import mikananime.prase_rss_html as mikan

# url = "https://mikanani.me/RSS/Bangumi?bangumiId=3526"

# html_str = utils.request_html(url)

# # 保存到文件
# with open("data/test.xml", "w", encoding="utf-8") as f:
#     f.write(html_str)

html_src = ""
with open("data/test.xml", "r", encoding="utf-8") as f:
    html_src = f.read()

data_list = mikan.prase("test", html_src)
print(data_list)
