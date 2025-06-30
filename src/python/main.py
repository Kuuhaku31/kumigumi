# main.py


import warnings

import headers
from database import 更新数据库
from excel_reader import ExcelReader
from fetch import 批量下载种子, 批量获取番组及单集数据, 批量获取种子数据
from utils import kumigumiPrint, safe_load


def 通过映射获取数据(bgm_url_rss_映射: dict[str, str]) -> tuple[list[list[str]], list[list[str]], list[list[str]]]:
    # 批量获取远程数据并更新数据库

    # 批量获取番组及单集数据
    anime_info_list, episode_info_list = 批量获取番组及单集数据(bgm_url_rss_映射.keys())
    torrent_info_list = 批量获取种子数据(bgm_url_rss_映射)

    # 翻译
    anime_info_list = [{headers.字段字典.get(k, k): v for k, v in row.items()} for row in anime_info_list]
    episode_info_list = [{headers.字段字典.get(k, k): v for k, v in row.items()} for row in episode_info_list]
    torrent_info_list = [{headers.字段字典.get(k, k): v for k, v in row.items()} for row in torrent_info_list]

    # 转换数据
    # 第一行是表头
    anime_data = [[headers.番组表头_主键_en] + headers.番组表头_自动更新_en.copy()]  # 表头
    episode_data = [[headers.单集表头_主键_en] + headers.单集表头_自动更新_en.copy()]  # 表头
    torrent_data = [[headers.种子表头_主键_en] + headers.种子表头_自动更新_en.copy()]  # 表头

    # 剩下的行是数据
    anime_data = anime_data + [
        [anime_info[headers.番组表头_主键_en]] + [anime_info.get(header, "") for header in headers.番组表头_自动更新_en]
        for anime_info in anime_info_list
    ]
    episode_data = episode_data + [
        [episode_info[headers.单集表头_主键_en]]
        + [episode_info.get(header, "") for header in headers.单集表头_自动更新_en]
        for episode_info in episode_info_list
    ]
    torrent_data = torrent_data + [
        [torrent_info[headers.种子表头_主键_en]]
        + [torrent_info.get(header, "") for header in headers.种子表头_自动更新_en]
        for torrent_info in torrent_info_list
    ]

    return (anime_data, episode_data, torrent_data)


if __name__ == "__main__":

    kumigumiPrint("开始执行脚本...")

    warnings.filterwarnings("ignore", category=UserWarning)

    excel_path = "D:/OneDrive/kumigumi.xlsx"
    kumigumiPrint(f"📖 读取 Excel 文件: {excel_path}")

    excel_reader = ExcelReader(safe_load(excel_path))

    # 更新 Access 数据库
    for 数据库表名, 工作表名 in excel_reader.更新数据参数:
        data: list[list[str]] = excel_reader.获取工作表数据(工作表名)
        更新数据库(excel_reader.数据库地址, 数据库表名, data)

    # 获取工作表中的 bgm_url 和 rss_url 映射
    bgm_url_rss_映射: dict[str, str] = {}
    for sheet名 in excel_reader.获取数据参数:
        bgm_url_rss_映射.update(excel_reader.读取sheet获取bgm_url_rss_映射(sheet名))

    ani_data, ep_data, tor_data = 通过映射获取数据(bgm_url_rss_映射)
    # 更新 Access 数据库
    if len(ani_data) > 1:
        更新数据库(excel_reader.数据库地址, excel_reader.数据库anime表名, ani_data)
    if len(ep_data) > 1:
        更新数据库(excel_reader.数据库地址, excel_reader.数据库episode表名, ep_data)
    if len(tor_data) > 1:
        更新数据库(excel_reader.数据库地址, excel_reader.数据库torrent表名, tor_data)

    torrent_url_list = excel_reader.获取下载种子url列表()

    批量下载种子(excel_reader.种子下载地址, torrent_url_list)

    kumigumiPrint("所有操作完成")
