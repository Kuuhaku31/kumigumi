# app.py
# import json
# import time

import database
from flask import Flask, jsonify, request

app = Flask(__name__)


@app.route("/predict", methods=["POST"])
def predict():
    data = request.json
    obj = {"value": data["value"] * 2, "list": [1, 2, 3]}

    database_data = database.获取数据("../../accdb/kumigumi.accdb", "anime")

    return jsonify({"msg": f"预测结果: {obj}", "database": database_data})


app.run(host="0.0.0.0", port=5000)


# def 读取json文件(file_path):
#     with open(file_path, "r", encoding="utf-8") as f:
#         config = json.load(f)
#         print("读取配置:", config)


# def loop():
#     while True:
#         print("Looping...")
#         time.sleep(1)


# def main():
#     print("Hello from Docker Python App!")
#     print("Current time:", time.strftime("%Y-%m-%d %H:%M:%S"))


# if __name__ == "__main__":
#     main()
#     读取json文件("/app/config/config.json")
#     # loop()
