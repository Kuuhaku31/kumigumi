# bangumi.py


from typing import Tuple

from lxml import etree

"""

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

"""


# 解析番剧信息
# 参数：
#   html_str: str - HTML字符串
# 返回：
#   动画信息列表: dict
#   单集信息列表: list[dict]
def 解析BangumiHTML_str(html_str: str) -> Tuple[dict, list[dict]]:
    tree = etree.HTML(html_str)

    # 初始化字典
    anime_infos = {}
    anime_infos["番组bangumi链接"] = "https://bangumi.tv" + tree.xpath("//h1/a/@href")[0]
    anime_infos["番组原名"] = tree.xpath("//h1/a/text()")[0]
    anime_infos["番组译名"] = tree.xpath("//h1/a/text()")[0]
    anime_infos["番组话数"] = "*"

    # 查找 ul#infobox 内的所有 li
    infobox_lis = tree.xpath('//ul[@id="infobox"]/li')
    for li in infobox_lis:
        # 获取 <span class="tip"> 标签的文本（键）
        key_elem = li.xpath('.//span[@class="tip"]/text()')
        if not key_elem:
            continue

        # 获取 <span class="tip"> 标签的文本（键）
        key = key_elem[0].strip().replace(":", "")  # 去除冒号

        # 替换键
        key = {
            "中文名": "番组原名",
            "别名": "番组别名",
            "话数": "番组话数",
            "放送开始": "发布日期",
            "上映年度": "发布日期",
        }.get(key, key)

        # 获取 <a> 标签内的文本（如果有）或者 li 内部的文本内容（去掉 <span class="tip">）
        value = None
        links = li.xpath(".//a/text()")
        sub_ul = li.xpath(".//ul/li/text()")
        if links:  # 获取 <a> 标签内的文本（如果有）
            if key == "官方网站":  # 如果key是官方网站
                key = "番组官网链接"
                value = li.xpath(".//a/@href")[0]
            else:
                value = " / ".join(links)

        elif sub_ul:  # 获取 <ul> 标签内的文本（如果有）
            value = " / ".join(sub_ul)

        else:  # 获取 li 内部的文本内容（去掉 <span class="tip">）
            value = "".join(li.xpath(".//text()")).strip().replace(key_elem[0], "").strip()

        # 保存到字典
        anime_infos[key] = value

    # 获取封面
    cover = tree.xpath('//img[@class="cover"]/@src')
    if cover:
        anime_infos["番组封面链接"] = "https:" + cover[0]

    # 获取单集信息
    # 话索引;话标题;中文标题;首播;时长;
    prg_list = []
    for prg in tree.xpath('//ul[@class="prg_list"]/li'):

        # 获取a标签
        tag_a = prg.xpath(".//a")
        if not tag_a:
            continue
        tag_a = tag_a[0]

        # 初始化字典
        new_prg = {}

        # 通过a标签获取信息
        new_prg["话bangumiURL"] = "https://bangumi.tv" + tag_a.xpath("@href")[0]
        new_prg["番组bangumi链接"] = anime_infos["番组bangumi链接"]
        new_prg["话标题"] = anime_infos["番组原名"]
        new_prg["话标题译名"] = anime_infos["番组译名"]
        new_prg["发布日期"] = anime_infos["发布日期"]
        new_prg["话索引"] = tag_a.xpath("text()")[0]
        new_prg["话标题"] = tag_a.xpath("@title")[0].split(" ", 1)[1]  # 按第一个空格分割，取第二部分

        # 通过id获取span标签
        span = tree.xpath(f'//div[@id="{prg.xpath(".//a/@rel")[0].replace("#", "")}"]/span')
        tip_text = (
            etree.tostring(span[0], encoding="utf-8")
            .decode("utf-8")
            .replace('<span class="tip">', "")
            .replace("<span>", "")
            .replace("</span>", "")
            .replace(" ", "")
            .replace("\n", "")
            .strip()
        )

        # tips
        # 中文标题,首播,时长
        for line in tip_text.split("<br/>"):
            if ":" not in line:
                continue

            key, value = line.split(":", 1)
            if key == "中文标题":
                new_prg["话标题"] = value
            elif key == "首播":
                new_prg["发布日期"] = value
            elif key == "时长":
                new_prg["话时长"] = value

        prg_list.append(new_prg)

    return anime_infos, prg_list
