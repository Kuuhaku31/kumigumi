# Excel 指令与数据块

Excel 工作簿由 `ExcelReader` 从名为 `main` 的工作表的第一行第一列开始读取。读取结果包含三类内容：

- `variables`：通过 `#define` 定义的字符串变量。
- `commands`：普通命令行，每行第一个单元格是命令名，后续单元格是参数。
- `dataBlockList`：通过 `#block` 元数据创建的 DataBlock 数据块。

## 预处理指令

| 指令         | 格式                            | 说明                                           |
| ------------ | ------------------------------- | ---------------------------------------------- |
| `#end`       | `#end`                          | 结束读取                                       |
| `#define`    | `#define name value`            | 定义字符串变量                                 |
| `#goto`      | `#goto row col [sheet]`         | 跳转到指定位置继续读取                         |
| `#goto_if`   | `#goto_if name row col [sheet]` | 如果变量 `name` 已定义则跳转                   |
| `#block`     | `#block blockName`              | 开始定义 DataBlock 元数据                      |
| `#sheet`     | `#sheet sheetName`              | 指定数据来源工作表                             |
| `#from`      | `#from row`                     | 指定起始行，使用 Excel 的 1 基编号，包含该行   |
| `#to`        | `#to row`                       | 指定结束行，使用 Excel 的 1 基编号，不包含该行 |
| `#block_end` | `#block_end`                    | 结束 DataBlock 元数据                          |

`#goto` 和 `#goto_if` 的行列编号都是 Excel 里的 1 基编号。`sheet` 为空时沿用当前工作表。

## DataBlock 格式

一个 DataBlock 定义由元数据和字段列表组成：

```text
#block RssRows
#sheet RSS
#from 2
#to 10
URL_RSS 1 text
ANI_ID  2 int
#block_end
```

字段行格式为：

```text
字段名 源列号 类型
```

源列号也是 Excel 的 1 基编号。`ExcelReadContext` 会把它转换成内部 0 基列号。

支持的类型：

| 类型       | 输出格式                |
| ---------- | ----------------------- |
| `int`      | 整数字符串              |
| `date`     | `yyyy-MM-dd`            |
| `time`     | `HH:mm:ss`              |
| `datetime` | `yyyy-MM-dd'T'HH:mm:ss` |
| `bool`     | `TRUE` / `FALSE`        |
| `text`     | 原始文本                |

日期、时间和日期时间类型当前主要面向 Excel 数值日期。无法解析时会得到 `null`。

## 命令总览

| 命令                            | 常用别名                                                | 参数                 | 结果                          |
| ------------------------------- | ------------------------------------------------------- | -------------------- | ----------------------------- |
| `_print_message`                | `_pm`                                                   | `<message...>`       | 打印消息                      |
| `_print_variable`               | `_pv`                                                   | `<var...>`           | 打印变量                      |
| `_save_log`                     | `_safe_log`                                             | `<file> <var...>`    | 保存变量打印结果              |
| `_make_info_episode_record`     | `_mier`, `_make_episode_record`, `_item_episode_record` | `<out> <block...>`   | 生成 `InfoSetItem`            |
| `_make_info_rss`                | `_mir`, `_make_rss`, `_item_rss`                        | `<out> <block...>`   | 生成 `InfoSetItem`            |
| `_make_task_fetch_anime`        | `_mtfa`, `_fetch_task_ani`, `_fetch_anime`              | `<out> <block...>`   | 生成 `TaskSetItem`            |
| `_make_task_fetch_episode`      | `_mtfe`, `_fetch_task_epi`, `_fetch_episode`            | `<out> <block...>`   | 生成 `TaskSetItem`            |
| `_make_task_fetch_torrent_page` | `_mtftp`, `_fetch_task_tor`, `_fetch_torrent_page`      | `<out> <block...>`   | 生成 `TaskSetItem`            |
| `_run_task`                     | `_run_fetch_task`, `_rft`                               | `<out> <taskVar...>` | 执行任务并收集 Info           |
| `_to_db`                        | 无                                                      | `<infoVar...>`       | 写入 SQLite                   |
| `_update_torrent`               | 无                                                      | `<infoVar...>`       | 下载缺失 torrent 并写入数据库 |
| `_export_torrent`               | 无                                                      | `<blockVar...>`      | 按 `TOR_HASH` 导出 `.torrent` |

## 命令细节

`_make_info_episode_record` 从 DataBlock 读取：

| 字段            | 必需 | 说明                   |
| --------------- | ---- | ---------------------- |
| `EPI_ID`        | 是   | Bangumi 分集 ID        |
| `view_datetime` | 是   | 不带时区的日期时间文本 |
| `timezone`      | 是   | 例如 `+09:00`          |
| `rating`        | 否   | 整数评分               |
| `comment`       | 否   | 评论                   |

`view_datetime` 和 `timezone` 会拼接后用 `OffsetDateTime.parse` 解析。无效行会被忽略。

`_make_info_rss` 从 DataBlock 读取：

| 字段      | 必需 | 说明            |
| --------- | ---- | --------------- |
| `URL_RSS` | 是   | RSS 订阅地址    |
| `ANI_ID`  | 否   | Bangumi 番剧 ID |

`_make_task_fetch_anime` 和 `_make_task_fetch_episode` 从 DataBlock 读取 `ANI_ID`。

`_make_task_fetch_torrent_page` 从 DataBlock 读取 `URL_RSS`。

`_run_task` 接收一个或多个 `TaskSetItem` 变量，使用 `Task.ParallelExecution` 并行执行，结果合并为 `InfoSetItem`。

`_to_db` 接收一个或多个 `InfoSetItem` 变量，并调用 `SQLiteAccess.UpsertInfo`。

`_update_torrent` 接收包含 `TorrentPageInfo` 的 `InfoSetItem`，查询数据库中还没有 torrent blob 的 `TOR_HASH`，下载成功后写入 `TorrentInfo`。

`_export_torrent` 接收 DataBlock 变量，要求包含 `TOR_HASH` 列。它从数据库读取已有 blob，并导出到 `EXPORT_DIR`。

## 当前限制

- 当前没有直接从 DataBlock 创建 `AnimeInfo`、`EpisodeInfo` 或 `TorrentPageInfo` 的 Excel 命令。
- `AnimeInfo` 和 `EpisodeInfo` 通常通过 Bangumi 抓取任务生成。
- `TorrentPageInfo` 通常通过 RSS 抓取任务生成。
- `_safe_log` 是当前枚举中保留的兼容别名，语义等同 `_save_log`。

## 示例流程

```text
#define RunName demo

#block AnimeIds
#sheet Anime
#from 2
#to 4
ANI_ID 1 int
#block_end

#block RssRows
#sheet RSS
#from 2
#to 4
URL_RSS 1 text
ANI_ID  2 int
#block_end

_make_info_rss rssInfo RssRows
_make_task_fetch_anime animeTasks AnimeIds
_make_task_fetch_episode episodeTasks AnimeIds
_make_task_fetch_torrent_page torrentPageTasks RssRows
_run_task fetched animeTasks episodeTasks torrentPageTasks
_to_db rssInfo fetched
_update_torrent fetched
```
