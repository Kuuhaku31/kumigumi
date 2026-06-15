# Excel 使用说明

kumigumi 以 Excel 作为批量操作入口。程序会读取命令流，按命令从指定工作表的数据块中生成 `Database.Info.*Info` 对象、创建 Main 内部 FetchTask 抓取任务，并最终通过 `_to_db` 写入 SQLite。

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

当前支持的命令如下：

| 命令                                       | 功能描述                                     |
| ------------------------------------------ | -------------------------------------------- |
| `_print_variable`                          | 打印变量内容                                 |
| `_print_message`                           | 打印消息内容                                 |
| `_make_info_episode_record` / `_mier`      | 根据 `DataBlock` 生成 EpisodeRecordInfo        |
| `_make_info_rss` / `_mir`                  | 根据 `DataBlock` 生成 RSSinfo                  |
| `_make_task_fetch_anime` / `_mtfa`         | 根据 `DataBlock` 生成 FetchAnimeInfoTask       |
| `_make_task_fetch_episode` / `_mtfe`       | 根据 `DataBlock` 生成 FetchEpisodeInfoTask     |
| `_make_task_fetch_torrent_page` / `_mtftp` | 根据 `DataBlock` 生成 FetchTorrentPageInfoTask |
| `_run_task`                                | 执行任务                                     |
| `_save_log`                                | 保存日志                                     |
| `_to_db`                                   | 写入数据库                                   |

### `_print_variable`

```text
_print_variable <variable_name> [<variable_name2> ...]
```

### `_print_message`

```text
_print_message <message>
```

### `_make_info_episode_record` / `_mier`

```text
_mier <item_name> <variable_name1> [<variable_name2> ...]
```

variable_name 必须引用一个 `DataBlock` 对象
且该 `DataBlock` 对象必须包含 `EPI_ID`、`view_datetime` 和 `timezone` 字段

1. 如果 `Map<String, Object> variables` 中不存在key为 `item_name` 的变量，则：
    构建完成后，`item_name` 变量将引用一个 `Set<EpisodeRecordInfo>` 对象。
2. 如果 `variables` 中已存在key为 `item_name` 的变量，且其值为一个 `Set<? extends BaseInfo>` 对象，则：
    构建完成后，将合并新生成的 `Set<EpisodeRecordInfo>` 对象到原有集合中。
3. 如果 `variables` 中已存在key为 `item_name` 的变量，但其值不是一个 `Set<? extends BaseInfo>` 对象，则：
    输出错误信息并跳过该命令。

### `_make_info_rss` / `_mir`

```text
_mir <item_name> <variable_name1> [<variable_name2> ...]
```

variable_name 必须引用一个 `DataBlock` 对象
且该 `DataBlock` 对象必须包含 `URL_RSS` 字段

接下来同 `_make_info_episode_record` / `_mier`

### `_make_task_fetch_anime` / `_mtfa`

```text
_mtfa <item_name> <variable_name1> [<variable_name2> ...]
```

variable_name 必须引用一个 `DataBlock` 对象
且该 `DataBlock` 对象必须包含 `ANI_ID` 字段

1. 如果 `Map<String, Object> variables` 中不存在key为 `item_name` 的变量，则：
    构建完成后，`item_name` 变量将引用一个 `Set<FetchAnimeInfoTask>` 对象。
2. 如果 `variables` 中已存在key为 `item_name` 的变量，且其值为一个 `Set<? extends Task>` 对象，则：
    构建完成后，将合并新生成的 `Set<FetchAnimeInfoTask>` 对象到原有集合中。
3. 如果 `variables` 中已存在key为 `item_name` 的变量，但其值不是一个 `Set<? extends Task>` 对象，则：
    输出错误信息并跳过该命令。

### `_make_task_fetch_episode` / `_mtfe`

同 `_make_task_fetch_anime` / `_mtfa`
但 `variable_name` 引用的 `DataBlock` 必须包含 `ANI_ID` 字段，生成的对象类型为 `FetchEpisodeInfoTask`

### `_make_task_fetch_torrent_page` / `_mtftp`

同 `_make_task_fetch_anime` / `_mtfa`
但 `variable_name` 引用的 `DataBlock` 必须包含 `URL_RSS` 字段，生成的对象类型为 `FetchTorrentPageInfoTask`

### `_run_task`

```text
_run_task <result_item_name> <variable_name1> [<variable_name2> ...]
```

`variable_name` 必须引用一个 `Set<? extends Task>` 对象

将所有任务集合合并到一起并丢给Task.ParallelExecution执行，获得一个结果，类型为 `Set<? extends BaseInfo>`

1. 如果 `Map<String, Object> variables` 中不存在key为 `result_item_name` 的变量，则：
    执行完成后，`result_item_name` 变量将引用一个 `Set<? extends BaseInfo>` 对象。
2. 如果 `variables` 中已存在key为 `result_item_name` 的变量，且其值为一个 `Set<? extends BaseInfo>` 对象，则：
    执行完成后，将合并新生成的结果集合到原有集合中。
3. 如果 `variables` 中已存在key为 `result_item_name` 的变量，但其值不是一个 `Set<? extends BaseInfo>` 对象，则：
    输出错误信息并跳过该命令。

### `_to_db`

```text
_to_db <variable_name1> [<variable_name2> ...]
```

`variable_name` 必须引用一个 `Set<? extends BaseInfo>` 对象

将所有集合合并到一起并写入 SQLite 数据库

调用 `SQLiteAccess.UpsertInfo`

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
