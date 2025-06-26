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
def 更新数据库(data: list[dict], headers: list[str], accdb_path: str, table_name: str):
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

    if not headers or len(headers) == 0:
        raise ValueError("❌ headers 列表不能为空")

    pk_column = headers[0]  # 假设第一列为主键

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
            update_fields = ", ".join(f"[{h}] = ?" for h in headers if h != pk_column)
            update_sql = f"UPDATE [{table_name}] SET {update_fields} WHERE [{pk_column}] = ?"
            update_values = [record.get(h, "") for h in headers if h != pk_column]
            cursor.execute(update_sql, tuple(update_values) + (pk_value,))
            更新_count += 1

        else:
            # 4. 执行插入
            field_names = ", ".join(f"[{h}]" for h in headers)
            placeholders = ", ".join("?" for _ in headers)
            insert_sql = f"INSERT INTO [{table_name}] ({field_names}) VALUES ({placeholders})"
            insert_values = [record.get(h, "") for h in headers]
            cursor.execute(insert_sql, tuple(insert_values))
            插入_count += 1

    conn.commit()
    cursor.close()
    conn.close()

    print("✅ 同步完成")
    print(f"➕ 插入记录数：{插入_count}")
    print(f"🔄 更新记录数：{更新_count}")


def 读取EXCEL表格区域(path: str, sheet_name: str) -> Tuple[List[str], List[dict]]:
    """
    读取 Excel 表格中指定工作表:
    :param path: Excel 文件路径
    :param sheet_name: 工作表名称
    第一行为表头，后续行为数据
    :return: 表头列表和数据行字典列表
    """
    print(f"📖 读取 Excel 文件: {path} 的工作表: {sheet_name}")

    wb = load_workbook(path, data_only=True)
    if sheet_name not in wb.sheetnames:
        raise ValueError(f"❌ 工作表 '{sheet_name}' 不存在")

    ws = wb[sheet_name]

    headers = []
    data = []

    # 读取第一行作为表头（从 A1 开始，直到第一个空单元格为止）
    for cell in ws[1]:
        if cell.value is None:
            break
        headers.append(str(cell.value).strip())

    num_cols = len(headers)
    if num_cols == 0:
        raise ValueError("❌ 未能读取到表头")

    # 从第二行开始读取数据，直到首列为空（视为表格结束）
    for row in ws.iter_rows(min_row=2, max_col=num_cols):
        if row[0].value is None:
            break  # 首列为空视为结束

        row_dict = {}
        for i in range(num_cols):
            key = headers[i]
            value = row[i].value
            row_dict[key] = "" if value is None else str(value)
        data.append(row_dict)

    wb.close()

    print(f"✅ 读取完成，共 {len(data)} 行数据，表头: {headers}")
    return headers, data


# Access 数据库路径和表名
accdb_path = "D:/def/test_db.accdb"
anime_table_name = "Anime"
episode_table_name = "Episode"

excel_path = "D:/OneDrive/2025.07.xlsx"
excel_sheet_name = "dev"


def func0():
    print("func0 called")

    # 读取 Excel 表格区域
    _, data = 读取EXCEL表格区域(excel_path, excel_sheet_name)

    # 更新 Access 数据库
    更新数据库(data, headers.番组表头_手动维护字段, accdb_path, anime_table_name)


def func1():
    print("func1 called")

    # 读取 Excel 表格区域
    headers, data = 读取EXCEL表格区域(excel_path, excel_sheet_name)

    # 更新 Access 数据库
    更新数据库(data, headers, accdb_path, episode_table_name)


def func2():
    print("func2 called")

    # 创建 Access 数据库表
    创建数据库表(accdb_path, anime_table_name, headers.番组表头, overwrite=True)
    创建数据库表(accdb_path, episode_table_name, headers.单集表头, overwrite=True)


def func3():
    print("func3 called")

    # 读取 Excel 表格区域
    _, data = 读取EXCEL表格区域(excel_path, "dev2")

    url_list: list[str] = []
    for row in data:
        if row.get(headers.番bangumiURL):
            url_list.append(row[headers.番bangumiURL])

    # 批量获取数据
    anime_info, episode_info = 批量获取数据(url_list)

    # 保存到 CSV 文件
    utils.保存CSV文件("anime.csv", headers.番组表头_src, anime_info)
    utils.保存CSV文件("episode.csv", headers.单集表头_src, episode_info)

    # 同步动画信息到 Access
    更新数据库(anime_info, headers.番组表头_src, accdb_path, anime_table_name)
    更新数据库(episode_info, headers.单集表头_src, accdb_path, episode_table_name)


if __name__ == "__main__":

    # func0()
    # func1()
    # func2()
    func3()

    print("所有操作完成")
