# main.py


import warnings

import headers
from database import 更新数据库
from fetch import 批量下载种子, 批量获取数据, 批量获取种子数据
from openpyxl import load_workbook
from utils import kumigumiPrint, safe_load

if __name__ == "__main__":

    warnings.filterwarnings("ignore", category=UserWarning)

    kumigumiPrint("开始执行脚本...")

    excel_path = "D:/OneDrive/kumigumi.xlsx"
    EXCEL文件地址 = safe_load(excel_path)
    kumigumiPrint(f"📖 读取 Excel 文件: {EXCEL文件地址}")

    wb = load_workbook(EXCEL文件地址, data_only=True)
    sheet_main = wb["main"]

    数据库地址: str = ""
    数据库anime表名: str = ""
    数据库episode表名: str = ""
    数据库torrent表名: str = ""

    excel_anime_sheet_fetch_list: list[str] = []
    excel_anime_sheet_store_list: list[str] = []
    excel_episode_sheet_store_list: list[str] = []
    excel_torrent_sheet_store_list: list[str] = []

    要下载的种子的状态: str = ""
    torrent_download_sheet_name: str = ""  # 用于存储种子下载链接的工作表名
    torrent_download_url_list: list[str] = []  # 用于存储种子下载链接

    # 解析 main 工作表
    行指针: int = 1
    while True:
        cell_Ax = sheet_main.cell(行指针, 1).value

        if cell_Ax == "_end":
            break
        elif cell_Ax == "_to":  # 跳到指定行
            行指针 = sheet_main.cell(行指针, 2).value
            continue
        elif cell_Ax is None:
            pass

        elif cell_Ax == "_database_path":
            数据库地址 = sheet_main.cell(行指针, 2).value
        elif cell_Ax == "_anime_table":
            数据库anime表名 = sheet_main.cell(行指针, 2).value
        elif cell_Ax == "_episode_table":
            数据库episode表名 = sheet_main.cell(行指针, 2).value
        elif cell_Ax == "_torrent_table":
            数据库torrent表名 = sheet_main.cell(行指针, 2).value

        elif cell_Ax == "_download_torrent":
            torrent_download_sheet_name = sheet_main.cell(行指针, 2).value
            要下载的种子的状态 = sheet_main.cell(行指针, 3).value

        elif cell_Ax == "_store":
            数据库表类型 = sheet_main.cell(行指针, 2).value
            工作表名 = sheet_main.cell(行指针, 3).value
            if 数据库表类型 == "_anime_table":
                excel_anime_sheet_store_list.append(工作表名)
            elif 数据库表类型 == "_episode_table":
                excel_episode_sheet_store_list.append(工作表名)
            elif 数据库表类型 == "_torrent_table":
                excel_torrent_sheet_store_list.append(工作表名)

        elif cell_Ax == "_fetch":
            excel_anime_sheet_fetch_list.append(sheet_main.cell(行指针, 3).value)

        else:
            kumigumiPrint(f"⚠️ 未知指令: {cell_Ax}")

        行指针 += 1

    # 检查是否定义变量
    if not 数据库地址 or not 数据库anime表名 or not 数据库episode表名 or not 数据库torrent表名:
        raise ValueError("❌ 请确保在 main 工作表中定义了数据库地址和表名")

    # 更新 Access 数据库
    for 数据库表名, 工作表名_list in zip(
        [数据库anime表名, 数据库episode表名, 数据库torrent表名],
        [excel_anime_sheet_store_list, excel_episode_sheet_store_list, excel_torrent_sheet_store_list],
    ):
        for 工作表名 in 工作表名_list:
            kumigumiPrint("🔄 更新 Access 数据库...")
            sheet_download_torrent = wb[工作表名]

            起始行: int = 0
            结束行: int = 0
            主键: str = ""
            字段字典: dict[str, int] = {}  # 字段名 : 列号

            行指针: int = 1
            while True:
                键: str = sheet_download_torrent.cell(row=行指针, column=1).value
                值: str = sheet_download_torrent.cell(row=行指针, column=2).value

                if 键 is None:
                    pass
                elif cell_Ax == "_to":  # 跳到指定行
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

            # 翻译
            主键 = headers.字段字典.get(主键, 主键)
            字段字典 = {headers.字段字典.get(k, k): v for k, v in 字段字典.items()}

            # 读取数据区域
            data: list[dict[str, int]] = []
            for 行号 in range(起始行, 结束行):
                row_data: dict[str, int] = {}
                for 字段名, 列号 in 字段字典.items():
                    单元格值 = sheet_download_torrent.cell(row=行号, column=列号).value
                    row_data[字段名] = 单元格值 if 单元格值 is not None else ""
                data.append(row_data)

            # 更新 Access 数据库
            更新数据库(data, 主键, [k for k in 字段字典.keys() if k != 主键], 数据库地址, 数据库表名)

    # 批量获取远程数据并更新数据库
    for 源sheet in excel_anime_sheet_fetch_list:
        kumigumiPrint("🔄 批量获取远程数据并更新数据库...")

        bgm_url_column: int = 0
        rss_url_column: int = 0
        起始行: int = 0
        结束行: int = 0

        # 读取源工作表
        print(f"📖 读取源工作表: {源sheet}")
        sheet_download_torrent = wb[源sheet]
        行指针 = 1
        while True:
            cell_Ax = sheet_download_torrent.cell(行指针, 1).value

            # 仅获取番组链接和RSS订阅链接
            if cell_Ax == "_end":
                break
            elif cell_Ax == "_to":  # 跳到指定行
                行指针 = int(sheet_download_torrent.cell(行指针, 2).value)
                continue
            elif cell_Ax is None:
                pass
            elif cell_Ax == "_start_row":
                起始行 = int(sheet_download_torrent.cell(行指针, 2).value)
            elif cell_Ax == "_end_row":
                结束行 = int(sheet_download_torrent.cell(行指针, 2).value)
            elif cell_Ax == "番组bangumi链接":
                bgm_url_column = sheet_download_torrent.cell(行指针, 2).value
            elif cell_Ax == "番组RSS订阅链接":
                rss_url_column = sheet_download_torrent.cell(行指针, 2).value

            行指针 += 1

        # 读取信息
        bgm_url_rss_映射: dict[str, str] = {}  # 番组链接 : RSS订阅链接
        for 行号 in range(起始行, 结束行):
            bgm_url = sheet_download_torrent.cell(行号, bgm_url_column).value
            rss_url = sheet_download_torrent.cell(行号, rss_url_column).value
            bgm_url_rss_映射[bgm_url] = rss_url

        anime_info_list, episode_info_list = 批量获取数据(bgm_url_rss_映射.keys())
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
            数据库anime表名,
        )
        更新数据库(
            episode_info_list,
            headers.单集表头_主键_en,
            headers.单集表头_自动更新_en,
            数据库地址,
            数据库episode表名,
        )
        更新数据库(
            torrent_info_list,
            headers.种子表头_主键_en,
            headers.种子表头_自动更新_en,
            数据库地址,
            数据库torrent表名,
        )

    # 下载种子链接
    if torrent_download_sheet_name != "":
        kumigumiPrint("🔄 下载种子链接...")

        # 获取种子下载链接工作表
        sheet_download_torrent = wb[torrent_download_sheet_name]

        起始行: int = 0
        结束行: int = 0
        种子下载链接_column: int = 0
        种子下载情况_column: int = 0

        # 读取种子下载链接
        行指针 = 1
        while True:
            cell_Ax = sheet_download_torrent.cell(行指针, 1).value

            if cell_Ax == "_end":
                break
            elif cell_Ax == "_to":
                行指针 = int(sheet_download_torrent.cell(行指针, 2).value)
                continue
            elif cell_Ax is None:
                pass

            elif cell_Ax == "_start_row":
                起始行 = int(sheet_download_torrent.cell(行指针, 2).value)
            elif cell_Ax == "_end_row":
                结束行 = int(sheet_download_torrent.cell(行指针, 2).value)

            elif cell_Ax == "种子下载链接":
                种子下载链接_column = sheet_download_torrent.cell(行指针, 2).value
            elif cell_Ax == "种子下载情况":
                种子下载情况_column = sheet_download_torrent.cell(行指针, 2).value

            行指针 += 1

        # 读取种子下载链接
        for 行号 in range(起始行, 结束行):
            torrent_download_url = sheet_download_torrent.cell(行号, 种子下载链接_column).value
            torrent_download_status = sheet_download_torrent.cell(行号, 种子下载情况_column).value
            if torrent_download_status == 要下载的种子的状态:
                torrent_download_url_list.append(torrent_download_url)

        批量下载种子(torrent_download_url_list)

    kumigumiPrint("所有操作完成")
