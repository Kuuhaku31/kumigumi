# main.py

import argparse
import json

import bangumi.update as ba_update
import mikananime.update as mk_update
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
        动画信息["蜜柑计划RSS源"] = 动画信息["蜜柑计划RSS源"] if "蜜柑计划RSS源" in 动画信息 else ""

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


def 打印帮助文档():
    文档 = """
    显示帮助文档            h
    构建配置文件            bc
    构建配置文件            bc
    更新配置文件            uc
    更新动画信息            ua
    更新种子信息            ut
    更新动画信息和种子信息  u
    """
    print(文档)


if __name__ == "__main__":

    # 创建解析器
    parser = argparse.ArgumentParser(description="一个可以接受启动参数的 Python 程序")

    # 添加参数
    parser.add_argument("--wd", type=str, help="工作目录")
    parser.add_argument("--ac", type=str, help="行为")

    # 解析参数
    args = parser.parse_args()
    工作目录: str = args.wd
    行为: str = args.ac

    print("工作目录: " + 工作目录)

    if not 行为:
        打印帮助文档()
        行为 = input("请输入操作参数: ")

    if 行为 == "bc":
        print("确认后将会覆盖原有配置文件【y/n】")
        if input() == "y" or "Y":
            构建配置文件(工作目录)
        else:
            print("取消构建配置文件")
    elif 行为 == "uc":
        更新配置文件(工作目录)
    elif 行为 == "ua":
        更新动画信息(工作目录)
    elif 行为 == "ut":
        更新种子信息(工作目录)
    elif 行为 == "u":
        更新动画信息(工作目录)
        更新种子信息(工作目录)
    else:
        print("未知的启动参数")

    print("程序结束")
