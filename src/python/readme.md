# KUMIGUMI

追番计划

## 操作手册

<!-- | 功能               | 参数              | 备注 |
| ------------------ | ----------------- | ---- |
| 显示帮助文档       | `-h`              |      |
| 构建配置文件       | `-bc`             |      |
| 更新配置文件       | `-uc`             |      |
| 更新动画信息       | `-ua`             |      |
| 更新种子信息       | `-ut`             |      |
| 更新动画和种子信息 | `-u`              |      |
| 批量下载种子       | `-dt`             |      |
| 设置工作目录       | `--wd <工作目录>` |      | -->

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
2. `episodes.csv`：记录所有动画的所有单集信息
3. `torrents.csv`：保存种子信息

---

## 各个细节

## ✅`anime` 表字段对应

| 中文键名         | 英文键名                   |          |
| ---------------- | -------------------------- | -------- |
| `作品bangumiURL` | `animeBangumiURL`          | 主键     |
| `作品mikanRSS`   | `animeMikananimeRSS`       | 手动维护 |
| `作品原名`       | `animeOriginalTitle`       |
| `作品中文名`     | `animeChineseTitle`        |
| `作品别名`       | `animeAliases`             |
| `作品话数`       | `animeEpisodeCount`        |
| `作品放送开始`   | `animeBroadcastStart`      |
| `作品官方网站`   | `animeOfficialSite`        |
| `作品封面URL`    | `animeCoverImageURL`       |
| `作品分类`       | `animeCategories`          | 手动维护 |
| `作品开播前评分` | `animePreBroadcastRating`  | 手动维护 |
| `作品完播后评分` | `animePostBroadcastRating` | 手动维护 |
| `作品备注`       |                            | 手动维护 |

---

## ✅`episode` 表字段对应

| 中文键名         | 英文键名               |          |
| ---------------- | ---------------------- | -------- |
| `话bangumiURL`   | `episodeBangumiURL`    | 主键     |
| `作品bangumiURL` | `animeBangumiURL`      |
| `话索引`         | `episodeIndex`         |
| `话原标题`       | `episodeOriginalTitle` |
| `话中文标题`     | `episodeChineseTitle`  |
| `话首播时间`     | `episodeAirDate`       |
| `话时长`         | `episodeDuration`      |
| `话备注`         |                        | 手动维护 |

---

## ✅`torrent` 表字段对应

| 中文键名           | 英文键名                  |          |
| ------------------ | ------------------------- | -------- |
| `种子下载链接`     | `torrentMikananimeURL`    | 主键     |
| `作品番组计划网址` | `animeBangumiURL`         |          |
| `种子下载页面网址` | `torrentPageURL`          |          |
| `种子字幕组`       | `torrentSubtitleGroup`    |          |
| `种子发布日期`     | `torrentReleaseDate`      |          |
| `种子标题`         | `torrentTitle`            |          |
| `种子描述`         |                           |          |
| `种子大小`         | `torrentSize`             |          |
| `种子大小_字节`    | `torrentSizeBytes`        |          |
| `备注`             |                           | 手动维护 |
| `话索引`           | `torrentEpisodeIndex`     | 可选     |
| `分辨率`           | `torrentResolution`       | 可选     |
| `片源`             | `torrentSource`           | 可选     |
| `片源类型`         | `torrentSourceType`       | 可选     |
| `视频编码格式`     | `torrentVideoCodec`       | 可选     |
| `音频编码格式`     | `torrentAudioCodec`       | 可选     |
| `字幕语言`         | `torrentSubtitleLanguage` | 可选     |
| `文件格式`         | `torrentFileFormat`       | 可选     |
| `其他标记`         | `torrentTags`             | 可选     |

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
