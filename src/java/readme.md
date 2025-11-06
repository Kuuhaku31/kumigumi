# KUMIGUMI

追番计划

## Java 版本

1. `anime.csv`：记录当季度所有在追番剧、记录动画信息
2. `episode.csv`：记录所有动画的所有单集信息
3. `torrent.csv`：保存种子信息

---

```

"D:/OneDrive/kumigumi.xlsx"

```

---

## 实现的功能

### 1. 从远程获取信息并保存到数据库

参数：

```txt

kumigumi fetch -a<Bangumi ID> [-r<RSS link> ...], [...]

```

例：

```bash

kumigumi fetch -a507634 -rhttps://mikanani.me/RSS/Bangumi?bangumiId=3774 -a539395 -a508958 -rhttps://mikanani.me/RSS/Bangumi?bangumiId=3783

```

功能说明：

提供 Bangumi ID 和 RSS 订阅链接（可选），程序会从远程获取该番组的信息并保存到数据库中

注意：对应的 RSS 紧跟在 Bangumi ID 后面

### 2. 从 Excel 文件获取信息并保存到数据库

参数：

```txt

kumigumi import <Excel file path>

```

### 3. 从 Excel 文件读取更新列表，并依据该列表执行 (1)

参数：

```txt

kumigumi fetch_excel <Excel file path>

```

### 4. 同时执行 (2) 和 (3)

参数：

```txt

kumigumi all <Excel file path>

```

---

## 各个细节

anime - ani

episode - eps

torrent - tor

---

### 使用 MySQL 数据库

## ✅`anime` 表字段对应

| 英文键名            | 数据类型 | 中文解释          | 备注     |
| ------------------- | -------- | ----------------- | -------- |
| `ANI_ID`            | Integer  | 番组 bangumi ID   | 主键     |
| `air_date`          | Date     | 放送开始日期      |          |
| `title`             | String   | 番组原名          |          |
| `title_cn`          | String   | 番组译名          |          |
| `aliases`           | String   | 番组别名          |          |
| `episode_count`     | Integer  | 番组话数          |          |
| `url_official_site` | String   | 番组官网链接      |          |
| `url_cover`         | String   | 番组封面链接      |          |
| `url_rss`           | String   | 番组 RSS 订阅链接 | 手动维护 |
| `rating_before`     | Integer  | 番组观前预期      | 手动维护 |
| `rating_after`      | Integer  | 番组观后评分      | 手动维护 |
| `remark`            | String   | 备注              | 手动维护 |

---

## ✅`episode` 表字段对应

| 英文键名          | 数据类型 | 中文解释        | 备注       |
| ----------------- | -------- | --------------- | ---------- |
| `EPI_ID`          | Integer  | 话 bangumi ID   | 主键       |
| `ANI_ID`          | Integer  | 番组 bangumi ID | 外键       |
| `air_date`        | Date     | 放送日期        |            |
| `duration`        | Integer  | 话时长          | 单位（秒） |
| `index`           | String   | 话索引          |            |
| `title`           | String   | 话标题          |            |
| `title_cn`        | String   | 话标题译名      |            |
| `description`     | String   | 单集介绍        |            |
| `status_download` | Enum     | 话下载情况      | 手动维护   |
| `status_view`     | Enum     | 话观看情况      | 手动维护   |
| `rating`          | int      | 话评分          | 手动维护   |
| `remark`          | String   | 备注            | 手动维护   |

`status_download`: 0 : `未下载`, 1 : `已下载`, 2 : `不下载`

`status_view`: 0 : `未观看`, 1 : `已观看`, 2 : `不观看`

---

## ✅`torrent` 表字段对应

