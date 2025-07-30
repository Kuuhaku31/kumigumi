# main.py


import warnings

import headers
from database import 更新数据库
from excel_reader import ExcelReader
from fetch import 批量下载种子, 批量获取番组及单集数据, 批量获取种子数据
from utils import kumigumiPrint, safe_load, 合并数据


def 通过映射获取数据(bgm_url_rss_映射: dict[str, str]) -> tuple[list[list[str]], list[list[str]], list[list[str]]]:

    kumigumiPrint("🔄 开始批量获取番组及单集数据...")

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

    kumigumiPrint("🔄 批量获取番组及单集数据完成")
    return (anime_data, episode_data, torrent_data)


if __name__ == "__main__":

    kumigumiPrint("开始执行脚本...")

    warnings.filterwarnings("ignore", category=UserWarning)

    excel_path = "C:/Users/admin-kh/OneDrive/kumigumi.xlsx"
    kumigumiPrint(f"📖 读取 Excel 文件: {excel_path}")

    excel_reader = ExcelReader(safe_load(excel_path))

    # 获取更新数据
    update_data: dict[str, list[list[list[str]]]] = {}  # 数据库表名 : [ 工作表数据1, 工作表数据2, ... ]
    for 数据库表名, 工作表名 in excel_reader.更新数据参数:
        data: list[list[str]] = excel_reader.获取工作表数据(工作表名)
        update_data.setdefault(数据库表名, []).append(data)

    # 合并工作表数据
    合并后的数据字典: dict[str, list[list[str]]] = {}
    for 数据库表名, 工作表数据列表 in update_data.items():
        for 工作表数据 in 工作表数据列表:
            合并后的数据字典.setdefault(数据库表名, [[]])
            合并后的数据字典[数据库表名] = 合并数据(合并后的数据字典[数据库表名], 工作表数据)

    # 更新数据库
    for 数据库表名, 工作表数据 in 合并后的数据字典.items():
        kumigumiPrint(f"更新数据库: {excel_reader.数据库地址} - {数据库表名}")
        更新数据库(excel_reader.数据库地址, 数据库表名, 工作表数据)

    # 获取工作表中的 bgm_url 和 rss_url 映射
    bgm_url_rss_映射: dict[str, str] = {}
    for 工作表名 in excel_reader.获取数据参数:
        bgm_url_rss_映射.update(excel_reader.读取sheet获取bgm_url_rss_映射(工作表名))

    # 批量获取番组及单集数据
    ani_data, ep_data, tor_data = 通过映射获取数据(bgm_url_rss_映射)
    if len(ani_data) > 1:
        更新数据库(excel_reader.数据库地址, excel_reader.数据库anime表名, ani_data)
    if len(ep_data) > 1:
        更新数据库(excel_reader.数据库地址, excel_reader.数据库episode表名, ep_data)
    if len(tor_data) > 1:
        更新数据库(excel_reader.数据库地址, excel_reader.数据库torrent表名, tor_data)

    # 获取需要下载的种子链接
    torrent_url_list = excel_reader.获取下载种子url列表()

    # 如果有需要下载的种子链接，则批量下载
    if len(torrent_url_list) > 0:
        kumigumiPrint(f"🔄 开始批量下载种子: {len(torrent_url_list)} 个链接")
        批量下载种子(excel_reader.种子下载地址, torrent_url_list)

    kumigumiPrint("所有操作完成")
