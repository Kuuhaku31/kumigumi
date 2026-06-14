# 表结构

本文档描述当前 SQLite schema。字段、主键和外键关系应与 `src/main/java/Database/SQLiteSQL.java`、`src/main/java/Database/SQLiteAccess.java` 以及 `Database.Info.*Info` 类保持一致。

统一约定：

- 表名、字段名和 Java `Database.Info.*Info` 类型保持一致。
- `date` 使用 `YYYY-MM-DD` 文本格式。
- `datetime` 使用带时区的 ISO-8601 文本格式，例如 `2026-06-10T12:34:56+09:00`。
- `TOR_HASH` 表示 torrent 的 info hash，是 `torrent` 表主键，也是 `torrent_page` 与下载结果的关联键。

## `anime`

| 字段                | 类型      | 说明            |
| ------------------- | --------- | --------------- |
| `ANI_ID`            | `integer` | Bangumi 番剧 ID |
| `air_date`          | `date`    | 放送开始日期    |
| `title`             | `text`    | 原标题          |
| `title_cn`          | `text`    | 中文标题        |
| `aliases`           | `text`    | 别名            |
| `description`       | `text`    | 简介            |
| `episode_count`     | `integer` | 总集数          |
| `url_official_site` | `text`    | 官方站点链接    |
| `url_cover`         | `text`    | 封面链接        |
| `update_datetime`   | `datetime` | 更新时间        |

主键：`ANI_ID DESC`

## `episode`

| 字段              | 类型       | 说明            |
| ----------------- | ---------- | --------------- |
| `EPI_ID`          | `integer`  | Bangumi 分集 ID |
| `ANI_ID`          | `integer`  | Bangumi 番剧 ID |
| `ep`              | `integer`  | 话数            |
| `sort`            | `real`     | 排序值          |
| `air_date`        | `date`     | 放送日期        |
| `duration`        | `text`     | 时长            |
| `title`           | `text`     | 标题            |
| `title_cn`        | `text`     | 中文标题        |
| `description`     | `text`     | 简介            |
| `update_datetime` | `datetime` | 更新时间        |

主键：`EPI_ID DESC`

外键：`ANI_ID` -> `anime.ANI_ID`

Bangumi 同时提供 `ep` 和 `sort` 两个分集编号字段：

- `ep` 是非负整数，大于 0 时通常表示正片话数，等于 0 时通常表示特别篇、总集篇、OVA、SP 等非正片。
- `sort` 是浮点排序值，可用于更直观地表示 `0`、`12.5` 等分集位置。

观看状态不保存在 `episode` 表中，应通过 `episode_record` 推导。下载状态不保存在 `episode` 表中，应通过 `rss`、`torrent_page` 和 `torrent` 推导。

## `episode_record`

| 字段            | 类型       | 说明            |
| --------------- | ---------- | --------------- |
| `EPI_ID`        | `integer`  | Bangumi 分集 ID |
| `view_datetime` | `datetime` | 观看日期时间    |
| `rating`        | `integer`  | 评分            |
| `comment`       | `text`     | 评论            |

主键：`(EPI_ID DESC, view_datetime DESC)`

外键：`EPI_ID` -> `episode.EPI_ID`

## `rss`

| 字段      | 类型      | 说明            |
| --------- | --------- | --------------- |
| `URL_RSS` | `text`    | RSS 订阅链接    |
| `ANI_ID`  | `integer` | Bangumi 番剧 ID |

主键：`URL_RSS DESC`

外键：`ANI_ID` -> `anime.ANI_ID ON DELETE SET NULL`

## `torrent_page`

| 字段              | 类型       | 说明              |
| ----------------- | ---------- | ----------------- |
| `URL_RSS`         | `text`     | RSS 订阅链接      |
| `TOR_HASH`        | `text`     | torrent info hash |
| `air_datetime`    | `datetime` | 发布日期时间      |
| `url_download`    | `text`     | 下载链接          |
| `url_page`        | `text`     | 页面链接          |
| `title`           | `text`     | 标题              |
| `subtitle_group`  | `text`     | 字幕组            |
| `description`     | `text`     | 简介              |
| `update_datetime` | `datetime` | 更新时间          |

主键：`(URL_RSS DESC, TOR_HASH DESC)`

外键：`URL_RSS` -> `rss.URL_RSS`

当前建表语句不强制 `TOR_HASH` 到 `torrent.TOR_HASH` 的外键约束。这样 RSS 抓取结果可以先入库，torrent 文件可以在后续 `_download_torrent` 步骤中补齐。

## `torrent`

| 字段           | 类型      | 说明                   |
| -------------- | --------- | ---------------------- |
| `TOR_HASH`     | `text`    | torrent info hash      |
| `file_name`    | `text`    | 文件名                 |
| `file_size`    | `integer` | 文件大小，单位为字节   |
| `torrent_file` | `blob`    | torrent 文件二进制内容 |

主键：`TOR_HASH DESC`

## 表关系

```text
anime 1 ---- 0..* episode 1 ---- 0..* episode_record
anime 1 ---- 0..* rss 1 ---- 0..* torrent_page
torrent_page 0..* ---- 0..1 torrent
```
