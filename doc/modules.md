# 模块结构与接口

项目当前是单个 JPMS 模块：

```java
module kumigumi {
    requires java.net.http;
    requires java.sql;
    requires static jdk.httpserver;

    requires com.apptasticsoftware.rssreader;
    requires org.apache.poi.ooxml;
    requires org.apache.poi.poi;
    requires org.json;
    requires org.xerial.sqlitejdbc;

    exports Main;
    exports Database;
    exports Info;
    exports Excel;
    exports NetAccess;
    exports Utils;
}
```

## 依赖方向

```text
Main
  -> Excel
  -> Database
  -> Info
  -> Utils
  -> Main.FetchTask

Main.FetchTask
  -> NetAccess
  -> Info
  -> Database.TorrentDownloader
  -> Utils

Database
  -> Info
  -> java.sql

NetAccess
  -> Info
  -> java.net.http
  -> rssreader / org.json

Excel
  -> Utils
  -> Apache POI

Info
  -> Utils
```

`Main.FetchTask` 没有被 `exports`，因此只能作为 `kumigumi` 模块内部实现使用。

## `Main`

公开入口：

| 类          | 接口                                     | 说明       |
| ----------- | ---------------------------------------- | ---------- |
| `Main.Main` | `public static void main(String[] args)` | 程序主入口 |

包内核心类：

| 类                                                                      | 说明                                     |
| ----------------------------------------------------------------------- | ---------------------------------------- |
| `MainApplication`                                                       | 解析参数、读取 Excel、保存变量、执行命令 |
| `Commands`                                                              | Excel 命令处理器                         |
| `ExcelCommand`                                                          | 命令枚举和别名匹配                       |
| `Item` / `StringItem` / `InfoSetItem` / `TaskSetItem` / `DataBlockItem` | Main 内部变量模型                        |

## `Main.FetchTask`

未导出的内部包。

| 类                     | 说明                                              |
| ---------------------- | ------------------------------------------------- |
| `FetchInfoTask`        | 抓取任务基类，返回 `Set<? extends BaseInfo>`      |
| `FetchAnimeInfoTask`   | 根据 `ANI_ID` 抓取 `AnimeInfo`                    |
| `FetchEpisodeInfoTask` | 根据 `ANI_ID` 抓取 `EpisodeInfo` 集合             |
| `FetchTorrentPageTask` | 根据 `URL_RSS` 抓取 `TorrentPageInfo` 集合        |
| `FetchTorrentInfoTask` | 根据 `TorrentDownloader` 下载并解析 `TorrentInfo` |

任务由 `Utils.Task.ParallelExecution` 并行执行，当前固定线程池大小为 16。

## `Excel`

公开接口：

| 类            | 接口                    | 说明                                          |
| ------------- | ----------------------- | --------------------------------------------- |
| `ExcelReader` | `Read(String filePath)` | 读取 `.xlsx` 并返回 `ExcelResult`             |
| `ExcelResult` | record                  | 保存 `variables`、`commands`、`dataBlockList` |

包内实现：

| 类                     | 说明                              |
| ---------------------- | --------------------------------- |
| `ExcelReadContext`     | 解析指令、命令和 DataBlock 元数据 |
| `ExcelCursor`          | 读取游标                          |
| `TableMetaData`        | DataBlock 元数据                  |
| `CellStringType`       | 单元格类型转换                    |
| `CreateTableException` | 创建 DataBlock 失败时使用         |

## `Info`

公开数据对象：

| 类                  | 对应表           | 说明                  |
| ------------------- | ---------------- | --------------------- |
| `BaseInfo`          | 无               | 所有 Info 的打印基类  |
| `AnimeInfo`         | `anime`          | 番剧信息              |
| `EpisodeInfo`       | `episode`        | 分集信息              |
| `EpisodeRecordInfo` | `episode_record` | 观看记录              |
| `RSSInfo`           | `rss`            | RSS 订阅              |
| `TorrentPageInfo`   | `torrent_page`   | RSS 中的 torrent 页面 |
| `TorrentInfo`       | `torrent`        | torrent 文件与元信息  |

`Info` 不保存 SQL，不直接操作 JDBC。字段绑定与逐条写入由 `Database.Transactions` 完成。

## `Database`

公开接口：

| 类                  | 接口                                      | 说明                           |
| ------------------- | ----------------------------------------- | ------------------------------ |
| `SQLiteAccess`      | `SQLiteAccess(String dbPath)`             | 打开或创建 SQLite 数据库       |
| `SQLiteAccess`      | `UpsertInfo(Set<? extends BaseInfo>)`     | 在单个事务中写入 Info 对象集合 |
| `SQLiteAccess`      | `ReplaceRequiredViewFilters(Set<Integer>, Set<String>)` | 替换视图的 ANI_ID 与 RSS URL 筛选条件 |
| `SQLiteAccess`      | `GetTorrentHashNotExist(Set<String>)`     | 查询缺失 torrent blob 的 hash  |
| `SQLiteAccess`      | `GetDownloaderByHash(Set<String>)`        | 查询 hash 对应下载地址         |
| `SQLiteAccess`      | `ExportTorrentFiles(Set<String>, String)` | 导出 torrent blob              |
| `TorrentDownloader` | record                                    | 保存 `TOR_HASH` 和候选下载 URL |

包内实现：

| 类              | 说明                                        |
| --------------- | ------------------------------------------- |
| `SQLiteSQL`     | 建表、PRAGMA 和查询 SQL                     |
| `Transactions`  | Info 参数绑定、逐条写入和失败数据打印       |
| `DatabaseUtils` | JDBC 空值绑定、hash 去重、分块和 schema 校验 |

`UpsertInfo` 在同一个事务中依次执行所有 Info 类型。单条数据写入失败时会记录错误并继续；全部类型处理完成后统一打印失败数据，由用户决定提交其余成功数据或回滚本次全部写入。没有失败项时直接提交。

## `NetAccess`

公开接口：

| 方法                              | 返回                   | 说明                  |
| --------------------------------- | ---------------------- | --------------------- |
| `DownloadFile(String)`            | `byte[]`               | 下载二进制文件        |
| `FetchAnimeInfo(Integer)`         | `AnimeInfo`            | 获取 Bangumi 番剧信息 |
| `FetchEpisodeInfoSet(Integer)`    | `Set<EpisodeInfo>`     | 获取 Bangumi 分集列表 |
| `FetchTorrentPageInfoSet(String)` | `Set<TorrentPageInfo>` | 解析 Mikan / Nyaa RSS |

包内实现：

| 类                 | 说明                          |
| ------------------ | ----------------------------- |
| `BangumiParser`    | Bangumi JSON -> Info          |
| `BangumiQueryType` | Bangumi API URL               |
| `RSSParser`        | RSS item -> `TorrentPageInfo` |
| `RSSSourceType`    | RSS 来源识别                  |

## `Utils`

公开工具：

| 类                 | 说明                             |
| ------------------ | -------------------------------- |
| `DataBlock`        | Excel 数据块模型                 |
| `Task`             | 可并行执行任务基类               |
| `TaskStatus`       | 任务状态                         |
| `Printable`        | 标准打印接口                     |
| `TorrentMetaUtil`  | bencode torrent 元信息解析       |
| `UtilityFunctions` | 日期、字符串、文件输出、颜色输出 |
| `ColorCode`        | ANSI 颜色枚举                    |
