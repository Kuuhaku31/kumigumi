# KUMIGUMI

追番计划

## 主体部分

1. **anime**：记录当季度所有在追番剧、记录动画信息
2. **episodes**：记录所有动画的所有单集信息
3. **torrents**：保存种子信息

---

## 各个细节

## ✅`anime` 表字段对应

| 中文键名           | 英文键名         | SQLite 类型 | 备注 |
| ------------------ | ---------------- | ----------- | ---- |
| `作品番组计划网址` | `bangumiURL`     | TEXT        | 主要 |
| `作品蜜柑计划网址` | `mikananimeURL`  | TEXT        | 储存 |
| `作品原名`         | `originalTitle`  | TEXT        | 更新 |
| `作品中文名`       | `chineseTitle`   | TEXT        | 更新 |
| `作品别名`         | `aliases`        | TEXT        | 更新 |
| `作品分类`         | `categories`     | TEXT        | 储存 |
| `作品话数`         | `episodeCount`   | INTEGER     | 更新 |
| `作品放送开始`     | `broadcastStart` | TEXT        | 更新 |
| `作品放送星期`     | `broadcastDay`   | INTEGER     | 更新 |
| `作品官方网址`     | `officialSite`   | TEXT        | 更新 |
| `作品封面`         | `coverImage`     | TEXT        | 更新 |

---

## ✅`episodes` 表字段对应

| 中文键名           | 英文键名        | SQLite 类型 | 备注 |
| ------------------ | --------------- | ----------- | ---- |
| `话番组计划网址`   | `episodeURL`    | TEXT        | 主要 |
| `作品番组计划网址` | `bangumiURL`    | TEXT        | 标记 |
| `话索引`           | `episodeIndex`  | TEXT        | 更新 |
| `话原标题`         | `originalTitle` | TEXT        | 更新 |
| `话中文标题`       | `chineseTitle`  | TEXT        | 更新 |
| `话首播时间`       | `airDate`       | TEXT        | 更新 |
| `话时长`           | `duration`      | INTEGER     | 更新 |
| `话是否下载`       | `isDownloaded`  | INTEGER     | 储存 |
| `话是否观看`       | `isWatched`     | INTEGER     | 储存 |

---

## ✅`torrents` 表字段对应

| 中文键名           | 英文键名           | SQLite 类型 | 备注 |
| ------------------ | ------------------ | ----------- | ---- |
| `种子下载链接`     | `torrentURL`       | TEXT        | 主要 |
| `作品番组计划网址` | `bangumiURL`       | TEXT        | 标记 |
| `种子字幕组`       | `subtitleGroup`    | TEXT        | 更新 |
| `种子发布日期`     | `releaseDate`      | TEXT        | 更新 |
| `种子标题`         | `torrentTitle`     | TEXT        | 更新 |
| `种子大小`         | `size`             | TEXT        | 更新 |
| `种子大小（字节）` | `sizeBytes`        | INTEGER     | 更新 |
| `话索引`           | `episodeIndex`     | TEXT        | 可选 |
| `分辨率`           | `resolution`       | TEXT        | 可选 |
| `片源`             | `source`           | TEXT        | 可选 |
| `片源类型`         | `sourceType`       | TEXT        | 可选 |
| `视频编码格式`     | `videoCodec`       | TEXT        | 可选 |
| `音频编码格式`     | `audioCodec`       | TEXT        | 可选 |
| `字幕语言`         | `subtitleLanguage` | TEXT        | 可选 |
| `文件格式`         | `fileFormat`       | TEXT        | 可选 |
| `其他标记`         | `tags`             | TEXT        | 可选 |
| `种子下载页面网址` | `pageURL`          | TEXT        | 更新 |
| `种子是否下载`     | `isDownloaded`     | INTEGER     | 储存 |

---
