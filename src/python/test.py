# test.py


from concurrent.futures import ThreadPoolExecutor, as_completed
from typing import List, Tuple

import pyodbc
from openpyxl import load_workbook
from tqdm import tqdm

import bangumi
import headers
import utils


def 批量获取数据(url_list: list[str]) -> Tuple[List[dict], List[dict]]:
    """
    批量获取动画信息和单集信息
    :param url_list: 包含多个 Bangumi URL 的列表
    :return: 返回动画信息列表和单集信息列表
    """

    anime_info_list = []
    episode_info_list = []

    # 多线程实现
    with ThreadPoolExecutor() as executor:
        future_to_url = {executor.submit(utils.request_html, url): url for url in url_list}

        for future in tqdm(as_completed(future_to_url), total=len(future_to_url), desc="获取进度"):
            url = future_to_url[future]
            try:
                html_str = future.result()
                anime_info, episode_info = bangumi.解析BangumiHTML_str(html_str)
                anime_info_list.append(anime_info)
                episode_info_list.extend(episode_info)
            except Exception as e:
                print(f"❌ 获取 {url} 时发生错误: {e}")

    # 返回动画信息和单集信息
    return anime_info_list, episode_info_list


def 创建数据库表(accdb_path: str, table_name: str, headers: list[str], overwrite: bool = False):
    """
    创建 Access 数据库表
    :param accdb_path: Access 数据库路径
    :param table_name: 要创建的表名
    :param headers: 表头列表（第一个字段作为主键）
    :param overwrite: 是否覆盖已存在的表
    """
    if not headers:
        raise ValueError("❌ 表头列表不能为空")

    conn_str = r"DRIVER={Microsoft Access Driver (*.mdb, *.accdb)};" rf"DBQ={accdb_path};"
    conn = pyodbc.connect(conn_str, autocommit=True)
    cursor = conn.cursor()

    # 删除旧表（如果 overwrite = True）
    if overwrite:
        try:
            cursor.execute(f"DROP TABLE [{table_name}]")
            print(f"🗑️ 已删除旧表：{table_name}")
        except Exception as e:
            print(f"⚠️ 删除旧表失败或不存在：{e}")

    # 构建字段定义
    field_defs = [f"[{headers[0]}] TEXT PRIMARY KEY"]  # 主键字段
    field_defs += [f"[{col}] TEXT" for col in headers[1:]]

    create_sql = f"CREATE TABLE [{table_name}] ({', '.join(field_defs)})"
    cursor.execute(create_sql)
    print(f"✅ 已创建新表：{table_name}，字段数：{len(headers)}")

    cursor.close()
    conn.close()


# 同步数据到 Access 数据库
def 更新数据库(data: list[dict], pk: str, headers_no_pk: list[str], accdb_path: str, table_name: str):
    """
    同步数据到 Access 数据库
    :param data:     list[dict]，每一行为一个字典，可能包含无关字段
    :param headers:  需要写入的字段列表（顺序指定）
    :param accdb_path: Access 数据库路径
    :param table_name: 目标表名

    逻辑：
    - 将 headers 的第一列作为主键
    - 遍历数据：
        - 如果缺主键或主键值为空，跳过
        - 若主键已存在 → 仅更新 headers 中指定的字段
        - 否则 → 仅插入 headers 中指定的字段
    """

    print(f"🔄 同步数据到 Access 数据库: {accdb_path} 的表 {table_name}")

    conn_str = r"DRIVER={Microsoft Access Driver (*.mdb, *.accdb)};" rf"DBQ={accdb_path};"
    conn = pyodbc.connect(conn_str)
    cursor = conn.cursor()

    # 1. 获取主键列名
    #  将 headers 的第一列作为主键

    if not pk:
        raise ValueError("❌ 主键列名 pk 不能为空")
    elif not headers_no_pk or len(headers_no_pk) == 0:
        raise ValueError("❌ headers 列表不能为空")

    pk_column = pk

    # cursor.execute(f"SELECT * FROM [{table_name}]")
    # pk_column = None
    # for column in cursor.description:
    #     if column[5]:  # column[5] 为 True 表示是主键
    #         pk_column = column[0]
    #         break

    if not pk_column:
        raise Exception(f"❌ 无法获取 Access 表 [{table_name}] 的主键列")

    插入_count = 0
    更新_count = 0

    for record in data:
        if pk_column not in record or not record[pk_column]:
            print(f"⚠️ 跳过记录，缺少主键 [{pk_column}]：{record}")
            continue

        # 仅保留 headers 中字段，按顺序提取值（空填""）
        # row = [record.get(h, "") for h in headers]
        pk_value = record[pk_column]

        # 2. 判断主键是否存在
        cursor.execute(f"SELECT COUNT(*) FROM [{table_name}] WHERE [{pk_column}] = ?", (pk_value,))
        exists = cursor.fetchone()[0] > 0

        if exists:
            # 3. 执行更新
            update_fields = ", ".join(f"[{h}] = ?" for h in headers_no_pk)
            update_sql = f"UPDATE [{table_name}] SET {update_fields} WHERE [{pk_column}] = ?"
            update_values = [record.get(h, "") for h in headers_no_pk]
            cursor.execute(update_sql, tuple(update_values) + (pk_value,))
            更新_count += 1

        else:
            # 4. 执行插入
            field_names = ", ".join(f"[{h}]" for h in headers_no_pk)
            field_names += f", [{pk_column}]"  # 添加主键列
            placeholders = ", ".join("?" for _ in headers_no_pk)
            placeholders += ", ?"
            insert_sql = f"INSERT INTO [{table_name}] ({field_names}) VALUES ({placeholders})"
            insert_values = [record.get(h, "") for h in headers_no_pk]
            cursor.execute(insert_sql, tuple(insert_values) + (pk_value,))
            插入_count += 1

    conn.commit()
    cursor.close()
    conn.close()

    print("✅ 同步完成")
    print(f"➕ 插入记录数：{插入_count}")
    print(f"🔄 更新记录数：{更新_count}")


