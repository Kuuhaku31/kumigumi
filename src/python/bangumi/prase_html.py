# Desc: 爬取 bangumi.tv 的番剧信息

import bangumi.headers as hs
from lxml import etree


# 解析番剧信息
# 字典：{动画信息列表（只有一个元素）, 单集信息列表}
def prase_html(html_str: str) -> dict:
    tree = etree.HTML(html_str)

    print(f"正在解析 {tree.xpath("//h1/a/text()")[0]}")

    # 初始化字典
    anime_infos = {}
    for header in hs.anime_headers:
        anime_infos[header] = ""
    anime_infos[hs.bangumi_url] = "https://bangumi.tv" + tree.xpath("//h1/a/@href")[0]
    anime_infos[hs.original_name] = tree.xpath("//h1/a/text()")[0]
    anime_infos[hs.chinese_name] = tree.xpath("//h1/a/text()")[0]
    anime_infos[hs.episode_index] = -1

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
            "中文名": hs.chinese_name,
            "别名": hs.alternate_name,
            "话数": hs.total_episodes,
            "放送开始": hs.start_date,
            "放送星期": hs.broadcast_day,
        }.get(key, key)

        # 获取 <a> 标签内的文本（如果有）或者 li 内部的文本内容（去掉 <span class="tip">）
        value = None
        links = li.xpath(".//a/text()")
        sub_ul = li.xpath(".//ul/li/text()")
        if links:  # 获取 <a> 标签内的文本（如果有）
            if key == "官方网站":  # 如果key是官方网站
                key = hs.official_site
                value = li.xpath(".//a/@href")[0]
            else:
                value = " / ".join(links)

        elif sub_ul:  # 获取 <ul> 标签内的文本（如果有）
            value = " / ".join(sub_ul)

        else:  # 获取 li 内部的文本内容（去掉 <span class="tip">）
            value = "".join(li.xpath(".//text()")).strip().replace(key_elem[0], "").strip()

        # 保存到字典
        anime_infos[key] = value

    # 转换星期成整数
    anime_infos[hs.broadcast_day] = {
        "星期日": 0,
        "星期一": 1,
        "星期二": 2,
        "星期三": 3,
        "星期四": 4,
        "星期五": 5,
        "星期六": 6,
    }.get(anime_infos[hs.broadcast_day], -1)

    # 获取封面
    cover = tree.xpath('//img[@class="cover"]/@src')
    if cover:
        anime_infos[hs.cover_image] = "https:" + cover[0]

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
        for header in hs.episode_headers:
            new_prg[header] = ""

        # 通过a标签获取信息
        new_prg[hs.bangumi_url] = "https://bangumi.tv" + tag_a.xpath("@href")[0]
        new_prg[hs.original_name] = anime_infos[hs.original_name]
        new_prg[hs.chinese_name] = anime_infos[hs.chinese_name]
        new_prg[hs.broadcast_day] = anime_infos[hs.broadcast_day]
        new_prg[hs.episode_index] = tag_a.xpath("text()")[0]
        new_prg[hs.episode_title] = tag_a.xpath("@title")[0].split(" ", 1)[1]  # 按第一个空格分割，取第二部分

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
                new_prg[hs.episode_chinese_title] = value
            elif key == "首播":
                new_prg[hs.episode_air_date] = value
            elif key == "时长":
                new_prg[hs.duration] = value

        prg_list.append(new_prg)

    print(f"解析完成：{anime_infos['名称']}")

    return {"动画信息": [anime_infos], "单集信息": prg_list}
