# main.py

import json

import bangumi.update as ba_update
import mikananime.prase_rss_html as mikan
import utils.utils as utils


def 构建配置文件(工作目录: str):
    print("开始构建配置文件...")

    动画ID列表文件名 = ""
    kumigumi_json = {}
    with open(工作目录 + "kumigumi.json", "r", encoding="utf-8") as f:
        kumigumi_json = json.load(f)
        动画ID列表文件名 = kumigumi_json["配置信息"]["动画ID列表文件名"]

    动画id列表 = []  # 获取动画ID列表
    with open(工作目录 + 动画ID列表文件名, "r", encoding="utf-8") as f:
        动画id列表 = f.readlines()

    for i in range(len(动画id列表)):
        动画id列表[i] = 动画id列表[i].strip()

    动画信息列表 = utils.build_config_file(动画id列表)
    kumigumi_json["动画信息列表"] = 动画信息列表

    # 保存配置文件
    with open(工作目录 + "kumigumi.json", "w", encoding="utf-8") as f:
        json.dump(kumigumi_json, f, ensure_ascii=False, indent=4)

    print("配置文件构建完成")


def 更新配置文件(工作目录: str):
    print("开始更新配置文件...")

    动画信息列表 = []
    with open(工作目录 + "kumigumi.json", "r", encoding="utf-8") as f:
        kumigumi_json = json.load(f)
        动画信息列表 = kumigumi_json["动画信息列表"]

    for 动画信息 in 动画信息列表:
        id = 动画信息["bangumi源"].split("/")[-1]
        url = "https://api.bgm.tv/v0/subjects/" + id
        json_str = utils.request_html(url)
        json_data = json.loads(json_str)

        动画信息["名称"] = json_data["name"]
        动画信息["中文名"] = json_data["name_cn"]

    # 保存配置文件
    with open(工作目录 + "kumigumi.json", "w", encoding="utf-8") as f:
        json.dump(kumigumi_json, f, ensure_ascii=False, indent=4)


def 更新动画信息(工作目录: str):
    print("开始更新bangumi动画信息...")

    动画信息列表 = []
    动画数据文件名 = ""
    单集数据文件名 = ""
    with open(工作目录 + "kumigumi.json", "r", encoding="utf-8") as f:
        kumigumi_json = json.load(f)
        动画信息列表 = kumigumi_json["动画信息列表"]
        动画数据文件名 = kumigumi_json["配置信息"]["动画数据文件名"]
        单集数据文件名 = kumigumi_json["配置信息"]["单集数据文件名"]

    动画URL列表 = []
    for 动画信息 in 动画信息列表:
        动画URL列表.append(动画信息["bangumi源"])

    ba_update.update_csv(动画URL列表, 工作目录 + 动画数据文件名, 工作目录 + 单集数据文件名)

    print("更新完成")


def 更新种子信息(工作目录: str):
    print("开始更新种子信息...")

    动画信息列表 = []
    种子数据文件名 = ""
    with open(工作目录 + "kumigumi.json", "r", encoding="utf-8") as f:
        kumigumi_json = json.load(f)
        动画信息列表 = kumigumi_json["动画信息列表"]
        种子数据文件名 = kumigumi_json["配置信息"]["种子数据文件名"]

    # 获取种子信息列表
    种子信息列表 = []
    for 动画信息 in 动画信息列表:
        if "蜜柑计划RSS源" not in 动画信息:
            continue
        rss_html_str = utils.request_html(动画信息["蜜柑计划RSS源"])
        种子信息列表 += mikan.prase(动画信息["名称"], rss_html_str)

    # 保存种子信息
    utils.save_csv(
        工作目录 + 种子数据文件名, ["名称", "字幕组", "标题", "大小", "大小（Byte）", "发布日期", "链接"], 种子信息列表
    )

    print("更新完成")


def main():

    # 读取配置文件
    配置信息 = {}
    with open("./config.json", "r", encoding="utf-8") as f:
        配置信息 = json.load(f)
    print("配置文件读取成功")

    if 配置信息["启动参数"] == "构建配置文件":
        构建配置文件(配置信息["工作目录"])
    elif 配置信息["启动参数"] == "更新配置文件":
        更新配置文件(配置信息["工作目录"])
    elif 配置信息["启动参数"] == "更新动画信息":
        更新动画信息(配置信息["工作目录"])
    elif 配置信息["启动参数"] == "更新种子信息":
        更新种子信息(配置信息["工作目录"])
    else:
        print("未知的启动参数")


main()
