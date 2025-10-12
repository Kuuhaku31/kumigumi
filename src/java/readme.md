# KUMIGUMI

追番计划

## Java 版本

1. `anime.csv`：记录当季度所有在追番剧、记录动画信息
2. `episode.csv`：记录所有动画的所有单集信息
3. `torrent.csv`：保存种子信息

---

## 各个细节

## 字段一览

| 中文键名          | 英文键名                  | 备注                 |
| ----------------- | ------------------------- | -------------------- |
| `番组bangumi链接` | `anime_bangumi_url`       | bangumi 网页链接     |
| `番组RSS订阅链接` | `anime_rss_url`           | rss 订阅源链接       |
| `番组原名`        | `anime_title`             |
| `番组译名`        | `anime_title_cn`          |
| `番组别名`        | `anime_aliases`           |
| `番组话数`        | `anime_episode_count`     |
| `番组官网链接`    | `anime_official_site_url` |
| `番组封面链接`    | `anime_cover_url`         |
| `番组观前评分`    | `anime_pre_view_rating`   |
| `番组观后评分`    | `anime_after_view_rating` |
| `话bangumiURL`    | `episode_bangumi_url`     |
| `话索引`          | `episode_index`           |
| `话标题`          | `episode_title`           |
| `话标题译名`      | `episode_title_cn`        |
| `话时长`          | `episode_duration`        |
| `话下载情况`      | `episode_download_status` | 单集下载情况         |
| `话观看情况`      | `episode_view_status`     | 单集观看情况         |
| `种子下载链接`    | `torrent_download_url`    |
| `种子页面链接`    | `torrent_page_url`        |
| `种子字幕组`      | `torrent_subtitle_group`  |
| `种子标题`        | `torrent_title`           |
| `种子描述`        | `torrent_description`     |
| `种子大小`        | `torrent_size`            | 种子文件大小         |
| `种子大小_字节`   | `torrent_size_bytes`      | 种子文件大小（字节） |
| `种子下载情况`    | `torrent_download_status` | 种子下载情况         |
| `发布日期`        | `air_date`                |
| `备注`            | `note`                    |

---

### 使用 MySQL 数据库

## ✅`anime` 表字段对应

| 英文键名                  | 数据类型 | 中文解释          | 备注     |
| ------------------------- | -------- | ----------------- | -------- |
| `anime_bangumi_id`        | Integer  | 番组 bangumi ID   | 主键     |
| `air_date`                | Date     | 发布日期          |          |
| `anime_title`             | String   | 番组原名          |          |
| `anime_title_cn`          | String   | 番组译名          |          |
| `anime_aliases`           | String   | 番组别名          |          |
| `anime_episode_count`     | Integer  | 番组话数          |          |
| `anime_official_site_url` | String   | 番组官网链接      |          |
| `anime_cover_url`         | String   | 番组封面链接      |          |
| `anime_pre_view_rating`   | Integer  | 番组观前评分      | 手动维护 |
| `anime_after_view_rating` | Integer  | 番组观后评分      | 手动维护 |
| `anime_rss_url`           | String   | 番组 RSS 订阅链接 | 手动维护 |
| `remark`                  | String   | 备注              | 手动维护 |

---

## ✅`episode` 表字段对应

| 英文键名                  | 数据类型 | 中文解释        | 备注       |
| ------------------------- | -------- | --------------- | ---------- |
| `episode_bangumi_id`      | String   | 话 bangumi ID   | 主键       |
| `anime_bangumi_id`        | Integer  | 番组 bangumi ID | 外键       |
| `air_date`                | Date     | 发布日期        |            |
| `episode_index`           | String   | 话索引          |            |
| `episode_title`           | String   | 话标题          |            |
| `episode_title_cn`        | String   | 话标题译名      |            |
| `episode_duration`        | String   | 话时长          | 单位（秒） |
| `episode_rating`          | int      | 话评分          | 新加       |
| `episode_download_status` | String   | 话下载情况      | 手动维护   |
| `episode_view_status`     | String   | 话观看情况      | 手动维护   |
| `remark`                  | String   | 备注            | 手动维护   |

---

## ✅`torrent` 表字段对应

| 英文键名                  | 数据类型 | 中文解释         | 备注     |
| ------------------------- | -------- | ---------------- | -------- |
| `torrent_download_url`    | String   | 种子下载链接     | 主键     |
| `anime_bangumi_id`        | Integer  | 番组 bangumi ID  |          |
| `air_date`                | Date     | 发布日期         |          |
| `torrent_page_url`        | String   | 种子页面链接     |          |
| `torrent_subtitle_group`  | String   | 种子字幕组       |          |
| `torrent_title`           | String   | 种子标题         |          |
| `torrent_description`     | String   | 种子描述         |          |
| `torrent_size_bytes`      | Integer  | 种子大小（字节） |          |
| `torrent_download_status` | String   | 种子下载情况     | 手动维护 |
| `remark`                  | String   | 备注             | 手动维护 |

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
