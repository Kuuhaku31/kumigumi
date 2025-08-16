# prase_torrent_info.py

import headers as 种子信息表头

# 解析整理种子信息


def ani(标题: str) -> dict:
    # [ANi]  BanG Dream! Ave Mujica - 01 [1080P][Baha][WEB-DL][AAC AVC][CHT][MP4]
    # [ANi]  BanG Dream! Ave Mujica - 09 [1080P][Baha][WEB-DL][AAC AVC][CHT][V2][MP4]

    其他标记 = ""

    信息列表 = 标题.replace("]", "[").split("[")

    i = 0
    while i < len(信息列表):
        信息列表[i] = 信息列表[i].strip()  # 清除首尾空字符串
        if len(信息列表[i]) == 2:  # 如果长度为2
            其他标记 += 信息列表[i] + ";"  # 添加到其他标记
            信息列表.pop(i)  # 移除当前元素
        else:
            i += 1  # 仅在未移除元素时递增索引

    信息列表 = [info for info in 信息列表 if info != ""]

    # 种子信息字典
    # 获取分割后的最后一个元素
    种子信息 = {
        种子信息表头.集数: int(信息列表[1].split(" ")[-1]),
        种子信息表头.分辨率: 信息列表[2],
        种子信息表头.片源: 信息列表[3],
        种子信息表头.片源类型: 信息列表[4],
        种子信息表头.视频编码格式: 信息列表[5].split(" ")[1],
        种子信息表头.音频编码格式: 信息列表[5].split(" ")[0],
        种子信息表头.字幕语言: 信息列表[6],
        种子信息表头.文件格式: 信息列表[7],
        种子信息表头.其他标记: 其他标记,
    }

    return 种子信息


def lol(标题: str) -> dict:
    # [LoliHouse] 结缘甘神神社 / Amagami-san Chi no Enmusubi - 05v2 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]
    # [LoliHouse] 我和班上最讨厌的女生结婚了。 / クラスの大嫌いな女子と结婚することになった。 / Kurakon [01-12 合集][WebRip 1080p HEVC-10bit AAC][简繁内封字幕][Fin]
    信息列表 = 标题.replace("]", "[").split("[")

    # 遍历列表
    for i in range(len(信息列表)):
        信息列表[i] = 信息列表[i].strip()
    信息列表 = [info for info in 信息列表 if info != ""]

    集数 = -1
    详细信息列表 = []
    字幕语言 = ""  # 简繁内封字幕
    其他标记 = ""

    # 种子信息字典
    # 如果最后一个元素可以转换为数字
    标题结尾 = 信息列表[1].split(" ")[-1]
    if 标题结尾.isdigit():
        集数 = int(标题结尾)
        详细信息列表 = 信息列表[2].split(" ")
        字幕语言 = 信息列表[3]
        if len(信息列表) > 4:
            其他标记 += 信息列表[4] + ";"

    elif "v" in 标题结尾:
        集数 = int(标题结尾.split("v")[0])
        其他标记 += "v" + 标题结尾.split("v")[1] + ";"
        详细信息列表 = 信息列表[2].split(" ")
        字幕语言 = 信息列表[3]
        if len(信息列表) > 4:
            其他标记 += 信息列表[4] + ";"

    else:  # [01-12 合集]
        集数 = -1
        详细信息列表 = 信息列表[3].split(" ")
        字幕语言 = 信息列表[4] if len(信息列表) > 4 else ""
        if len(信息列表) > 5:
            其他标记 += 信息列表[5] + ";"

    种子信息 = {
        种子信息表头.集数: 集数,
        种子信息表头.分辨率: 详细信息列表[1] if len(详细信息列表) > 1 else "",
        种子信息表头.片源类型: 详细信息列表[0] if len(详细信息列表) > 0 else "",
        种子信息表头.视频编码格式: 详细信息列表[2] if len(详细信息列表) > 2 else "",
        种子信息表头.音频编码格式: 详细信息列表[3] if len(详细信息列表) > 3 else "",
        种子信息表头.字幕语言: 字幕语言,
        种子信息表头.其他标记: 其他标记,
    }

    return 种子信息


def upto21(标题: str) -> dict:

    # [Up to 21°C] 超超超超超喜欢你的 100 个女朋友 / Kimi no Koto ga Daidaidaidaidaisuki na 100-nin no Kanojo 2nd Season - 15 (CR 1920x1080 AVC AAC MKV)

    # 获取最后一对括号内的内容
    # CR 1920x1080 AVC AAC MKV
    详细信息 = 标题.split("(")[-1].split(")")[0].split(" ")

    集数 = 标题.split(" - ")[-1].split(" ")[0]

    种子信息 = {
        种子信息表头.集数: 集数,
        种子信息表头.片源: 详细信息[0],
        种子信息表头.分辨率: 详细信息[1],
        种子信息表头.视频编码格式: 详细信息[2],
        种子信息表头.音频编码格式: 详细信息[3],
        种子信息表头.文件格式: 详细信息[4],
    }

    return 种子信息


def 北宇治(标题: str) -> dict:
    # [北宇治字幕组] BanG Dream! Ave Mujica / 颂乐人偶 [01v2][WebRip][HEVC_AAC][简繁日内封]
    信息列表 = 标题.replace("]", "[").split("[")
    for i in range(len(信息列表)):
        信息列表[i] = 信息列表[i].strip()
    信息列表 = [info for info in 信息列表 if info != ""]

    集数 = -1
    其他标记 = ""

    集数单元 = 信息列表[2]
    if "v" in 集数单元:
        其他标记 = "v" + 集数单元.split("v")[1]
        集数 = int(集数单元.split("v")[0]) if 集数单元.split("v")[0].isdigit() else -1
    else:
        集数 = int(集数单元) if 集数单元.isdigit() else -1

    种子信息 = {
        种子信息表头.集数: 集数,
        种子信息表头.片源类型: 信息列表[3],
        种子信息表头.视频编码格式: 信息列表[4].split("_")[0] if len(信息列表[4].split("_")) > 0 else "",
        种子信息表头.音频编码格式: 信息列表[4].split("_")[1] if len(信息列表[4].split("_")) > 1 else "",
        种子信息表头.字幕语言: 信息列表[5],
        种子信息表头.其他标记: 其他标记,
    }

    return 种子信息


def 喵萌奶茶屋(标题: str) -> dict:
    # 【喵萌奶茶屋】★10月新番★[冻牌 / Touhai: Ura Rate Mahjong Touhai Roku][22][1080p][简体][招募翻译]
    信息列表 = 标题.replace("]", "[").split("[")
    for i in range(len(信息列表)):
        信息列表[i] = 信息列表[i].strip()
    信息列表 = [info for info in 信息列表 if info != ""]

    种子信息 = {
        种子信息表头.集数: int(信息列表[2]) if 信息列表[2].isdigit() else -1,
        种子信息表头.分辨率: 信息列表[3],
        种子信息表头.字幕语言: 信息列表[4],
        种子信息表头.其他标记: 信息列表[5] if len(信息列表) > 5 else "",
    }

    return 种子信息


解析方法字典 = {
    "ANi": ani,
    "喵萌奶茶屋&LoliHouse": lol,
    "LoliHouse": lol,
    "Up to 21°C": upto21,
    "北宇治字幕组": 北宇治,
    "喵萌奶茶屋": 喵萌奶茶屋,
    "喵萌Production": 喵萌奶茶屋,
    "喵萌Production&LoliHouse": lol,
    "北宇治字幕组&LoliHouse": lol,
}
