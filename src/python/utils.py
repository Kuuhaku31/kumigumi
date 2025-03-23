# 请求html页面

import csv  # noqa: E402
import json  # noqa: E402
import urllib.request  # noqa: E402


# 获取html页面
def request_html(url: str) -> str:
    print(f"正在请求 {url}")

    res = urllib.request.urlopen(url)
    if res.status != 200:
        print(f"请求失败：{url}")
        return None
    else:
        print(f"请求成功：{url}")
        return res.read().decode("utf-8")


# 保存csv文件
def save_csv(file: str, headers: list, data: dict):
    with open(file, "w+", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(headers)  # 写入表头
        for info in data:
            new_row = []
            for header in headers:
                elem = info.get(header)
                new_row.append(elem)
            writer.writerow(new_row)  # 写入数据


# 获取json文件
def get_json(file: str) -> dict:
    with open(file, encoding="utf-8") as f:
        return json.load(f)
