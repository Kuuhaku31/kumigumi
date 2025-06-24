# main.py

import json
import sys
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

from tqdm import tqdm  # 引入 tqdm 进度条库

import bangumi
import headers
import mikananime.mikananime as mikananime
import utils as utils


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
        if item[bangumi.作品bangumiURL] == url_ba:
            item[bangumi.作品原名] = 名称
            item[bangumi.作品中文名] = 中文名

            return 动画信息列表

    # 不存在则添加
    动画信息列表.append(
        {
            bangumi.作品原名: 名称,
            bangumi.作品中文名: 中文名,
            bangumi.作品bangumiURL: url_ba,
            bangumi.作品mikanRSS: None,
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
    动画索引信息列表: list[dict] = []

    # 读取配置文件
    with open(kumigumi_json_path, "r", encoding="utf-8") as file:
        kumigumi_json = json.load(file)
        动画索引信息列表 = kumigumi_json["动画信息列表"]

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
            动画id列表: list[str] = []
            with open(启动参数列表[1], "r", encoding="utf-8") as file:
                动画id列表 = [id.strip() for id in file.readlines() if id.strip()]

            len_id = len(动画id列表)
            for i in range(len_id):
                动画索引信息列表 = 更新动画信息列表(动画索引信息列表, 动画id列表[i])
                print(f"已添加: {i + 1} / {len_id}")

        else:
            动画索引信息列表 = 更新动画信息列表(动画索引信息列表, 启动参数列表[1])

        # 保存配置文件
        with open(kumigumi_json_path, "w", encoding="utf-8") as file:
            kumigumi_json["动画信息列表"] = 动画索引信息列表
            json.dump(kumigumi_json, file, ensure_ascii=False, indent=4)

    # 更新动画信息
    elif 启动参数列表[0] == "-update-anime-info":
        print("开始更新bangumi动画信息...")

        动画信息CSV文件地址: Path = 工作目录 / "anime.csv"
        单集信息CSV文件地址: Path = 工作目录 / "episode.csv"

        动画信息列表 = []
        单集信息列表 = []

        # 使用线程池并行处理 URL 请求和解析
        with ThreadPoolExecutor(max_workers=5) as 线程池:  # 设置线程数为 5

            # 定义请求 HTML 并解析的函数
            def 请求HTML并解析(动画信息索引: dict):
                html_str = utils.request_html(动画信息索引[bangumi.作品bangumiURL])
                info: dict = bangumi.解析HTML(html_str)
                info["动画信息"][0][bangumi.作品mikanRSS] = 动画信息索引[bangumi.作品mikanRSS]
                return info

            # 提交任务到线程池
            futures = [线程池.submit(请求HTML并解析, 动画信息索引) for 动画信息索引 in 动画索引信息列表]

            # 使用 tqdm 显示进度条
            for future in tqdm(as_completed(futures), total=len(futures), desc="处理进度"):
                info = future.result()  # 获取任务结果
                动画信息列表 += info["动画信息"]
                单集信息列表 += info["单集信息"]

        # 保存到 CSV 文件
        utils.保存CSV文件(动画信息CSV文件地址, bangumi.anime_headers, 动画信息列表)
        utils.保存CSV文件(单集信息CSV文件地址, bangumi.episode_headers, 单集信息列表)

        print("更新完成")

    # 更新种子信息
    elif 启动参数列表[0] == "-update-torrent-info":

        种子信息列表: list[dict] = []

        # 使用线程池并行处理 URL 请求和解析
        with ThreadPoolExecutor(max_workers=5) as 线程池:  # 设置线程数，例如 5

            def 请求HTML并解析(动画索引信息: dict) -> list[dict]:

                mikan_rss: str = 动画索引信息.get(bangumi.作品mikanRSS)
                if mikan_rss is None:
                    return []

                rss_html_str = utils.request_html(mikan_rss)
                info: list[dict] = mikananime.解析mikanRSS_XML(动画索引信息[bangumi.作品bangumiURL], rss_html_str)
                return info

            futures = [线程池.submit(请求HTML并解析, 动画索引信息) for 动画索引信息 in 动画索引信息列表]

            # 使用 tqdm 显示进度条
            for future in tqdm(as_completed(futures), total=len(futures), desc="处理进度"):
                info = future.result()  # 获取任务结果
                种子信息列表 += info

        # 保存到 CSV 文件
        utils.保存CSV文件(工作目录 / "torrent.csv", headers.种子信息表头, 种子信息列表)

        print("更新完成")

    print("程序结束")
