# init.py

import json

with open("./configure.json", encoding="utf-8") as f:
    config = json.load(f)["log"]
    log_path = config["log_path"]
