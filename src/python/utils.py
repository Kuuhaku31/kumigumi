# 请求html页面

import csv  # noqa: E402
import json  # noqa: E402
import os  # noqa: E402
import urllib.request  # noqa: E402
import winreg  # noqa: E402
from pathlib import Path

import fake_useragent


# 获取html页面
def request_html(url: str) -> str:

    # 随机请求头池
    headers = {
        "User-Agent": fake_useragent.UserAgent().random,
        "Connection": "keep-alive",
    }

    res = urllib.request.urlopen(urllib.request.Request(url, headers=headers))
    if res.status != 200:
        return None
    else:
        return res.read().decode("utf-8")


# 保存csv文件
def 保存CSV文件(文件地址: Path, 表头: list[str], 数据: list[dict]) -> None:
    with open(文件地址, "w+", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(表头)  # 写入表头

        # 遍历数据，写入每一行
        for info in 数据:
            new_row = []
            for header in 表头:
                elem = ""
                if header in info:
                    elem = info.get(header)
                new_row.append(elem)
            writer.writerow(new_row)  # 写入数据

    print(f"\033[92m[CSV文件保存成功]\033[0m: {文件地址}")


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
    length = len(动画id列表)
    for i in range(length):

        # 遍历每个index，拼接url
        json_str = request_html("https://api.bgm.tv/v0/subjects/" + 动画id列表[i].strip())
        json_data = json.loads(json_str)

        cn_name = name = json_data["name"]
        if json_data["name_cn"] != "":
            cn_name = json_data["name_cn"]

        动画信息列表.append(
            {
                "名称": name,
                "中文名": cn_name,
                "bangumi源": "https://bangumi.tv/subject/" + 动画id列表[i].strip(),
                "蜜柑计划RSS源": "",
            }
        )

        print(f"{i}/{length} 已添加动画信息：{动画信息列表[i]['名称']}")

    return 动画信息列表


def 获取用户默认下载路径():
    try:
        with winreg.OpenKey(
            winreg.HKEY_CURRENT_USER, r"SOFTWARE\Microsoft\Windows\CurrentVersion\Explorer\Shell Folders"
        ) as key:
            downloads, _ = winreg.QueryValueEx(key, "{374DE290-123F-4565-9164-39C4925E467B}")
            return downloads + os.sep
    except Exception:
        # 兼容性处理，回退到用户主目录下的 Downloads
        return os.path.join(os.path.expanduser("~"), "Downloads") + os.sep
