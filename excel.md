# Excel 使用说明

kumigumi 以 Excel 作为批量操作入口。程序会读取命令流，按命令从指定工作表的数据块中生成 `Database.Info.*Info` 对象、创建 `Task.*` 抓取任务，并最终通过 `_to_db` 写入 SQLite。

## 命令流控制

Excel 指令由 `ExcelReader` 解析，当前支持：

| 指令       | 说明                             |
| ---------- | -------------------------------- |
| `#end`     | 结束命令流                       |
| `#goto`    | 跳转到指定命令位置               |
| `#define`  | 定义变量                         |
| `#goto_if` | 条件跳转                         |
| `#block`   | 定义一个可被后续命令引用的数据块 |

## 数据块格式

数据块以 `#block <BlockName>` 开始，以 `#block_end` 结束：

```text
#block AnimeRows
#sheet Anime
#from 1
#to 100
ANI_ID 0 int
title 1 text
title_cn 2 text
air_date 3 date
#block_end
```

字段行格式为：

```text
<字段名> <列序号> <类型>
```

支持的类型：

| 类型       | 说明     |
| ---------- | -------- |
| `int`      | 整数     |
| `date`     | 日期     |
| `time`     | 时间     |
| `datetime` | 日期时间 |
| `bool`     | 布尔值   |
| `text`     | 文本     |

## 运行命令

| 命令                                      | 输入                               | 输出/效果                       |
| ----------------------------------------- | ---------------------------------- | ------------------------------- |
| `_item_anime` / `_item_ani`               | anime 数据块                       | 生成 `AnimeInfo`                |
| `_item_episode` / `_item_epi`             | episode 数据块                     | 生成 `EpisodeInfo`              |
| `_item_episode_record`                    | episode_record 数据块              | 生成 `EpisodeRecordInfo`        |
| `_item_rss`                               | rss 数据块                         | 生成 `RSSInfo`                  |
| `_item_torrent_page`                      | torrent_page 数据块                | 生成 `TorrentPageInfo`          |
| `_fetch_task_ani` / `_fetch_anime`        | 含 `ANI_ID` 的数据块               | 创建番剧抓取任务                |
| `_fetch_task_epi` / `_fetch_episode`      | 含 `ANI_ID` 的数据块               | 创建分集抓取任务                |
| `_fetch_task_tor` / `_fetch_torrent_page` | 含 `URL_RSS` 或 `url_rss` 的数据块 | 创建 RSS 种子页抓取任务         |
| `_run_fetch_task`                         | 任务集合名                         | 执行任务并缓存结果              |
| `_download_torrent`                       | torrent_page 结果集合              | 下载数据库中缺失的 torrent 文件 |
| `_to_db`                                  | 已缓存的 Info 集合                 | 批量 upsert 到 SQLite           |

## 数据块字段

字段名应与 `Database.Info.*Info` 和 `Table.md` 保持一致。

### anime

必需字段：

- `ANI_ID`

可选字段：

- `air_date`
- `title`
- `title_cn`
- `aliases`
- `description`
- `episode_count`
- `url_official_site`
- `url_cover`
- `update_datetime`

### episode

必需字段：

- `EPI_ID`
- `ANI_ID`

可选字段：

- `ep`
- `sort`
- `air_date`
- `duration`
- `title`
- `title_cn`
- `description`
- `update_datetime`

### episode_record

必需字段：

- `EPI_ID`
- `view_datetime`
- `timezone`

可选字段：

- `rating`
- `comment`

`view_datetime` 与 `timezone` 会拼接后解析为 `OffsetDateTime`，例如 `2026-06-10T21:30:00` + `+09:00`。

### rss

必需字段：

- `URL_RSS`

可选字段：

- `ANI_ID`

### torrent_page

必需字段：

- `URL_RSS`
- `TOR_HASH`

可选字段：

- `air_datetime`
- `url_download`
- `url_page`
- `title`
- `subtitle_group`
- `description`
- `update_datetime`

### torrent

`torrent` 通常由 `_download_torrent` 自动生成，不建议手动维护。

字段：

- `TOR_HASH`
- `file_name`
- `file_size`
- `torrent_file`

## 推荐流程

```text
1. 用 _item_anime/_item_episode/_item_rss 导入基础数据
2. 用 _to_db 写入 SQLite
3. 用 _fetch_task_ani/_fetch_task_epi/_fetch_task_tor 创建抓取任务
4. 用 _run_fetch_task 执行任务并缓存结果
5. 用 _download_torrent 下载缺失 torrent
6. 再次用 _to_db 写入抓取和下载结果
```

## Power Query / ODBC 示例

以下示例用于从 SQLite 回填 Excel。表结构以 `Table.md` 为准。

按番剧查询分集：

```powerquery
let
    Source = Odbc.Query(
        "dsn=kumigumi",
        "select * from episode where ANI_ID = " & Number.ToText(ANI_ID)
    )
in
    Source
```

按番剧查询 RSS：

```powerquery
let
    Source = Odbc.Query(
        "dsn=kumigumi",
        "select * from rss where ANI_ID = " & Number.ToText(ANI_ID)
    )
in
    Source
```

按 RSS 查询种子页：

```powerquery
let
    Source = Odbc.Query(
        "dsn=kumigumi",
        "select * from torrent_page where URL_RSS = '" & URL_RSS & "'"
    )
in
    Source
```

按番剧查询种子页：

```powerquery
let
    Source = Odbc.Query(
        "dsn=kumigumi",
        "select tp.* from torrent_page tp join rss r on tp.URL_RSS = r.URL_RSS where r.ANI_ID = " & Number.ToText(ANI_ID)
    )
in
    Source
```

按种子哈希查询已下载 torrent：

```powerquery
let
    Source = Odbc.Query(
        "dsn=kumigumi",
        "select * from torrent where TOR_HASH = '" & TOR_HASH & "'"
    )
in
    Source
```
