# 表格结构

## `anime` 表

| 键名                | 数据类型 | 解释              | 备注           |
| ------------------- | -------- | ----------------- | -------------- |
| `ANI_ID`            | integer  | 番组 bangumi ID   | 主键           |
| `air_date`          | text     | 放送开始日期      | （YYYY-MM-DD） |
| `title`             | text     | 番组原名          |                |
| `title_cn`          | text     | 番组译名          |                |
| `aliases`           | text     | 番组别名          |                |
| `description`       | text     | 番组介绍          |                |
| `episode_count`     | integer  | 番组话数          |                |
| `url_official_site` | text     | 番组官网链接      |                |
| `url_cover`         | text     | 番组封面链接      |                |
| `url_rss`           | text     | 番组 RSS 订阅链接 | 手动维护       |
| `rating_before`     | integer  | 番组观前预期      | 手动维护       |
| `rating_after`      | integer  | 番组观后评分      | 手动维护       |
| `remark`            | text     | 备注              | 手动维护       |

---

## `episode` 表

| 键名              | 数据类型 | 解释            | 备注                                  |
| ----------------- | -------- | --------------- | ------------------------------------- |
| `EPI_ID`          | integer  | 话 bangumi ID   | 主键                                  |
| `ANI_ID`          | integer  | 番组 bangumi ID | 外键                                  |
| `ep`              | integer  | 话索引          |                                       |
| `sort`            | real     | 话排序索引      |                                       |
| `air_date`        | text     | 放送日期        |                                       |
| `duration`        | integer  | 话时长          | 单位（秒）                            |
| `title`           | text     | 话标题          |                                       |
| `title_cn`        | text     | 话标题译名      |                                       |
| `description`     | text     | 单集介绍        |                                       |
| `rating`          | integer  | 话评分          | 手动维护                              |
| `view_datetime`   | text     | 观看日期时间    | 手动维护（yyyy-MM-ddThh:mm:ss+hh:mm） |
| `status_download` | text     | 话下载情况      | 手动维护                              |
| `status_view`     | text     | 话观看情况      | 手动维护                              |
| `remark`          | text     | 备注            | 手动维护                              |

`status_download`: 0 : `未下载`, 1 : `已下载`, 2 : `不下载`

`status_view`: 0 : `未观看`, 1 : `已观看`, 2 : `不观看`

---

特殊说明：

bgm 提供两个字段标记各个话的索引：`ep` 和 `sort`

- `ep`：一个不小于 0 的整数：
  - 当大于 0 时，表示该话是**正片**，该整数值即表示本话是从 1 开始计数的第几话
  - 当等于 0 时，表示该话非正片（可能是特别篇、总集篇、OVA、SP 等）

- `sort`：一个浮点数，用于对话进行直观的编号
  - 无论是不是正片，均可使用该字段进行编号

例如：

《永久的黄昏》的第 0 话：

- `ep` = 1：表示该话是正片的第 1 话
- `sort` = 0：表示该话在正片里编号为 0

《千岁同学》的特别篇：

- `ep` = 0：表示该话非正片
- `sort` = 1：表示该话非正片的编号为 1

《公主的管弦乐》的总集篇：

- `ep` = 0：表示该话非正片
- `sort` = 12.5：表示该话非正片的编号为 12.5

---

## `torrent` 表

| 键名              | 数据类型 | 解释            | 备注     |
| ----------------- | -------- | --------------- | -------- |
| `TOR_HASH`        | text     | 种子 info_hash  | 主键     |
| `ANI_ID`          | integer  | 番组 bangumi ID | 外键     |
| `air_datetime`    | datetime | 发布日期时间    |          |
| `size`            | integer  | 种子大小        | （字节） |
| `url_page`        | text     | 种子页面链接    |          |
| `title`           | text     | 种子标题        |          |
| `subtitle_group`  | text     | 种子字幕组      |          |
| `description`     | text     | 种子描述        |          |
| `status_download` | text     | 种子下载情况    | 手动维护 |
| `remark`          | text     | 备注            | 手动维护 |

`status_download`: 0 : `未下载`, 1 : `已下载`, 2 : `不下载`
