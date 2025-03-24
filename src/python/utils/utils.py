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
def build_config_file(index_list: list, config_file: str):

    config_data = {
        "动画信息": [],
        "配置信息": {
            "动画数据文件": "anime.csv",
            "单集数据文件": "episode.csv",
            "种子数据文件": "torrent.csv",
        },
    }

    # 遍历每个index，拼接url
    for index in index_list:

        json_str = request_html("https://api.bgm.tv/v0/subjects/" + index)
        json_data = json.loads(json_str)

        cn_name = name = json_data["name"]
        if json_data["name_cn"] != "":
            cn_name = json_data["name_cn"]

        config_data["动画信息"].append(
            {
                "链接": {"bangumi": "https://bangumi.tv/subject/" + index, "蜜柑计划": ""},
                "名称": name,
                "中文名": cn_name,
            }
        )

    # 保存到配置文件
    with open(config_file, "w+", encoding="utf-8") as f:
        json.dump(config_data, f, ensure_ascii=False)
