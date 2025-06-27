# headers.py


from typing import Tuple

字段字典: dict = {
    "番组bangumi链接": "anime_bangumi_URL",
    "番组RSS订阅链接": "anime_RSS_URL",
    "番组原名": "anime_title",
    "番组译名": "anime_title_cn",
    "番组别名": "anime_aliases",
    "番组话数": "anime_episode_count",
    "番组官网链接": "anime_official_site_url",
    "番组封面链接": "anime_cover_url",
    "番组观前评分": "anime_pre_view_rating",
    "番组观后评分": "anime_after_view_rating",
    "话bangumiURL": "episode_bangumi_URL",
    "话索引": "episode_index",
    "话标题": "episode_title",
    "话标题译名": "episode_title_cn",
    "话时长": "episode_duration",
    "话下载情况": "episode_download_status",
    "话观看情况": "episode_view_status",
    "种子下载链接": "torrent_download_URL",
    "种子页面链接": "torrent_page_URL",
    "种子字幕组": "torrent_subtitle_group",
    "种子标题": "torrent_title",
    "种子描述": "torrent_description",
    "种子大小": "torrent_size",
    "种子大小_字节": "torrent_size_bytes",
    "种子下载情况": "torrent_download_status",
    "发布日期": "air_date",
    "备注": "remark",
}


"""

## ✅`anime` 表字段对应

| 中文键名          | 英文键名                  | 备注     |
| ----------------- | ------------------------- | -------- |
| `番组bangumi链接` | `anime_bangumi_URL`       | 主键     |
| `发布日期`        | `air_date`                |          |
| `番组原名`        | `anime_title`             |          |
| `番组译名`        | `anime_title_cn`          |          |
| `番组别名`        | `anime_aliases`           |          |
| `番组话数`        | `anime_episode_count`     |          |
| `番组官网链接`    | `anime_official_site_url` |          |
| `番组封面链接`    | `anime_cover_url`         |          |
| `番组观前评分`    | `anime_pre_view_rating`   | 手动维护 |
| `番组观后评分`    | `anime_after_view_rating` | 手动维护 |
| `番组RSS订阅链接` | `anime_RSS_URL`           | 手动维护 |
| `备注`            | `remark`                  | 手动维护 |

---

## ✅`episode` 表字段对应

| 中文键名          | 英文键名                  |          |
| ----------------- | ------------------------- | -------- |
| `话bangumiURL`    | `episode_bangumi_URL`     | 主键     |
| `番组bangumi链接` | `anime_bangumi_URL`       |          |
| `发布日期`        | `air_date`                |          |
| `话索引`          | `episode_index`           |          |
| `话标题`          | `episode_title`           |          |
| `话标题译名`      | `episode_title_cn`        |          |
| `话时长`          | `episode_duration`        |          |
| `话下载情况`      | `episode_download_status` | 手动维护 |
| `话观看情况`      | `episode_view_status`     | 手动维护 |
| `备注`            | `remark`                  | 手动维护 |

---

## ✅`torrent` 表字段对应

| 中文键名          | 英文键名                  |          |
| ----------------- | ------------------------- | -------- |
| `种子下载链接`    | `torrentMikananimeURL`    | 主键     |
| `番组bangumi链接` | `anime_RSS_URL`           |          |
| `发布日期`        | `air_date`                |          |
| `种子下载链接`    | `torrent_download_URL`    |          |
| `种子页面链接`    | `torrent_page_URL`        |          |
| `种子字幕组`      | `torrent_subtitle_group`  |          |
| `种子标题`        | `torrent_title`           |          |
| `种子描述`        | `torrent_description`     |          |
| `种子大小`        | `torrent_size`            |          |
| `种子大小_字节`   | `torrent_size_bytes`      |          |
| `种子下载情况`    | `torrent_download_status` | 手动维护 |
| `备注`            | `remark`                  | 手动维护 |

"""


def 预处理表头(表头: list[str]) -> Tuple[str, list[str], list[str], list[str], str, list[str], list[str], list[str]]:

    主键 = [key.split("*")[0] for key in 表头 if key.split("*")[1] == "pk"][0]
    自动更新 = [key.split("*")[0] for key in 表头 if key.split("*")[1] == "a"]
    手动维护 = [key.split("*")[0] for key in 表头 if key.split("*")[1] == "m"]
    数据库 = [key.split("*")[0] for key in 表头]

    主键_en = 字段字典.get(主键, 主键)
    自动更新_en = [字段字典.get(h, h) for h in 自动更新]
    手动维护_en = [字段字典.get(h, h) for h in 手动维护]
    数据库_en = [字段字典.get(h, h) for h in 数据库]

    return (主键, 自动更新, 手动维护, 数据库, 主键_en, 自动更新_en, 手动维护_en, 数据库_en)


