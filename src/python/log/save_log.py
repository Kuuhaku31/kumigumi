# data_list

import json
import os
from datetime import datetime

import Log.__init__ as init

log_path = init.log_path


def log(data_lists):
    # 如果文件夹不存在，则创建
    if not os.path.exists(log_path):
        os.makedirs(log_path)

    date = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    save_path = log_path + date + ".json"

    print("log saving to " + save_path)

    with open(save_path, "w", encoding="utf-8") as f:
        json_log = {"items": []}

        for data_list in data_lists:
            # 添加到字典
            json_log["items"].append(data_list)

        # 保存到 JSON 文件
        # ensure_ascii=False 保证中文不会被转义
        # indent=4 保证 JSON 数据格式化输出
        f.write(json.dumps(json_log, ensure_ascii=False, indent=4))

    print("log saved")
