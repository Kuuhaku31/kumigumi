# kumigumi 追番计划

kumigumi 是一个以 Excel 为操作入口、SQLite 为本地数据仓库的追番辅助项目。当前版本的数据流已经统一为：

```text
Excel 指令/数据块 -> Task.* 抓取任务 -> Database.Info.*Info 数据对象 -> SQLiteAccess.UpsertInfo -> SQLite
```

旧的 `FetchTask/`、`InfoItem/`、`Database.Item`、`UpsertItem/UpdateItem` 系列已经废弃。历史代码保留在本地忽略目录中用于参考，不再作为当前实现的一部分。

## 当前能力

- 从 Excel 读取指令表和数据块，批量导入番剧、分集、观看记录、RSS、种子页信息。
- 通过 Bangumi API 抓取番剧与分集信息。
- 通过 RSS 抓取种子页信息，并按缺失的 `TOR_HASH` 下载 torrent 文件。
- 使用 SQLite 保存结构化数据，schema 以 `Table.md`、`Database.Info.*Info`、`SQLiteSQL` 和 `SQLiteAccess` 初始化逻辑为准。
- 所有数据库写入统一走 `SQLiteAccess.UpsertInfo` 方法。

## 项目结构

```text
src/main/java/
  Main/          程序入口，解析启动参数和 Excel 指令
  Excel/         Excel 读取、命令流和数据块解析
  Task/          新的抓取任务与批量任务执行器
  Database/      SQLite 初始化、访问层、SQL 定义和 Info 数据对象
  NetAccess/     Bangumi、RSS、torrent 下载等网络访问
  Utils/         数据块、torrent 元信息解析和数据库绑定工具

Table.md         当前数据库 schema
database.md      Database 模块结构和维护约定
excel.md         Excel 指令、数据块和查询示例
TODO.md          后续优化清单
```

## 启动参数

`Main.Main` 支持以下参数：

| 参数                | 简写  | 说明              |
| ------------------- | ----- | ----------------- |
| `--excel_file_path` | `-ex` | Excel 工作簿路径  |
| `--database_path`   | `-db` | SQLite 数据库路径 |
| `--log_path`        | 无    | 日志输出目录      |

示例：

```powershell
mvn -q test
mvn -q -DskipTests compile
java -cp <classpath> Main.Main --excel_file_path ignore/input.xlsx --database_path ignore/kumigumi.db --log_path ignore/logs/
```

如果参数未提供，程序会使用 `MetaData.ARGS` 中定义的默认值。

## Excel 工作流

Excel 通过命令和数据块驱动程序运行。常用命令如下：

| 命令                                      | 作用                               |
| ----------------------------------------- | ---------------------------------- |
| `_item_anime` / `_item_ani`               | 从数据块生成 `AnimeInfo`           |
| `_item_episode` / `_item_epi`             | 从数据块生成 `EpisodeInfo`         |
| `_item_episode_record`                    | 从数据块生成 `EpisodeRecordInfo`   |
| `_item_rss`                               | 从数据块生成 `RSSInfo`             |
| `_item_torrent_page`                      | 从数据块生成 `TorrentPageInfo`     |
| `_fetch_task_ani` / `_fetch_anime`        | 根据 `ANI_ID` 创建番剧信息抓取任务 |
| `_fetch_task_epi` / `_fetch_episode`      | 根据 `ANI_ID` 创建分集信息抓取任务 |
| `_fetch_task_tor` / `_fetch_torrent_page` | 根据 `URL_RSS` 创建种子页抓取任务  |
| `_run_fetch_task`                         | 执行前面创建的任务集合             |
| `_download_torrent`                       | 下载数据库中还缺失的 torrent 文件  |
| `_to_db`                                  | 将当前缓存的数据批量写入 SQLite    |

更完整的 Excel 块格式、字段要求和 Power Query 示例见 `excel.md`。

## 数据库

当前数据库包含六张核心表：

- `anime`
- `episode`
- `episode_record`
- `rss`
- `torrent_page`
- `torrent`

字段、主键和外键关系见 `Table.md`。Database 模块代码结构见 `database.md`。需要注意的是，`torrent_page.TOR_HASH` 用于关联后续下载得到的 `torrent.TOR_HASH`，但当前建表语句没有强制外键约束，这样 RSS 抓取结果可以先入库，torrent 文件可以稍后下载。

## 开发说明

- 新代码应使用 `Task.*` 表达抓取流程。
- 新数据对象应放在 `Database.Info.*Info` 中，并与 `Table.md` 保持一致。
- 数据库写入只新增或复用 `UpsertInfo`，不要恢复 `UpsertItem/UpdateItem` 抽象。
- schema 变更需要同步更新 `Table.md`、对应 `*Info` 类、`SQLiteSQL`、`SQLiteAccess` 初始化逻辑和 Excel 文档。

## 网络访问模块 `NetAccess`

NetAccess 模块对外保留以下静态入口，内部负责 HTTP 请求、Bangumi JSON 解析和 RSS 源分流：

```java
// 下载指定 URL 的内容，返回原始字节数组，适用于 torrent 等二进制文件
byte[] DownloadFile(String url_str)

// 根据 Bangumi 番剧 ID 获取番剧基本信息
AnimeInfo FetchAnimeInfo(Integer anime_id)

// 根据 Bangumi 番剧 ID 获取分集信息集合
Set<EpisodeInfo> FetchEpisodeInfoSet(Integer anime_id)

// 根据 RSS URL 获取种子页信息集合
Set<TorrentPageInfo> FetchTorrentPageInfoSet(String rss_url)
```
