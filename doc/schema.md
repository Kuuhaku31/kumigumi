# SQLite Schema

Schema 由 `Database.SQLiteSQL` 创建，并由 `Database.SQLiteAccess` 在数据库文件不存在时初始化。字段和 Java 数据对象位于 `Info.*Info`。

## 统一约定

- `date` 以 `yyyy-MM-dd` 文本保存。
- `datetime` 以带时区的 ISO-8601 文本保存，例如 `2026-06-10T12:34:56+09:00`。
- `update_datetime` 由 Java 对象构造时生成。
- 所有写入统一走 `SQLiteAccess.UpsertInfo(Set<? extends BaseInfo>)`。

初始化时会执行：

```sql
PRAGMA foreign_keys = ON;
PRAGMA journal_mode = WAL;
PRAGMA synchronous = NORMAL;
PRAGMA temp_store = MEMORY;
PRAGMA cache_size = 10000;
```

## 表关系

```text
anime 1 ---- 0..* episode 1 ---- 0..* episode_record
anime 1 ---- 0..* rss 1 ---- 0..* torrent_page
torrent_page 0..* ---- 0..1 torrent
```

`torrent_page.TOR_HASH` 与 `torrent.TOR_HASH` 是逻辑关联，当前 schema 不强制外键。这样 RSS 结果可以先入库，torrent 文件后续补齐。

## `anime`

Java 类型：`Info.AnimeInfo`

| 字段                | 类型    | 必需 | 说明            |
| ------------------- | ------- | ---- | --------------- |
| `ANI_ID`            | integer | 是   | Bangumi 番剧 ID |
| `air_date`          | text    | 否   | 放送开始日期    |
| `title`             | text    | 否   | 原标题          |
| `title_cn`          | text    | 否   | 中文标题        |
| `aliases`           | text    | 否   | 别名            |
| `description`       | text    | 否   | 简介            |
| `episode_count`     | integer | 否   | 总集数          |
| `url_official_site` | text    | 否   | 官方站点        |
| `url_cover`         | text    | 否   | 封面链接        |
| `update_datetime`   | text    | 是   | 更新时间        |

主键：`ANI_ID DESC`

## `episode`

Java 类型：`Info.EpisodeInfo`

| 字段              | 类型    | 必需 | 说明            |
| ----------------- | ------- | ---- | --------------- |
| `EPI_ID`          | integer | 是   | Bangumi 分集 ID |
| `ANI_ID`          | integer | 是   | Bangumi 番剧 ID |
| `ep`              | integer | 否   | 分集编号        |
| `sort`            | real    | 否   | 排序值          |
| `air_date`        | text    | 否   | 放送日期        |
| `duration`        | integer | 否   | 时长            |
| `title`           | text    | 否   | 标题            |
| `title_cn`        | text    | 否   | 中文标题        |
| `description`     | text    | 否   | 简介            |
| `update_datetime` | text    | 是   | 更新时间        |

主键：`EPI_ID DESC`

外键：`ANI_ID -> anime.ANI_ID ON DELETE CASCADE ON UPDATE CASCADE`

## `episode_record`

Java 类型：`Info.EpisodeRecordInfo`

| 字段            | 类型    | 必需 | 说明            |
| --------------- | ------- | ---- | --------------- |
| `EPI_ID`        | integer | 是   | Bangumi 分集 ID |
| `view_datetime` | text    | 是   | 观看日期时间    |
| `rating`        | integer | 否   | 评分            |
| `comment`       | text    | 否   | 评论            |

主键：`(EPI_ID DESC, view_datetime DESC)`

外键：`EPI_ID -> episode.EPI_ID ON DELETE CASCADE ON UPDATE CASCADE`

## `rss`

Java 类型：`Info.RSSInfo`

| 字段      | 类型    | 必需 | 说明            |
| --------- | ------- | ---- | --------------- |
| `URL_RSS` | text    | 是   | RSS 订阅地址    |
| `ANI_ID`  | integer | 否   | Bangumi 番剧 ID |

主键：`URL_RSS DESC`

外键：`ANI_ID -> anime.ANI_ID ON DELETE SET NULL ON UPDATE CASCADE`

## `torrent_page`

Java 类型：`Info.TorrentPageInfo`

| 字段              | 类型 | 必需 | 说明              |
| ----------------- | ---- | ---- | ----------------- |
| `URL_RSS`         | text | 是   | RSS 订阅地址      |
| `TOR_HASH`        | text | 是   | torrent info hash |
| `air_datetime`    | text | 否   | 发布日期时间      |
| `url_download`    | text | 否   | 下载地址          |
| `url_page`        | text | 否   | 页面地址          |
| `title`           | text | 否   | 标题              |
| `subtitle_group`  | text | 否   | 字幕组            |
| `description`     | text | 否   | 简介              |
| `update_datetime` | text | 是   | 更新时间          |

