# 操作数据库

import sqlite3

import utils


def 创建表(表名称: str, 表头: list, cursor: sqlite3.Cursor):
    SQL语句 = f"CREATE TABLE IF NOT EXISTS {表名称} ("
    SQL语句 += f"{表头[0]} TEXT PRIMARY KEY, "
    for i in range(1, len(表头)):
        SQL语句 += f"{表头[i]} TEXT, "
    SQL语句 = SQL语句[:-2] + ")"

    cursor.execute(SQL语句)


def 初始化数据库(存放地址: str):

    print("正在初始化数据库...")

    conn = sqlite3.connect(存放地址)
    cursor = conn.cursor()
    创建表("anime", utils.anime表表头, cursor)
    创建表("episodes", utils.episodes表表头, cursor)
    创建表("torrents", utils.torrents表表头, cursor)
    conn.commit()
    conn.close()

    print("数据库初始化完成！")


def 插入或更新表(数据库地址: str, 表名称: str, 表头: list, 待更新数据: dict):

    # 生成插入或更新的 SQL 语句
    SQL语句 = f"INSERT OR REPLACE INTO {表名称} ("
    SQL语句_VALUES = "VALUES ("

    # 仅更新字典里有的字段
    valuesList = []
    for key in 表头:
        value = 待更新数据.get(key)
        if value is not None:
            valuesList.append(value)
            SQL语句 += f"{key}, "
            SQL语句_VALUES += "?, "

    # 去掉最后的逗号和空格
    SQL语句 = SQL语句[:-2] + ") "
    SQL语句_VALUES = SQL语句_VALUES[:-2] + ")"
    SQL语句 += SQL语句_VALUES

    # 执行插入或更新操作
    conn = sqlite3.connect(数据库地址)
    cursor = conn.cursor()
    cursor.execute(SQL语句, valuesList)
    conn.commit()
    conn.close()


def 利用ID查询话番组计划网址(数据库地址: str, ID: str) -> str:
    # 连接到数据库
    conn = sqlite3.connect(数据库地址)
    cursor = conn.cursor()

    # 执行查询操作
    cursor.execute(f"SELECT * FROM anime WHERE {utils.作品番组计划网址} = ?", (f"https://bangumi.tv/subject/{ID}",))
    result = cursor.fetchone()

    # 关闭连接
    conn.close()
    # 返回结果
    if result is not None:
        return result[0]
    else:
        return None
