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
    with open("D:/repositories/kumigumi/src/python/config.json", "r", encoding="utf-8") as f:
        config = json.load(f)

    工作目录: Path = Path("")
    if "wd" in config:
        工作目录 = Path(config["wd"])

    return 工作目录


def 获取动画索引信息(id: str) -> dict:

    # 拼接 url
    url_v0: str = "https://api.bgm.tv/v0/subjects/" + id
    url_ba: str = "https://bangumi.tv/subject/" + id

    # 请求数据
    res: str = utils.request_html(url_v0)
    json_data = json.loads(res)

    return {
        bangumi.作品原名: json_data["name"],
        bangumi.作品中文名: json_data["name_cn"] if json_data["name_cn"] != "" else None,
        bangumi.作品bangumiURL: url_ba,
        bangumi.作品mikanRSS: None,
    }


def 更新动画信息(动画索引信息列表: list[dict]):
    print("开始更新bangumi动画信息...")

    动画信息CSV文件地址: Path = 工作目录 / "anime.csv"
    单集信息CSV文件地址: Path = 工作目录 / "episode.csv"

    动画信息列表 = []
    单集信息列表 = []

    # 使用线程池并行处理 URL 请求和解析
    with ThreadPoolExecutor() as 线程池:

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


def 更新种子信息(动画索引信息列表: list[dict]):
    种子信息列表: list[dict] = []

    # 使用线程池并行处理 URL 请求和解析
    with ThreadPoolExecutor() as 线程池:

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

        # 将 动画索引信息列表 根据 bangumi.作品bangumiURL 排序
        动画索引信息列表.sort(key=lambda x: x[bangumi.作品bangumiURL])

    # 获取参数列表
    启动参数列表 = sys.argv[1:]

    # 如果没有参数，则打印帮助文档
    if len(启动参数列表) == 0:
        # sys.exit(1)
        pass

    # 设置配置变量
    elif 启动参数列表[0] == "-config":
        配置变量(启动参数列表[1:])

    # 添加动画信息
    elif 启动参数列表[0] == "-list-add":

        是添加文件: bool = 启动参数列表[1].endswith(".txt")  # 检查是否是文件

        if 是添加文件:

            print("添加所有动画信息...")
            # 添加所有动画信息
            动画id列表: list[str] = []
            with open(启动参数列表[1], "r", encoding="utf-8") as file:
                动画id列表 = [id.strip() for id in file.readlines() if id.strip()]

            # 使用线程池并行处理 URL 请求和解析
            动画索引信息列表_new: list[dict] = []
            with ThreadPoolExecutor() as 线程池:
                futures = [线程池.submit(获取动画索引信息, id) for id in 动画id列表]

                for future in tqdm(as_completed(futures), total=len(futures), desc="处理进度"):
                    动画索引信息列表_new.append(future.result())

            # 将新动画索引信息列表根据 bangumi.作品bangumiURL 排序
            动画索引信息列表_new.sort(key=lambda x: x[bangumi.作品bangumiURL])

            # 对比新旧动画索引信息列表
            # 如果新动画索引信息列表中的 bangumi.作品bangumiURL 不在旧动画索引信息列表中，则添加到旧动画索引信息列表中
            # 如果新动画索引信息列表中的 bangumi.作品bangumiURL 在旧动画索引信息列表中，则更新旧动画索引信息列表中的对应项
            i: int = 0
            j: int = 0
            while i < len(动画索引信息列表_new) and j < len(动画索引信息列表):
                if 动画索引信息列表_new[i][bangumi.作品bangumiURL] < 动画索引信息列表[j][bangumi.作品bangumiURL]:
                    动画索引信息列表.append(动画索引信息列表_new[i])
                    i += 1
                elif 动画索引信息列表_new[i][bangumi.作品bangumiURL] > 动画索引信息列表[j][bangumi.作品bangumiURL]:
                    j += 1
                else:
                    动画索引信息列表[j][bangumi.作品原名] = 动画索引信息列表_new[i][bangumi.作品原名]
                    动画索引信息列表[j][bangumi.作品中文名] = 动画索引信息列表_new[i][bangumi.作品中文名]
                    i += 1
                    j += 1

        else:
            print("添加单个动画信息...")

            # 添加单个动画信息
            动画索引信息: dict = 获取动画索引信息(启动参数列表[1])

            found = False
            for i in range(len(动画索引信息列表)):
                if 动画索引信息列表[i][bangumi.作品bangumiURL] == 动画索引信息[bangumi.作品bangumiURL]:
                    print(f"动画 {动画索引信息[bangumi.作品原名]} 已存在，更新信息...")
                    动画索引信息列表[i][bangumi.作品原名] = 动画索引信息[bangumi.作品原名]
                    动画索引信息列表[i][bangumi.作品中文名] = 动画索引信息[bangumi.作品中文名]
                    found = True
                    break

            if not found:
                print(f"添加动画 {动画索引信息[bangumi.作品原名]}...")
                动画索引信息列表.append(动画索引信息)

        动画索引信息列表.sort(key=lambda x: x[bangumi.作品bangumiURL])  # 再次排序

        # 保存配置文件
        with open(kumigumi_json_path, "w", encoding="utf-8") as file:
            kumigumi_json["动画信息列表"] = 动画索引信息列表
            json.dump(kumigumi_json, file, ensure_ascii=False, indent=4)

    # 更新动画信息
    elif 启动参数列表[0] == "-update-anime-info":
        更新动画信息(动画索引信息列表)

    # 更新种子信息
    elif 启动参数列表[0] == "-update-torrent-info":
        更新种子信息(动画索引信息列表)

    print("程序结束")
