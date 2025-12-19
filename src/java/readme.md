# KUMIGUMI

追番计划

## Java 版本

需配合 `D:/OneDrive/kumigumi.xlsx` 使用

---

## 各个细节

anime - ani

episode - eps

torrent - tor

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
