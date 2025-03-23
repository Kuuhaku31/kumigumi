##

import json

config_info = None  # 配置信息


def init(config_file: str):
    global config_info
    with open(config_file, encoding="utf-8") as f:
        config_info = json.load(f)


def get_config() -> json:
    return config_info
