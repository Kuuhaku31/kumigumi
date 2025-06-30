# main.py


import warnings

import headers
from database import 更新数据库
from fetch import 批量下载种子, 批量获取番组及单集数据, 批量获取种子数据
from openpyxl import Workbook, load_workbook
from utils import kumigumiPrint, safe_load


class 解析结果:
    def __init__(己):
        己.数据库地址: str = ""
        己.A: list[tuple[str, str]] = []
        己.B: list[tuple[str, str, str, str]] = []
        己.C: list[tuple[str, str, str]] = []

    # [(数据库表名, 工作表名), ...]
    # [(数据库anime表名, 数据库episode表名, 数据库torrent表名, 工作表名), ...]
    # [(种子下载地址, 工作表名, 种子状态), ...]


def 解析指令(work_book: Workbook) -> 解析结果:

    main_sheet = work_book["main"]

    解析结果实例: 解析结果 = 解析结果()

    # 解析 main 工作表
    行指针: int = 1
    列指针: int = 1
    while True:
        指令 = main_sheet.cell(行指针, 列指针).value

        if 指令 == "_end":
            break
        elif 指令 == "_to":  # 跳到指定行
            行指针_to = int(main_sheet.cell(行指针, 列指针 + 1).value)
            列指针_to = int(main_sheet.cell(行指针, 列指针 + 2).value)
            行指针 = 行指针_to
            列指针 = 列指针_to
            continue
        elif 指令 is None:
            pass

        elif 指令 == "_database_path":
            解析结果实例.数据库地址 = main_sheet.cell(行指针, 列指针 + 1).value

        elif 指令 == "_store":
            数据库表名 = main_sheet.cell(行指针, 列指针 + 1).value
            工作表名 = main_sheet.cell(行指针, 列指针 + 2).value
            解析结果实例.A.append((数据库表名, 工作表名))

        elif 指令 == "_fetch":
            数据库anime表名 = main_sheet.cell(行指针, 列指针 + 1).value
            数据库episode表名 = main_sheet.cell(行指针, 列指针 + 2).value
            数据库torrent表名 = main_sheet.cell(行指针, 列指针 + 3).value
            工作表名 = main_sheet.cell(行指针, 列指针 + 4).value
            解析结果实例.B.append((数据库anime表名, 数据库episode表名, 数据库torrent表名, 工作表名))

        elif 指令 == "_dt":
            下载地址 = main_sheet.cell(行指针, 列指针 + 1).value
            工作表名 = main_sheet.cell(行指针, 列指针 + 2).value
            种子下载状态 = main_sheet.cell(行指针, 列指针 + 3).value
            解析结果实例.C.append((下载地址, 工作表名, 种子下载状态))

        行指针 += 1

    return 解析结果实例


def 获取工作表数据(work_book: Workbook, 工作表名: str) -> list[list[str]]:
    """
    获取指定工作表的数据
    返回一个二维字符串列表
    第一个元素是表头
    表头的第一个字符串是主键
    其他字符串是字段名
    """

    kumigumiPrint(f"🔄 读取 {工作表名} ...")

    工作表 = work_book[工作表名]

    起始行: int = 0
    结束行: int = 0
    主键: str = ""
    字段字典: dict[str, int] = {}  # 字段名 : 列号

    指令: str = ""
    行指针: int = 1
    while True:
        键: str = 工作表.cell(row=行指针, column=1).value
        值: str = 工作表.cell(row=行指针, column=2).value

        if 键 is None:
            pass
        elif 指令 == "_to":  # 跳到指定行
            行指针 = int(值)
            continue
        elif 键 == "_end":
            break
        elif 键 == "_start_row":
            起始行 = int(值)
        elif 键 == "_end_row":
            结束行 = int(值)
        elif 键 == "_primary_key":
            主键 = 值
        else:
            字段字典[键] = int(值)

        行指针 += 1

    if not 主键 or 主键 not in 字段字典:
        raise ValueError(f"❌ 工作表 {工作表名} 中未定义主键或主键 {主键} 不存在")

    # 读取数据区域
    data: list[list[str]] = []
    data_header: list[str] = [主键] + [字段名 for 字段名 in 字段字典.keys() if 字段名 != 主键]  # 设置表头，主键在第一个
    data.append(data_header)
    for 行号 in range(起始行, 结束行):
        # 读取每一行的数据
        # 直接从数据区域读取，跳过表头
        row_data: list[str] = []
        for 字段名 in data_header:
            单元格值 = 工作表.cell(row=行号, column=字段字典[字段名]).value
            row_data.append(单元格值 if 单元格值 is not None else "")
        data.append(row_data)

    kumigumiPrint("🔄 读取结束")
    return data