| 英文键名          | 数据类型 | 中文解释         | 备注     |
| ----------------- | -------- | ---------------- | -------- |
| `TOR_URL`         | String   | 种子下载链接     | 主键     |
| `ANI_ID`          | Integer  | 番组 bangumi ID  | 外键     |
| `air_datetime`    | DateTime | 发布日期时间     |          |
| `size`            | Integer  | 种子大小（字节） |          |
| `url_page`        | String   | 种子页面链接     |          |
| `title`           | String   | 种子标题         |          |
| `subtitle_group`  | String   | 种子字幕组       |          |
| `description`     | text     | 种子描述         |          |
| `status_download` | String   | 种子下载情况     | 手动维护 |
| `remark`          | String   | 备注             | 手动维护 |

`status_download`: 0 : `未下载`, 1 : `已下载`, 2 : `不下载`

---

#### 可选字段

| 中文键名       | 英文键名                  | 备注 |
| -------------- | ------------------------- | ---- |
| `话索引`       | `torrentEpisodeIndex`     | 可选 |
| `分辨率`       | `torrentResolution`       | 可选 |
| `片源`         | `torrentSource`           | 可选 |
| `片源类型`     | `torrentSourceType`       | 可选 |
| `视频编码格式` | `torrentVideoCodec`       | 可选 |
| `音频编码格式` | `torrentAudioCodec`       | 可选 |
| `字幕语言`     | `torrentSubtitleLanguage` | 可选 |
| `文件格式`     | `torrentFileFormat`       | 可选 |
| `其他标记`     | `torrentTags`             | 可选 |

---

```xml
 <item>
      <guid isPermaLink="false">[喵萌奶茶屋&amp;LoliHouse] 群花绽放，仿如修罗 / Hana wa Saku Shura no Gotoku - 11 [WebRip 1080p HEVC-10bit AAC][简繁日内封字幕]</guid>
      <link>https://mikanani.me/Home/Episode/9d22370519e85dde9c9521a289812d30b7b0321b</link>
      <title>[喵萌奶茶屋&amp;LoliHouse] 群花绽放，仿如修罗 / Hana wa Saku Shura no Gotoku - 11 [WebRip 1080p HEVC-10bit AAC][简繁日内封字幕]</title>
      <description>[喵萌奶茶屋&amp;LoliHouse] 群花绽放，仿如修罗 / Hana wa Saku Shura no Gotoku - 11 [WebRip 1080p HEVC-10bit AAC][简繁日内封字幕][364.66 MB]</description>

      <torrent xmlns="https://mikanani.me/0.1/">
        <link>https://mikanani.me/Home/Episode/9d22370519e85dde9c9521a289812d30b7b0321b</link>
        <contentLength>382373728</contentLength>
        <pubDate>2025-03-23T10:02:52.301</pubDate>
      </torrent>

      <enclosure type="application/x-bittorrent" length="382373728" url="https://mikanani.me/Download/20250323/9d22370519e85dde9c9521a289812d30b7b0321b.torrent" />
    </item>
```

```python

# 种子信息格式：
dict:{
    "作品bangumiURL" : "...",
    "字幕组"         : "喵萌奶茶屋&amp;LoliHouse",
    "下载链接"       : "https://mikanani.me/Download/20250323/9d22370519e85dde9c9521a289812d30b7b0321b.torrent",
    "页面链接"       : "https://mikanani.me/Home/Episode/9d22370519e85dde9c9521a289812d30b7b0321b",
    "种子标题"       : "[喵萌奶茶屋&amp;LoliHouse] 群花绽放，仿如修罗 / Hana wa Saku Shura no Gotoku - 11 [WebRip 1080p HEVC-10bit AAC][简繁日内封字幕]",
    "种子描述"       : "[喵萌奶茶屋&amp;LoliHouse] 群花绽放，仿如修罗 / Hana wa Saku Shura no Gotoku - 11 [WebRip 1080p HEVC-10bit AAC][简繁日内封字幕]",
    "种子日期"       : "2025-03-23T10:02:52.301",
    "种子大小_字节"  :  382373728 , # int 表示字节数
    "种子大小"       :  "364.66 MB", # 字符串表示大小
}

```
