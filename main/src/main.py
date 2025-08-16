# app.py
import json
import time


def 读取json文件(file_path):
    with open(file_path, "r", encoding="utf-8") as f:
        config = json.load(f)
        print("读取配置:", config)


def loop():
    while True:
        print("Looping...")
        time.sleep(1)


def main():
    print("Hello from Docker Python App!")
    print("Current time:", time.strftime("%Y-%m-%d %H:%M:%S"))


if __name__ == "__main__":
    main()
    读取json文件("/app/config/config.json")
    # loop()
