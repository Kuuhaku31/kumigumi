# database.py


import traceback

import pyodbc


# 操作数据库
def 更新数据库(accdb_path: str, table_name: str, data: list[list[str]]) -> list[int]:
    """
    同步数据到 Access 数据库。

    :param accdb_path: Access 数据库路径
    :param table_name: 目标表名
    :param data: list[list[Any]]，第一行为字段名，后续行为数据，第一列为主键字段
    :return: 返回处理失败的数据行索引列表（从1开始计数）
    """

    def database_print(msg: str, end: str = "\n"):
        print(f"\033[34m[数据库操作]:\033[0m {msg}", end=end)

    if not data or not data[0] or not data[0][0]:
        raise ValueError("❌ 数据为空或无效，或表头缺失主键字段")

    主键 = data[0][0]  # 主键字段名
    表头 = data[0]
    表头无主键 = 表头[1:]

    database_print(f"开始同步 → 数据库: '{accdb_path}', 表: '{table_name}'")

    conn_str = rf"DRIVER={{Microsoft Access Driver (*.mdb, *.accdb)}};DBQ={accdb_path};"
    conn = pyodbc.connect(conn_str)
    cursor = conn.cursor()

    # 获取当前已有主键
    cursor.execute(f"SELECT [{主键}] FROM [{table_name}]")
    已存在主键集 = {row[0] for row in cursor.fetchall()}

    插入计数 = 0
    更新计数 = 0
    处理失败数据索引列表: list[int] = []

    for 数据索引, 数据行 in enumerate(data[1:], start=1):
        try:
            # 处理数据长度小于表头长度的行
            if len(数据行) < len(表头):
                数据行 += [""] * (len(表头) - len(数据行))

            主键值 = 数据行[0]
            if 主键值 in ("", None):
                database_print(f"⚠️ 跳过记录，缺少主键 [{主键}]：{数据行}")
                处理失败数据索引列表.append(数据索引)
                continue

            # 构建值列表，确保长度与表头一致
            # 如果数据行长度小于表头，则补充空字符串
            值列表 = [数据行[i] if i < len(数据行) else "" for i in range(len(表头))]

            # 主键值已存在于数据库中，执行更新操作
            if 主键值 in 已存在主键集:
                # 构建更新语句（跳过主键列）
                update_fields = ", ".join(f"[{col}] = ?" for col in 表头无主键)
                update_sql = f"UPDATE [{table_name}] SET {update_fields} WHERE [{主键}] = ?"
                update_values = 值列表[1:] + [主键值]
                cursor.execute(update_sql, update_values)
                更新计数 += 1

            # 主键值不存在于数据库中，执行插入操作
            else:
                # 插入语句中主键列在最后（字段名顺序和数据匹配）
                insert_fields = ", ".join(f"[{col}]" for col in 表头无主键 + [主键])
                insert_placeholders = ", ".join("?" for _ in 表头)
                insert_sql = f"INSERT INTO [{table_name}] ({insert_fields}) VALUES ({insert_placeholders})"
                insert_values = 值列表[1:] + [值列表[0]]  # 主键放最后
                cursor.execute(insert_sql, insert_values)
                插入计数 += 1

        except Exception as e:
            database_print(f"❌ 错误：第 {数据索引} 行处理失败 → {e}")
            traceback.print_exc()
            处理失败数据索引列表.append(数据索引)
            continue

    conn.commit()
    cursor.close()
    conn.close()

    # 输出同步结果
    database_print(f"➕ 插入记录数：{插入计数}")
    database_print(f"🔄 更新记录数：{更新计数}")
    if 处理失败数据索引列表:
        database_print(f"❌ 失败记录数：{len(处理失败数据索引列表)}")
    print()

    return 处理失败数据索引列表


def 获取数据(accdb_path: str, table_name: str) -> list[list[str]]:
    """
    从 Access 数据库中获取数据。

    :param accdb_path: Access 数据库路径
    :param table_name: 目标表名
    :return: 返回查询结果，格式为 list[list[str]]
    """

    def database_print(msg: str, end: str = "\n"):
        print(f"\033[34m[数据库操作]:\033[0m {msg}", end=end)

    database_print(f"开始获取数据 → 数据库: '{accdb_path}', 表: '{table_name}'")

    conn_str = rf"DRIVER={{Microsoft Access Driver (*.mdb, *.accdb)}};DBQ={accdb_path};"
    conn = pyodbc.connect(conn_str)
    cursor = conn.cursor()

    cursor.execute(f"SELECT * FROM [{table_name}]")
    rows = cursor.fetchall()

    cursor.close()
    conn.close()

    if not rows:
        database_print(f"⚠️ 表 '{table_name}' 没有数据")
        return []

    # 将查询结果转换为 list[list[str]]
    result = [list(row) for row in rows]
    database_print(f"获取到数据行数：{len(result)}")
    return result
