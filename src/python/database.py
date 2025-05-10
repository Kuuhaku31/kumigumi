# 操作数据库

import sqlite3


def 创建anime表(cursor: sqlite3.Cursor):
    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS anime (
            bangumiURL TEXT PRIMARY KEY,
            mikananimeURL TEXT,
            originalTitle TEXT,
            chineseTitle TEXT,
            aliases TEXT,
            categories TEXT,
            episodeCount INTEGER,
            broadcastStart TEXT,
            broadcastDay INTEGER,
            officialSite TEXT,
            coverImage TEXT
        )
        """
    )


def 创建episodes表(cursor: sqlite3.Cursor):
    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS episodes (
            episodeURL TEXT PRIMARY KEY,
            bangumiURL TEXT,
            episodeIndex TEXT,
            originalTitle TEXT,
            chineseTitle TEXT,
            airDate TEXT,
            duration INTEGER,
            isDownloaded INTEGER,
            isWatched INTEGER
        )
        """
    )


def 创建torrents表(cursor: sqlite3.Cursor):
    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS torrents (
            torrentURL TEXT PRIMARY KEY,
            bangumiURL TEXT,
            subtitleGroup TEXT,
            releaseDate TEXT,
            torrentTitle TEXT,
            size TEXT,
            sizeBytes INTEGER,
            episodeIndex TEXT,
            resolution TEXT,
            source TEXT,
            sourceType TEXT,
            videoCodec TEXT,
            audioCodec TEXT,
            subtitleLanguage TEXT,
            fileFormat TEXT,
            tags TEXT,
            pageURL TEXT,
            isDownloaded INTEGER
        )
        """
    )


def 初始化数据库(存放地址: str):

    print("正在初始化数据库...")

    conn = sqlite3.connect(存放地址)
    cursor = conn.cursor()
    创建anime表(cursor)
    创建episodes表(cursor)
    创建torrents表(cursor)
    conn.commit()
    conn.close()

    print("数据库初始化完成！")


def 插入或更新anime表(数据库地址: str, 字典: dict):
    conn = sqlite3.connect(数据库地址)
    cursor = conn.cursor()

    # 插入数据
    cursor.execute(
        """
        INSERT OR REPLACE INTO anime (bangumiURL, mikananimeURL, originalTitle, chineseTitle, aliases, categories, episodeCount, broadcastStart, broadcastDay, officialSite, coverImage)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        (
            字典.get("作品番组计划网址", None),
            字典.get("作品蜜柑计划网址", None),
            字典.get("作品原名", None),
            字典.get("作品中文名", None),
            字典.get("作品别名", None),
            字典.get("作品分类", None),
            字典.get("作品话数", None),
            字典.get("作品放送开始", None),
            字典.get("作品放送星期", None),
            字典.get("作品官方网址", None),
            字典.get("作品封面", None),
        ),
    )

    conn.commit()
    conn.close()


def 插入或更新episodes表(数据库地址: str, 字典: dict):
    conn = sqlite3.connect(数据库地址)
    cursor = conn.cursor()

    # 插入数据
    cursor.execute(
        """
        INSERT OR REPLACE INTO episodes (episodeURL, bangumiURL, episodeIndex, originalTitle, chineseTitle, airDate, duration, isDownloaded, isWatched)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        (
            字典.get("话番组计划网址", None),
            字典.get("作品番组计划网址", None),
            字典.get("话索引", None),
            字典.get("话原标题", None),
            字典.get("话中文标题", None),
            字典.get("话首播时间", None),
            字典.get("话时长", None),
            字典.get("话是否下载", None),
            字典.get("话是否观看", None),
        ),
    )

    conn.commit()
    conn.close()


def 插入或更新torrents表(数据库地址: str, 字典: dict):
    conn = sqlite3.connect(数据库地址)
    cursor = conn.cursor()

    # 插入数据
    cursor.execute(
        """
        INSERT OR REPLACE INTO torrents (torrentURL, bangumiURL, subtitleGroup, releaseDate, torrentTitle, size, sizeBytes, episodeIndex, resolution, source, sourceType, videoCodec, audioCodec, subtitleLanguage, fileFormat, tags, pageURL, isDownloaded)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        (
            字典.get("种子下载链接", None),
            字典.get("作品番组计划网址", None),
            字典.get("种子字幕组", None),
            字典.get("种子发布日期", None),
            字典.get("种子标题", None),
            字典.get("种子大小", None),
            字典.get("种子大小（字节）", None),
            字典.get("话索引", None),
            字典.get("分辨率", None),
            字典.get("片源", None),
            字典.get("片源类型", None),
            字典.get("视频编码格式", None),
            字典.get("音频编码格式", None),
            字典.get("字幕语言", None),
            字典.get("文件格式", None),
            字典.get("其他标记", None),
            字典.get("种子下载页面网址", None),
            字典.get("种子是否下载", None),
        ),
    )

    conn.commit()
    conn.close()


if __name__ == "__main__":

    # 测试插入数据
    字典 = {
        "作品番组计划网址": "https://example.com/anime/123",
        "作品蜜柑计划网址": "https://example.com/mikan/123",
        "作品原名": "Original Title",
        "作品中文名": "Chinese Title",
        "作品别名": "Alias1, Alias2",
        "作品分类": "Action, Adventure",
        "作品话数": 12,
        "作品放送开始": "2023-01-01",
        "作品放送星期": 0,
        "作品官方网址": "https://example.com",
        "作品封面": "https://example.com/cover.jpg",
    }

    数据库地址 = "D:/Projects/kumigumi/src/python/test.db"

    初始化数据库(数据库地址)
    插入或更新anime表(数据库地址, 字典)