主键：`(URL_RSS DESC, TOR_HASH DESC)`

外键：`URL_RSS -> rss.URL_RSS ON DELETE CASCADE ON UPDATE CASCADE`

## `torrent`

Java 类型：`Info.TorrentInfo`

| 字段           | 类型    | 必需 | 说明                     |
| -------------- | ------- | ---- | ------------------------ |
| `TOR_HASH`     | text    | 是   | torrent info hash        |
| `file_name`    | text    | 否   | torrent 元信息中的文件名 |
| `file_size`    | integer | 否   | 文件大小，单位字节       |
| `torrent_file` | blob    | 否   | 原始 torrent 文件        |

主键：`TOR_HASH DESC`

## Upsert 顺序

`SQLiteAccess.UpsertInfo` 会先按运行时类型分类，再在同一个事务中按以下顺序写入：

```text
anime -> episode -> episode_record -> rss -> torrent_page -> torrent
```

该顺序保证 `episode`、`episode_record`、`rss`、`torrent_page` 的外键依赖尽量先满足。任何 `SQLException` 或运行时异常都会 rollback。

## view 表格

以下视图可通过 `Utils.CreateDatabaseViews` 入口创建。该入口会删除已有的同名视图，并按照本节定义重新创建：

```powershell
java --enable-native-access=ALL-UNNAMED `
  -cp target/kumigumi-<version>.jar `
  Utils.CreateDatabaseViews <database-path>
```

### view_anime

| 字段                       | 源字段                    | 说明              |
| -------------------------- | ------------------------- | ----------------- |
| `ANI_ID`                   | `anime.ANI_ID`            | Bangumi 番剧 ID   |
| `ani_air_date`             | `anime.air_date`          | 放送开始日期      |
| `ani_title`                | `anime.title`             | 原标题            |
| `ani_title_cn`             | `anime.title_cn`          | 中文标题          |
| `ani_aliases`              | `anime.aliases`           | 别名              |
| `ani_description`          | `anime.description`       | 简介              |
| `ani_episode_count`        | `anime.episode_count`     | 总集数            |
| `ani_official_site`        | `anime.url_official_site` | 官方站点          |
| `ani_cover`                | `anime.url_cover`         | 封面链接          |
| `ani_info_update_datetime` | `anime.update_datetime`   | 更新时间          |
| `ani_rss_list`             |                           | RSS 订阅地址      |
| `ani_bgm_site`             |                           | 番剧 Bangumi 页面 |

- `ani_rss_list` 字段要先查找 `rss` 表中所有 `ANI_ID` 对应的 `URL_RSS`，再用 `; ` 拼接成字符串
- `ani_bgm_site` 由 "https://bgm.tv/subject/" + `ANI_ID` 组成

### view_episode

| 字段                       | 源字段                    | 说明            |
| -------------------------- | ------------------------- | --------------- |
| `EPI_ID`                   | `episode.EPI_ID`          | Bangumi 分集 ID |
| `ANI_ID`                   | `episode.ANI_ID`          | Bangumi 番剧 ID |
| `epi_index`                | `episode.ep`              | 分集编号        |
| `epi_sort`                 | `episode.sort`            | 排序值          |
| `epi_air_date`             | `episode.air_date`        | 放送日期        |
| `epi_duration`             | `episode.duration`        | 时长            |
| `epi_title`                | `episode.title`           | 标题            |
| `epi_title_cn`             | `episode.title_cn`        | 中文标题        |
| `epi_description`          | `episode.description`     | 简介            |
| `epi_info_update_datetime` | `episode.update_datetime` | 更新时间        |
| `ani_title`                | `anime.title`             | 原标题          |
| `ani_title_cn`             | `anime.title_cn`          | 中文标题        |

### view_torrent_page

| 字段              | 源字段                         | 说明              |
| ----------------- | ------------------------------ | ----------------- |
| `URL_RSS`         | `torrent_page.URL_RSS`         | RSS 订阅地址      |
| `TOR_HASH`        | `torrent_page.TOR_HASH`        | torrent info hash |
| `air_datetime`    | `torrent_page.air_datetime`    | 发布日期时间      |
| `url_download`    | `torrent_page.url_download`    | 下载地址          |
| `url_page`        | `torrent_page.url_page`        | 页面地址          |
| `title`           | `torrent_page.title`           | 标题              |
| `subtitle_group`  | `torrent_page.subtitle_group`  | 字幕组            |
| `description`     | `torrent_page.description`     | 简介              |
| `update_datetime` | `torrent_page.update_datetime` | 更新时间          |
| `ani_title`       | `anime.title`                  | 原标题            |
| `ani_title_cn`    | `anime.title_cn`               | 中文标题          |
| `tor_file_size`   | `torrent.file_size`            | torrent 文件大小  |
| `tor_file_name`   | `torrent.file_name`            | torrent 文件名    |
