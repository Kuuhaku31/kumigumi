# 日志存储

import json

# import requests

url = "https://mikanani.me/RSS/MyBangumi?token=xzis5q9HJHqZudKJbY7KL2bci%2bAmLEJ9URet6OgIaoQ%3d"
path = "F:/log.json"

# re = requests.get(url)

# with open(path, "wb") as f:
#     f.write(re.content)
#     print("下载完成")


# 读取现有的 JSON 文件
with open(path, encoding="utf-8") as file:
    data = json.load(file)

# 创建新的 item 节点
new_item = {"title": "New sakana Title", "link": "http://example.com/new-item", "description": "This is a new item."}

# 将新的 item 节点添加到 JSON 数据的顶部
if "items" in data:
    data["items"].insert(0, new_item)
else:
    data["items"] = [new_item]

# 保存修改后的 JSON 文件
with open(path, "w", encoding="utf-8") as file:
    json.dump(data, file, ensure_ascii=False, indent=4)

print("新的 item 节点已添加并保存到 JSON 文件的顶部")
