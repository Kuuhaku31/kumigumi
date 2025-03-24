# main.py

import json

import bangumi.update as ba_update

config_data = {}

# 读取配置文件
with open("./kumigumi.json", "r", encoding="utf-8") as f:
    config_data = json.load(f)

print("配置文件读取成功")
print("开始更新bangumi动画信息...")

url_list = []
for anime in config_data["动画信息"]:
    url_list.append(anime["链接"]["bangumi"])

ba_update.update_csv(url_list, config_data["配置信息"]["动画数据文件"], config_data["配置信息"]["单集数据文件"])
print("更新完成")
