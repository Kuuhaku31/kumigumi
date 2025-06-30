# database.py


# 操作数据库

import pyodbc


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

    def database_print(msg: str, end: str = "\n"):
        print(f"\033[92m[数据库操作]:\033[0m {msg}", end=end)

    database_print(f"同步数据到数据库: {accdb_path} 的表 {table_name} : ", "")

    conn_str = r"DRIVER={Microsoft Access Driver (*.mdb, *.accdb)};" rf"DBQ={accdb_path};"
    conn = pyodbc.connect(conn_str)
    cursor = conn.cursor()

    # 1. 获取主键列名
    if not pk:
        raise ValueError("❌ 主键列名 pk 不能为空")
    elif not headers_no_pk or len(headers_no_pk) == 0:
        raise ValueError("❌ headers 列表不能为空")
    pk_column = pk

    if not pk_column:
        raise Exception(f"❌ 无法获取 Access 表 [{table_name}] 的主键列")

    插入_count = 0
    更新_count = 0

    for record in data:
        if pk_column not in record or not record[pk_column]:
            database_print(f"⚠️ 跳过记录，缺少主键 [{pk_column}]：{record}")
            continue

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

    print("同步完成")
    database_print(f"➕ 插入记录数：{插入_count}")
    database_print(f"🔄 更新记录数：{更新_count}")
    print()
