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

| 模式         | 参数  | 说明                                                                     |
| ------------ | ----- | ------------------------------------------------------------------------ |
| 构建配置文件 | `-bc` | 利用`ids.txt`文件构建配置文件`kumigumi.json`，包含所有番剧的番组计划网址 |

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

| 中文键名           | 英文键名              | SQLite 类型 | 备注 |
| ------------------ | --------------------- | ----------- | ---- |
| `作品番组计划网址` | `animeBangumiURL`     | TEXT        | 主要 |
| `作品蜜柑计划网址` | `animeMikananimeURL`  | TEXT        | 储存 |
| `作品原名`         | `animeOriginalTitle`  | TEXT        | 更新 |
| `作品中文名`       | `animeChineseTitle`   | TEXT        | 更新 |
| `作品别名`         | `animeAliases`        | TEXT        | 更新 |
| `作品分类`         | `animeCategories`     | TEXT        | 储存 |
| `作品话数`         | `animeEpisodeCount`   | TEXT        | 更新 |
| `作品放送开始`     | `animeBroadcastStart` | TEXT        | 更新 |
| `作品放送星期`     | `animeBroadcastDay`   | TEXT        | 更新 |
| `作品官方网址`     | `animeOfficialSite`   | TEXT        | 更新 |
| `作品封面`         | `animeCoverImage`     | TEXT        | 更新 |

---

## ✅`episodes` 表字段对应

| 中文键名           | 英文键名               | SQLite 类型 | 备注 |
| ------------------ | ---------------------- | ----------- | ---- |
| `话番组计划网址`   | `episodeBangumiURL`    | TEXT        | 主要 |
| `作品番组计划网址` | `animeBangumiURL`      | TEXT        | 标记 |
| `话索引`           | `episodeIndex`         | TEXT        | 更新 |
| `话原标题`         | `episodeOriginalTitle` | TEXT        | 更新 |
| `话中文标题`       | `episodeChineseTitle`  | TEXT        | 更新 |
| `话首播时间`       | `episodeAirDate`       | TEXT        | 更新 |
| `话时长`           | `episodeDuration`      | TEXT        | 更新 |
| `话是否下载`       | `episodeIsDownloaded`  | TEXT        | 储存 |
| `话是否观看`       | `episodeIsWatched`     | TEXT        | 储存 |

---

## ✅`torrents` 表字段对应

| 中文键名           | 英文键名                  | SQLite 类型 | 备注 |
| ------------------ | ------------------------- | ----------- | ---- |
| `种子下载链接`     | `torrentMikananimeURL`    | TEXT        | 主要 |
| `作品番组计划网址` | `animeBangumiURL`         | TEXT        | 标记 |
| `种子字幕组`       | `torrentSubtitleGroup`    | TEXT        | 更新 |
| `种子发布日期`     | `torrentReleaseDate`      | TEXT        | 更新 |
| `种子标题`         | `torrentTitle`            | TEXT        | 更新 |
| `种子大小`         | `torrentSize`             | TEXT        | 更新 |
| `种子大小_字节`    | `torrentSizeBytes`        | TEXT        | 更新 |
| `话索引`           | `torrentEpisodeIndex`     | TEXT        | 可选 |
| `分辨率`           | `torrentResolution`       | TEXT        | 可选 |
| `片源`             | `torrentSource`           | TEXT        | 可选 |
| `片源类型`         | `torrentSourceType`       | TEXT        | 可选 |
| `视频编码格式`     | `torrentVideoCodec`       | TEXT        | 可选 |
| `音频编码格式`     | `torrentAudioCodec`       | TEXT        | 可选 |
| `字幕语言`         | `torrentSubtitleLanguage` | TEXT        | 可选 |
| `文件格式`         | `torrentFileFormat`       | TEXT        | 可选 |
| `其他标记`         | `torrentTags`             | TEXT        | 可选 |
| `种子下载页面网址` | `torrentPageURL`          | TEXT        | 更新 |
| `种子是否下载`     | `torrentIsDownloaded`     | TEXT        | 储存 |

---
