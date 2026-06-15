# Database 模块结构

本文档描述当前 `Database` 模块的代码结构、职责边界和维护约定。表字段、主键和外键细节见 `Table.md`。

## 总览

当前数据库写入链路为：

```text
Excel / NetAccess / Task
  -> Info.*Info
  -> SQLiteAccess.UpsertInfo(Set<? extends BaseInfo>)
  -> SQLiteSQL
  -> SQLite
```

`Database` 模块不负责网络请求和 Excel 命令解析。它负责：

- 将 `Info.*Info` 对象写入 SQLite。
- 在数据库文件不存在时创建表结构。
- 保存所有 SQL 文本。
- 查询 torrent 缺失状态、下载 URL，并导出 torrent 文件。

## 文件结构

```text
src/main/java/Database/
  SQLiteAccess.java        SQLite 访问门面、初始化执行、事务和批量写入
  SQLiteSQL.java           所有 SQLite SQL 文本和按类型选择 SQL 的工厂
  DatabaseUtils.java       PreparedStatement 安全绑定、hash 去重、分块和字符串绑定
  TorrentDownloader.java   TOR_HASH 与候选下载 URL 列表

src/main/java/Info/
  BaseInfo.java            所有可入库对象的公共基类
  AnimeInfo.java           anime 表数据对象
  EpisodeInfo.java         episode 表数据对象
  EpisodeRecordInfo.java   episode_record 表数据对象
  RSSInfo.java             rss 表数据对象
  TorrentPageInfo.java     torrent_page 表数据对象
  TorrentInfo.java         torrent 表数据对象

src/main/java/Utils/
  TorrentMetaUtil.java     torrent 元信息解析，供 TorrentInfo 使用
```

## 核心类职责

`SQLiteAccess` 是唯一对外数据库访问入口。构造时根据数据库路径打开 JDBC 连接；如果数据库文件不存在，会执行 `SQLiteSQL.createTableStatements()` 创建六张表；随后应用 `SQLiteSQL.PRAGMA_SETTINGS`。

`SQLiteAccess.UpsertInfo(Set<? extends BaseInfo>)` 是统一写入入口。它会先把混合集合一次性分类为 `AnimeInfo`、`EpisodeInfo`、`EpisodeRecordInfo`、`RSSInfo`、`TorrentPageInfo` 和 `TorrentInfo` 六个集合，再按外键依赖顺序批量写入：

```text
anime -> episode -> episode_record -> rss -> torrent_page -> torrent
```

所有批量写入位于同一个事务中。任何 `SQLException` 或运行时异常都会触发 rollback，并恢复调用前的 auto-commit 状态。

`SQLiteSQL` 是包内 SQL 集中定义类。建表 SQL、PRAGMA、upsert SQL、torrent 查询 SQL 都放在这里。业务代码不应在其它文件中散落 SQL 文本。

`BaseInfo` 是所有数据对象的公共基类，统一实现 `Printable` 的输出约定，并提供 `formatInfo` 辅助方法。

`Info.*Info` 类负责保存字段、校验构造参数、从 `Map<String, String>` 或 `DataBlock` 解析数据。这些类已经从 `Database` 中移出，不保存 SQL，也不直接绑定 `PreparedStatement`。

`DatabaseUtils` 位于 `Database` 包内，负责 JDBC 参数安全绑定、hash 去重、批量查询分块和字符串参数绑定。它是数据库访问层内部工具，不作为外部 API 使用。

## 表与对象

当前六张核心表与数据对象一一对应：

| SQLite 表 | Java 类型 |
| --- | --- |
| `anime` | `Info.AnimeInfo` |
| `episode` | `Info.EpisodeInfo` |
| `episode_record` | `Info.EpisodeRecordInfo` |
| `rss` | `Info.RSSInfo` |
| `torrent_page` | `Info.TorrentPageInfo` |
| `torrent` | `Info.TorrentInfo` |

`torrent_page.TOR_HASH` 与 `torrent.TOR_HASH` 是逻辑关联，但 schema 当前不强制外键。这样 RSS 抓取到的 torrent 页面可以先入库，torrent 文件稍后再补齐。

## 读取和导出辅助接口

`SQLiteAccess` 还保留三个 torrent 相关接口：

- `GetTorrentHashNotExist(Set<String> hashList)`：返回数据库中还没有有效 torrent blob 的 hash。
- `GetDownloaderByHash(Set<String> hashList)`：根据 hash 从 `torrent_page` 查询候选下载 URL，返回 `TorrentDownloader`。
- `ExportTorrentFiles(Set<String> torHashList, String safePath)`：从 `torrent` 表读取 blob 并导出为 `.torrent` 文件。

这些接口使用 `DatabaseUtils.normalizeHashes` 去重和过滤空 hash，使用 `DatabaseUtils.chunks` 按 `SQL_PARAM_CHUNK_SIZE` 分块，避免 SQL 参数数量过大。

## 维护约定

新增或修改数据库字段时，需要同步检查：

- `Table.md` 的字段、主键、外键说明。
- `SQLiteSQL` 的建表 SQL 和 upsert SQL。
- 对应 `Info.*Info` 类的字段、构造器，以及 `SQLiteAccess` 中各类型参数绑定顺序。
- Excel 文档中对应数据块字段。
- `SQLiteAccessTest` 或相关集成测试。

新增一种可入库对象时，需要：

- 在 `Info` 下新增 `*Info` 类并继承 `BaseInfo`。
- 在 `SQLiteSQL` 中新增 upsert SQL，并在 `SQLiteAccess.UpsertInfo` 中映射类型。
- 在 `SQLiteAccess.UpsertInfo` 中新增分类集合和批量写入顺序。
- 如果有新表，加入 `SQLiteSQL.createTableStatements()`。

不要恢复旧的 `UpsertXxxInfo`、`UpsertItem` 或 `UpdateItem` 接口。数据库写入统一走 `SQLiteAccess.UpsertInfo`。