def 读取EXCEL表格区域(path: str, sheet_name: str) -> Tuple[List[str], List[dict]]:
    """
    读取 Excel 表格中指定工作表的区域（由 A1:A4 定义）
    返回：表头列表和数据字典列表
    """
    print(f"📖 读取 Excel 文件: {path} 的工作表: {sheet_name}")

    wb = load_workbook(path, data_only=True)
    if sheet_name not in wb.sheetnames:
        raise ValueError(f"❌ 工作表 '{sheet_name}' 不存在")

    ws = wb[sheet_name]

    # Step 1: 读取 A1, A2, A3, A4
    row = ws["A1"].value
    start_col = ws["A2"].value
    height = ws["A3"].value
    width = ws["A4"].value

    # Step 2: 解析坐标
    # start_col 和 width 可能是字母和数字混合的情况，需转换为列号
    # 假设 start_col 是列号（数字），否则需要 openpyxl.utils.column_index_from_string
    # 这里假设 start_col/width 都为整数
    if (
        not isinstance(row, int)
        or not isinstance(start_col, int)
        or not isinstance(height, int)
        or not isinstance(width, int)
    ):
        raise ValueError("❌ A1:A4 必须为整数，分别代表起始行、起始列、区域高、区域宽")

    end_row = row + height - 1
    end_col = start_col + width - 1

    # Step 3: 读取区域内的数据
    headers = []
    data = []

    # 表头行
    header_row = ws.iter_rows(min_row=row, max_row=row, min_col=start_col, max_col=end_col)
    for header_cell in next(header_row):
        if header_cell.value is None:
            headers.append("")
        else:
            headers.append(str(header_cell.value).strip())

    if not any(headers):
        raise ValueError("❌ 区域内未能读取到有效表头")

    # 数据行
    for row_cells in ws.iter_rows(min_row=row + 1, max_row=end_row, min_col=start_col, max_col=end_col):
        # 如果首列为空，跳过整行
        if row_cells[0].value is None:
            continue
        row_dict = {}
        for i, cell in enumerate(row_cells):
            key = headers[i] if i < len(headers) else f"列{i+1}"
            value = "" if cell.value is None else str(cell.value)
            row_dict[key] = value
        data.append(row_dict)

    wb.close()
    print(f"✅ 读取完成，共 {len(data)} 行数据，表头: {headers}")
    return headers, data


# Access 数据库路径和表名
全局_accdb_path = "D:/def/test_db.accdb"
全局_数据库anime表名 = "anime"
全局_数据库episode表名 = "episode"
全局_数据库torrent表名 = "torrent"
# 全局_kumigumi_db_path = "D:/def/kumigumi.accdb"

excel_path = "D:/def/2025.07.xlsx"
excel_sheet_name = "ani_index"
excel_sheet_name_ep202504 = "ep202504"
excel_sheet_name_ani202507 = "ani202507"
excel_sheet_name_ani202504 = "ani202504"


def 读取表格区域并更新数据库(EXCEL文件地址, 工作表名, mode):
    print("读取表格区域并更新数据库")

    # 读取 Excel 表格区域
    _, data = 读取EXCEL表格区域(EXCEL文件地址, 工作表名)
    data = [{headers.字段字典.get(k, k): v for k, v in row.items()} for row in data]

    # 更新 Access 数据库
    if mode == "a":
        更新数据库(
            data,
            headers.番组表头_主键_en,
            headers.番组表头_手动维护_en,
            全局_accdb_path,
            全局_数据库anime表名,
        )
    elif mode == "e":
        更新数据库(
            data,
            headers.单集表头_主键_en,
            headers.单集表头_手动维护_en,
            全局_accdb_path,
            全局_数据库episode表名,
        )
    elif mode == "t":
        # todo
        pass
    else:
        raise ValueError("❌ 无效的模式")


def 读取表格区域并爬取数据然后更新数据库(EXCEL文件地址, 工作表名):
    print("读取表格区域并爬取数据然后更新数据库")

    # 读取 Excel 表格区域
    _, data = 读取EXCEL表格区域(EXCEL文件地址, 工作表名)

    bgm_url_list: list[str] = []
    for row in data:
        if row.get("番组bangumi链接"):
            bgm_url_list.append(row["番组bangumi链接"])

    # 批量获取数据
    anime_info, episode_info = 批量获取数据(bgm_url_list)

    # 翻译键名
    anime_info = [{headers.字段字典.get(k, k): v for k, v in row.items()} for row in anime_info]
    episode_info = [{headers.字段字典.get(k, k): v for k, v in row.items()} for row in episode_info]

    # 同步动画信息到 Access
    更新数据库(
        anime_info,
        headers.番组表头_主键_en,
        headers.番组表头_自动更新_en,
        全局_accdb_path,
        全局_数据库anime表名,
    )
    更新数据库(
        episode_info,
        headers.单集表头_主键_en,
        headers.单集表头_自动更新_en,
        全局_accdb_path,
        全局_数据库episode表名,
    )


if __name__ == "__main__":

    print("开始执行脚本...")

    读取表格区域并更新数据库(excel_path, "ep202504", "e")
    # 读取表格区域并爬取数据然后更新数据库(EXCEL文件地址=excel_path, 工作表名=excel_sheet_name_ani202507)

    print("所有操作完成")
