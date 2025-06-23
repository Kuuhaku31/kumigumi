# main.py

import json
import sys
from pathlib import Path

import bangumi
import mikananime.update as mk_update
import utils.utils as utils


def 更新种子信息(工作目录: str):
    print("开始更新种子信息...")

    动画信息列表 = []
    种子数据文件名 = ""
    with open(工作目录 + "kumigumi.json", "r", encoding="utf-8") as f:
        kumigumi_json = json.load(f)
        动画信息列表 = kumigumi_json["动画信息列表"]
        种子数据文件名 = kumigumi_json["配置信息"]["种子数据文件名"]

    # 获取种子信息列表
    MikanAnimate任务列表 = []
    for 动画信息 in 动画信息列表:
        if "蜜柑计划RSS源" not in 动画信息:
            continue
        elif 动画信息["蜜柑计划RSS源"] == "":
            continue

        # 添加到任务列表
        MikanAnimate任务列表.append(
            {
                "动画名称": 动画信息["名称"],
                "蜜柑计划RSS源": 动画信息["蜜柑计划RSS源"],
            }
        )

    mk_update.update_csv(MikanAnimate任务列表, 工作目录 + 种子数据文件名)

    print("更新完成")


def 配置变量(配置参数列表: list[str]):

    print("设置配置变量")

    # 读取配置文件
    with open("config.json", "r", encoding="utf-8") as f:
        config = json.load(f)

    for i in range(0, len(配置参数列表)):
        if 配置参数列表[i].startswith("--"):
            key_value = 配置参数列表[i][2:].split("=")
            if len(key_value) == 2:
                config[key_value[0]] = key_value[1]
            else:
                print(f"无效的配置变量: {配置参数列表[i]}")
        else:
            print(f"无效的参数: {配置参数列表[i]}")

    # 保存配置文件
    with open("config.json", "w", encoding="utf-8") as f:
        json.dump(config, f, ensure_ascii=False, indent=4)


def 读取工作目录() -> Path:
    # 读取配置文件
    with open("config.json", "r", encoding="utf-8") as f:
        config = json.load(f)

    工作目录: Path = Path("")
    if "wd" in config:
        工作目录 = Path(config["wd"])

    return 工作目录


def 更新动画信息列表(动画信息列表: list[dict], id: str) -> list[dict]:

    # 拼接 url
    url_v0: str = "https://api.bgm.tv/v0/subjects/" + id
    url_ba: str = "https://bangumi.tv/subject/" + id

    # 请求数据
    res: str = utils.request_html(url_v0)
    json_data = json.loads(res)

    名称 = json_data["name"]
    中文名 = json_data["name_cn"] if json_data["name_cn"] != "" else None

    # 确认 url 是否存在
    for item in 动画信息列表:
        if item["bangumi源"] == url_ba:
            item["名称"] = 名称
            item["中文名"] = 中文名

            return 动画信息列表

    动画信息列表.append(
        {
            "名称": 名称,
            "中文名": 中文名,
            "bangumi源": url_ba,
            "蜜柑计划RSS源": None,
        }
    )

    return 动画信息列表


if __name__ == "__main__":

    工作目录 = 读取工作目录()
    if 工作目录 == "":
        print("请先设置工作目录")
        sys.exit(1)
    print(f"工作目录: {工作目录}")

    kumigumi_json_path: Path = 工作目录 / "kumigumi.json"
    kumigumi_json: dict = {}
    动画信息列表: list[dict] = []

    # 读取配置文件
    with open(kumigumi_json_path, "r", encoding="utf-8") as file:
        kumigumi_json = json.load(file)
        动画信息列表 = kumigumi_json["动画信息列表"]

    # 获取参数列表
    启动参数列表 = sys.argv[1:]

    # 如果没有参数，则打印帮助文档
    if len(启动参数列表) == 0:
        sys.exit(1)

    # 设置配置变量
    elif 启动参数列表[0] == "-config":
        配置变量(启动参数列表[1:])

    # 添加动画信息
    elif 启动参数列表[0].startswith("-list-add"):

        参数列表: list[str] = 启动参数列表[0].replace("-list-add=", "").split("-")

        是添加文件: bool = False
        for 参数 in 参数列表:
            if 参数 == "all":
                是添加文件 = True
                break

        if 是添加文件:
            # 添加所有动画信息
            动画id列表 = []
            with open(启动参数列表[1], "r", encoding="utf-8") as file:
                动画id列表 = [id.strip() for id in file.readlines() if id.strip()]

            for id in 动画id列表:
                动画信息列表 = 更新动画信息列表(动画信息列表, id)

        else:
            动画信息列表 = 更新动画信息列表(动画信息列表, 启动参数列表[1])

        # 保存配置文件
        with open(kumigumi_json_path, "w", encoding="utf-8") as file:
            kumigumi_json["动画信息列表"] = 动画信息列表
            json.dump(kumigumi_json, file, ensure_ascii=False, indent=4)

    # 更新动画信息
    elif 启动参数列表[0] == "-update-anime-info":
        print("开始更新bangumi动画信息...")

        动画数据文件地址: Path = 工作目录 / "anime.csv"
        单集数据文件地址: Path = 工作目录 / "episodes.csv"

        动画URL列表 = []
        for 动画信息 in 动画信息列表:
            动画URL列表.append(动画信息["bangumi源"])

        bangumi.更新CSV文件(动画URL列表, 动画数据文件地址, 单集数据文件地址)

        print("更新完成")

    print("程序结束")