# 表头

番组表头_数据库_src: list[str] = [
    "番组bangumi链接*pk",  # 主键
    "发布日期*a",
    "番组原名*a",
    "番组译名*a",
    "番组别名*a",
    "番组话数*a",
    "番组官网链接*a",
    "番组封面链接*a",
    "番组观前评分*m",  # 手动维护
    "番组观后评分*m",  # 手动维护
    "番组RSS订阅链接*m",  # 手动维护
    "备注*m",  # 手动维护
]
(
    番组表头_主键,
    番组表头_自动更新,
    番组表头_手动维护,
    番组表头_数据库,
    番组表头_主键_en,
    番组表头_自动更新_en,
    番组表头_手动维护_en,
    番组表头_数据库_en,
) = 预处理表头(番组表头_数据库_src)


单集表头_数据库_src: list[str] = [
    "话bangumiURL*pk",  # 主键
    "番组bangumi链接*a",
    "发布日期*a",
    "话索引*a",
    "话标题*a",
    "话标题译名*a",
    "话时长*a",
    "话下载情况*m",  # 手动维护
    "话观看情况*m",  # 手动维护
    "备注*m",  # 手动维护
]
(
    单集表头_主键,
    单集表头_自动更新,
    单集表头_手动维护,
    单集表头_数据库,
    单集表头_主键_en,
    单集表头_自动更新_en,
    单集表头_手动维护_en,
    单集表头_数据库_en,
) = 预处理表头(单集表头_数据库_src)


种子表头_数据库_src: list[str] = [
    "种子下载链接*pk",  # 主键
    "番组bangumi链接*a",
    "发布日期*a",
    "种子下载链接*a",
    "种子页面链接*a",
    "种子字幕组*a",
    "种子标题*a",
    "种子描述*a",
    "种子大小*a",
    "种子大小_字节*a",
    "种子下载情况*m",  # 手动维护
    "备注*m",  # 手动维护
]
(
    种子表头_主键,
    种子表头_自动更新,
    种子表头_手动维护,
    种子表头_数据库,
    种子表头_主键_en,
    种子表头_自动更新_en,
    种子表头_手动维护_en,
    种子表头_数据库_en,
) = 预处理表头(种子表头_数据库_src)


if __name__ == "__main__":
    print("各个表头的处理结果：")
    print("番组表头_主键:", 番组表头_主键)
    print("番组表头_自动更新:", 番组表头_自动更新)
    print("番组表头_手动维护:", 番组表头_手动维护)
    print("番组表头_数据库:", 番组表头_数据库)
    print("番组表头_主键_en:", 番组表头_主键_en)
    print("番组表头_自动更新_en:", 番组表头_自动更新_en)
    print("番组表头_手动维护_en:", 番组表头_手动维护_en)
    print("番组表头_数据库_en:", 番组表头_数据库_en)
    print("单集表头_主键:", 单集表头_主键)
    print("单集表头_自动更新:", 单集表头_自动更新)
    print("单集表头_手动维护:", 单集表头_手动维护)
    print("单集表头_数据库:", 单集表头_数据库)
    print("单集表头_主键_en:", 单集表头_主键_en)
    print("单集表头_自动更新_en:", 单集表头_自动更新_en)
    print("单集表头_手动维护_en:", 单集表头_手动维护_en)
    print("单集表头_数据库_en:", 单集表头_数据库_en)
    print("种子表头_主键:", 种子表头_主键)
    print("种子表头_自动更新:", 种子表头_自动更新)
    print("种子表头_手动维护:", 种子表头_手动维护)
    print("种子表头_数据库:", 种子表头_数据库)
    print("种子表头_主键_en:", 种子表头_主键_en)
    print("种子表头_自动更新_en:", 种子表头_自动更新_en)
    print("种子表头_手动维护_en:", 种子表头_手动维护_en)
    print("种子表头_数据库_en:", 种子表头_数据库_en)
