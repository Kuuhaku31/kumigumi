# 创建配置文件

import json

import utils.utils as utils

index_list = []

with open("./index_list.json", "r", encoding="utf-8") as f:
    index_list = json.load(f)

utils.build_config_file(index_list, "./kumigumi.json")
