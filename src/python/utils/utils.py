# 请求html页面

import csv  # noqa: E402
import json  # noqa: E402
import urllib.request  # noqa: E402

import fake_useragent


# 获取html页面
def request_html(url: str) -> str:
    print(f"正在请求: {url}")

    # 随机请求头池
    headers = {
        "User-Agent": fake_useragent.UserAgent().random,
        "Connection": "keep-alive",
    }

    res = urllib.request.urlopen(urllib.request.Request(url, headers=headers))
    if res.status != 200:
        print(f"请求失败：{url}, 状态码：{res.status}")
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

    print(f"保存成功：{file}")


# 读取csv文件
def load_csv(file_path):
    data = []
    with open(file_path, mode="r", encoding="utf-8") as file:
        reader = csv.DictReader(file)  # 使用 DictReader 读取为字典形式
        for row in reader:
            data.append(row)  # 每一行是一个字典

    print(f"读取成功：{file_path}")
    return data


# 获取json文件
def get_json(file: str) -> dict:
    data = None
    with open(file, encoding="utf-8") as f:
        data = json.load(f)

    print(f"读取成功：{file}")
    return data


# 补全配置文件
def build_config_file(动画id列表: list) -> list:

    # 获取动画信息列表
    动画信息列表 = []
    for id in 动画id列表:

        # 遍历每个index，拼接url
        json_str = request_html("https://api.bgm.tv/v0/subjects/" + id)
        json_data = json.loads(json_str)

        cn_name = name = json_data["name"]
        if json_data["name_cn"] != "":
            cn_name = json_data["name_cn"]

        动画信息列表.append(
            {"名称": name, "中文名": cn_name, "bangumi源": "https://bangumi.tv/subject/" + id, "蜜柑计划RSS源": ""}
        )

    return 动画信息列表
