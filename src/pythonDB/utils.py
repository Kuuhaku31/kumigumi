# 请求html页面

import csv  # noqa: E402
import json  # noqa: E402
import urllib.request  # noqa: E402

import fake_useragent


def 获取HTML内容(url: str) -> str:

    # 随机请求头池
    headers = {
        "User-Agent": fake_useragent.UserAgent().random,
        "Connection": "keep-alive",
    }

    res = urllib.request.urlopen(urllib.request.Request(url, headers=headers))
    if res.status != 200:
        return None
    else:
        return res.read().decode("utf-8")


def 保存csv文件(file: str, headers: list, data: list):
    with open(file, "w+", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(headers)  # 写入表头
        for info in data:
            new_row = []
            for header in headers:
                elem = ""
                if header in info:
                    elem = info.get(header)
                new_row.append(elem)
            writer.writerow(new_row)  # 写入数据

    print(f"保存成功：{file}")


def 读取csv文件(file_path):
    data = []
    with open(file_path, mode="r", encoding="utf-8") as file:
        reader = csv.DictReader(file)  # 使用 DictReader 读取为字典形式
        for row in reader:
            data.append(row)  # 每一行是一个字典

    print(f"读取成功：{file_path}")
    return data


def 获取json文件(file: str) -> dict:
    data = None
    with open(file, encoding="utf-8") as f:
        data = json.load(f)

    print(f"读取成功：{file}")
    return data


# 变量命名


# ## ✅`anime` 表字段对应

# | 中文键名           | 英文键名              | SQLite 类型 | 备注 |
# | ------------------ | --------------------- | ----------- | ---- |
# | `作品番组计划网址` | `animeBangumiURL`     | TEXT        | 主要 |
# | `作品蜜柑计划网址` | `animeMikananimeURL`  | TEXT        | 储存 |
# | `作品原名`         | `animeOriginalTitle`  | TEXT        | 更新 |
# | `作品中文名`       | `animeChineseTitle`   | TEXT        | 更新 |
# | `作品别名`         | `animeAliases`        | TEXT        | 更新 |
# | `作品分类`         | `animeCategories`     | TEXT        | 储存 |
# | `作品话数`         | `animeEpisodeCount`   | INTEGER     | 更新 |
# | `作品放送开始`     | `animeBroadcastStart` | TEXT        | 更新 |
# | `作品放送星期`     | `animeBroadcastDay`   | INTEGER     | 更新 |
# | `作品官方网址`     | `animeOfficialSite`   | TEXT        | 更新 |
# | `作品封面`         | `animeCoverImage`     | TEXT        | 更新 |

# ---

# ## ✅`episodes` 表字段对应

# | 中文键名           | 英文键名               | SQLite 类型 | 备注 |
# | ------------------ | ---------------------- | ----------- | ---- |
# | `话番组计划网址`   | `episodeBangumiURL`    | TEXT        | 主要 |
# | `作品番组计划网址` | `animeBangumiURL`      | TEXT        | 标记 |
# | `话索引`           | `episodeIndex`         | TEXT        | 更新 |
# | `话原标题`         | `episodeOriginalTitle` | TEXT        | 更新 |
# | `话中文标题`       | `episodeChineseTitle`  | TEXT        | 更新 |
# | `话首播时间`       | `episodeAirDate`       | TEXT        | 更新 |
# | `话时长`           | `episodeDuration`      | INTEGER     | 更新 |
# | `话是否下载`       | `episodeIsDownloaded`  | INTEGER     | 储存 |
# | `话是否观看`       | `episodeIsWatched`     | INTEGER     | 储存 |

# ---

# ## ✅`torrents` 表字段对应

# | 中文键名           | 英文键名                  | SQLite 类型 | 备注 |
# | ------------------ | ------------------------- | ----------- | ---- |
# | `种子下载链接`     | `torrentMikananimeURL`    | TEXT        | 主要 |
# | `作品番组计划网址` | `animeBangumiURL`         | TEXT        | 标记 |
# | `种子字幕组`       | `torrentSubtitleGroup`    | TEXT        | 更新 |
# | `种子发布日期`     | `torrentReleaseDate`      | TEXT        | 更新 |
# | `种子标题`         | `torrentTitle`            | TEXT        | 更新 |
# | `种子大小`         | `torrentSize`             | TEXT        | 更新 |
# | `种子大小_字节`    | `torrentSizeBytes`        | INTEGER     | 更新 |
# | `话索引`           | `torrentEpisodeIndex`     | TEXT        | 可选 |
# | `分辨率`           | `torrentResolution`       | TEXT        | 可选 |
# | `片源`             | `torrentSource`           | TEXT        | 可选 |
# | `片源类型`         | `torrentSourceType`       | TEXT        | 可选 |
# | `视频编码格式`     | `torrentVideoCodec`       | TEXT        | 可选 |
# | `音频编码格式`     | `torrentAudioCodec`       | TEXT        | 可选 |
# | `字幕语言`         | `torrentSubtitleLanguage` | TEXT        | 可选 |
# | `文件格式`         | `torrentFileFormat`       | TEXT        | 可选 |
# | `其他标记`         | `torrentTags`             | TEXT        | 可选 |
# | `种子下载页面网址` | `torrentPageURL`          | TEXT        | 更新 |
# | `种子是否下载`     | `torrentIsDownloaded`     | INTEGER     | 储存 |


作品番组计划网址 = "animeBangumiURL"
作品蜜柑计划网址 = "animeMikananimeURL"
作品原名 = "animeOriginalTitle"
作品中文名 = "animeChineseTitle"
作品别名 = "animeAliases"
作品分类 = "animeCategories"
作品话数 = "animeEpisodeCount"
作品放送开始 = "animeBroadcastStart"
作品放送星期 = "animeBroadcastDay"
作品官方网址 = "animeOfficialSite"
作品封面 = "animeCoverImage"
话番组计划网址 = "episodeBangumiURL"
话索引 = "episodeIndex"
话原标题 = "episodeOriginalTitle"
话中文标题 = "episodeChineseTitle"
话首播时间 = "episodeAirDate"
话时长 = "episodeDuration"
话是否下载 = "episodeIsDownloaded"
话是否观看 = "episodeIsWatched"
种子下载链接 = "torrentMikananimeURL"
种子字幕组 = "torrentSubtitleGroup"
种子发布日期 = "torrentReleaseDate"
种子标题 = "torrentTitle"
种子大小 = "torrentSize"
种子大小_字节 = "torrentSizeBytes"
话索引 = "torrentEpisodeIndex"
分辨率 = "torrentResolution"
片源 = "torrentSource"
片源类型 = "torrentSourceType"
视频编码格式 = "torrentVideoCodec"
音频编码格式 = "torrentAudioCodec"
字幕语言 = "torrentSubtitleLanguage"
文件格式 = "torrentFileFormat"
其他标记 = "torrentTags"
种子下载页面网址 = "torrentPageURL"
种子是否下载 = "torrentIsDownloaded"


anime表表头 = [
    作品番组计划网址,
    作品蜜柑计划网址,
    作品原名,
    作品中文名,
    作品别名,
    作品分类,
    作品话数,
    作品放送开始,
    作品放送星期,
    作品官方网址,
    作品封面,
]

episodes表表头 = [
    话番组计划网址,
    作品番组计划网址,
    话索引,
    话原标题,
    话中文标题,
    话首播时间,
    话时长,
    话是否下载,
    话是否观看,
]

torrents表表头 = [
    种子下载链接,
    作品番组计划网址,
    种子字幕组,
    种子发布日期,
    种子标题,
    种子大小,
    种子大小_字节,
    话索引,
    分辨率,
    片源,
    片源类型,
    视频编码格式,
    音频编码格式,
    字幕语言,
    文件格式,
    其他标记,
    种子下载页面网址,
    种子是否下载,
]
