# KUMIGUMI

追番计划

## 操作手册

```bash
# 启动参数
kumigumi < 模式 > < 参数1 > < 参数2 > ...

kumigumi -list-add < id >
kumigumi -list-add-all < path to ids.txt >

kumigumi -update-anime-info

kumigumi -update-torrent-info

```

| 参数  | 说明         |
| ----- | ------------ |
| `-m`  | 指定模式     |
| `-h`  | 显示帮助     |
| `-wd` | 设置工作目录 |

## 主体部分

```

kumigumi/20XX.XX/
├── anime.csv
├── episodes.csv
├── torrents.csv
└── kumigumi.json

```

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

## ✅`anime` 表字段对应

| 中文键名          | 英文键名                  | 备注     |
| ----------------- | ------------------------- | -------- |
| `番组bangumi链接` | `anime_bangumi_url`       | 主键     |
| `发布日期`        | `air_date`                |          |
| `番组原名`        | `anime_title`             |          |
| `番组译名`        | `anime_title_cn`          |          |
| `番组别名`        | `anime_aliases`           |          |
| `番组话数`        | `anime_episode_count`     |          |
| `番组官网链接`    | `anime_official_site_url` |          |
| `番组封面链接`    | `anime_cover_url`         |          |
| `番组观前评分`    | `anime_pre_view_rating`   | 手动维护 |
| `番组观后评分`    | `anime_after_view_rating` | 手动维护 |
| `番组RSS订阅链接` | `anime_rss_url`           | 手动维护 |
| `备注`            | `note`                    | 手动维护 |

---

## ✅`episode` 表字段对应

| 中文键名          | 英文键名                  |          |
| ----------------- | ------------------------- | -------- |
| `话bangumiURL`    | `episode_bangumi_url`     | 主键     |
| `番组bangumi链接` | `anime_bangumi_url`       |          |
| `发布日期`        | `air_date`                |          |
| `话索引`          | `episode_index`           |          |
| `话标题`          | `episode_title`           |          |
| `话标题译名`      | `episode_title_cn`        |          |
| `话时长`          | `episode_duration`        |          |
| `话下载情况`      | `episode_download_status` | 手动维护 |
| `话观看情况`      | `episode_view_status`     | 手动维护 |
| `备注`            | `note`                    | 手动维护 |

---

## ✅`torrent` 表字段对应

| 中文键名          | 英文键名                  |          |
| ----------------- | ------------------------- | -------- |
| `种子下载链接`    | `torrent_download_url`    | 主键     |
| `番组bangumi链接` | `anime_bangumi_url`       |          |
| `发布日期`        | `air_date`                |          |
| `种子页面链接`    | `torrent_page_url`        |          |
| `种子字幕组`      | `torrent_subtitle_group`  |          |
| `种子标题`        | `torrent_title`           |          |
| `种子描述`        | `torrent_description`     |          |
| `种子大小`        | `torrent_size`            |          |
| `种子大小_字节`   | `torrent_size_bytes`      |          |
| `种子下载情况`    | `torrent_download_status` | 手动维护 |
| `备注`            | `note`                    | 手动维护 |

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