def 批量获取远程数据并更新数据库(work_book: Workbook, 数据库地址: str, B: list[tuple[str, str, str, str]]) -> None:
    # 批量获取远程数据并更新数据库
    for db_ani名, db_ep名, db_tor名, sheet名 in B:
        kumigumiPrint(f"🔄 批量获取 {sheet名} 的远程数据并更新数据库...")

        # 读取源工作表
        data = 获取工作表数据(work_book, sheet名)

        # 读取信息
        bgm_url_rss_映射: dict[str, str] = {}  # 番组链接 : RSS订阅链接
        for row in data.数据:
            bgm_url = row.get("番组bangumi链接", "")
            rss_url = row.get("番组RSS订阅链接", "")
            bgm_url_rss_映射[bgm_url] = rss_url

        anime_info_list, episode_info_list = 批量获取番组及单集数据(bgm_url_rss_映射.keys())
        torrent_info_list = 批量获取种子数据(bgm_url_rss_映射)

        # 翻译键名
        anime_info_list = [{headers.字段字典.get(k, k): v for k, v in row.items()} for row in anime_info_list]
        episode_info_list = [{headers.字段字典.get(k, k): v for k, v in row.items()} for row in episode_info_list]
        torrent_info_list = [{headers.字段字典.get(k, k): v for k, v in row.items()} for row in torrent_info_list]

        kumigumiPrint("获取完毕")

        # 同步动画信息到 Access
        更新数据库(
            anime_info_list,
            headers.番组表头_主键_en,
            headers.番组表头_自动更新_en,
            数据库地址,
            db_ani名,
        )
        更新数据库(
            episode_info_list,
            headers.单集表头_主键_en,
            headers.单集表头_自动更新_en,
            数据库地址,
            db_ep名,
        )
        更新数据库(
            torrent_info_list,
            headers.种子表头_主键_en,
            headers.种子表头_自动更新_en,
            数据库地址,
            db_tor名,
        )


def 下载种子(work_book: Workbook, 数据库地址: str, C: list[tuple[str, str, str]]) -> None:

    for 种子下载地址, 工作表名, 种子状态 in C:

        # 下载种子链接
        if 工作表名 != "":
            kumigumiPrint(f"🔄 下载 {工作表名} 的种子链接...")

            # 获取种子下载链接工作表
            sheet_download_torrent = work_book[工作表名]

            起始行: int = 0
            结束行: int = 0
            种子下载链接_column: int = 0
            种子下载情况_column: int = 0

            # 读取种子下载链接
            行指针 = 1
            while True:
                指令 = sheet_download_torrent.cell(行指针, 1).value

                if 指令 == "_end":
                    break
                elif 指令 == "_to":
                    行指针 = int(sheet_download_torrent.cell(行指针, 2).value)
                    continue
                elif 指令 is None:
                    pass

                elif 指令 == "_start_row":
                    起始行 = int(sheet_download_torrent.cell(行指针, 2).value)
                elif 指令 == "_end_row":
                    结束行 = int(sheet_download_torrent.cell(行指针, 2).value)

                elif 指令 == "种子下载链接":
                    种子下载链接_column = sheet_download_torrent.cell(行指针, 2).value
                elif 指令 == "种子下载情况":
                    种子下载情况_column = sheet_download_torrent.cell(行指针, 2).value

                行指针 += 1

            # 读取种子下载链接
            torrent_download_url_list: list[str] = []
            for 行号 in range(起始行, 结束行):
                torrent_download_url = sheet_download_torrent.cell(行号, 种子下载链接_column).value
                torrent_download_status = sheet_download_torrent.cell(行号, 种子下载情况_column).value
                if torrent_download_status == 种子状态:
                    torrent_download_url_list.append(torrent_download_url)

            批量下载种子(种子下载地址, torrent_download_url_list)


if __name__ == "__main__":

    kumigumiPrint("开始执行脚本...")

    warnings.filterwarnings("ignore", category=UserWarning)

    excel_path = "D:/OneDrive/kumigumi.xlsx"
    kumigumiPrint(f"📖 读取 Excel 文件: {excel_path}")

    work_book: Workbook = load_workbook(safe_load(excel_path), data_only=True)

    res = 解析指令(work_book)

    print("解析结果:")
    print(f"数据库地址: {res.数据库地址}")
    print(f"数据库表名与工作表名: {res.A}")
    print(f"数据库表名元组与工作表名: {res.B}")
    print(f"地址与下载种子工作表名: {res.C}")

    # 检查是否定义变量
    if not res.数据库地址:
        raise ValueError("❌ 请确保在 main 工作表中定义了数据库地址")

    # 更新 Access 数据库
    for 数据库表名, 工作表名 in res.A:
        data: list[list[str]] = 获取工作表数据(work_book, 工作表名)
        更新数据库(res.数据库地址, 数据库表名, data)

    批量获取远程数据并更新数据库(work_book, res.数据库地址, res.B)

    下载种子(work_book, res.数据库地址, res.C)

    kumigumiPrint("所有操作完成")
