from lxml import etree

import utils


# 解析番剧信息
# 返回字典列表
# 第一个字典是番剧信息
# 其余的字典是单集信息
def 解析bangumiHTML(html_str: str) -> list:
    tree = etree.HTML(html_str)

    # 初始化字典
    anime信息字典 = {}
    anime信息字典[utils.作品番组计划网址] = "https://bangumi.tv" + tree.xpath("//h1/a/@href")[0]
    anime信息字典[utils.作品原名] = tree.xpath("//h1/a/text()")[0]
    anime信息字典[utils.作品中文名] = tree.xpath("//h1/a/text()")[0]

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
            "中文名": utils.作品中文名,
            "别名": utils.作品别名,
            "话数": utils.作品话数,
            "放送开始": utils.作品放送开始,
            "放送星期": utils.作品放送星期,
        }.get(key, key)

        # 获取 <a> 标签内的文本（如果有）或者 li 内部的文本内容（去掉 <span class="tip">）
        value = None
        links = li.xpath(".//a/text()")
        sub_ul = li.xpath(".//ul/li/text()")
        if links:  # 获取 <a> 标签内的文本（如果有）
            if key == "官方网站":  # 如果key是官方网站
                key = utils.作品官方网址
                value = li.xpath(".//a/@href")[0]
            else:
                value = " / ".join(links)

        elif sub_ul:  # 获取 <ul> 标签内的文本（如果有）
            value = " / ".join(sub_ul)

        else:  # 获取 li 内部的文本内容（去掉 <span class="tip">）
            value = "".join(li.xpath(".//text()")).strip().replace(key_elem[0], "").strip()

        # 保存到字典
        anime信息字典[key] = value

    # 转换星期成整数
    anime信息字典[utils.作品放送星期] = {
        "星期日": 0,
        "星期一": 1,
        "星期二": 2,
        "星期三": 3,
        "星期四": 4,
        "星期五": 5,
        "星期六": 6,
    }.get(anime信息字典[utils.作品放送星期])

    # 获取封面
    cover = tree.xpath('//img[@class="cover"]/@src')
    if cover:
        anime信息字典[utils.作品封面] = "https:" + cover[0]

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
        new_prg[utils.作品番组计划网址] = anime信息字典[utils.作品番组计划网址]

        # 通过a标签获取信息
        new_prg[utils.话番组计划网址] = "https://bangumi.tv" + tag_a.xpath("@href")[0]
        new_prg[utils.话索引] = tag_a.xpath("text()")[0]
        new_prg[utils.话原标题] = tag_a.xpath("@title")[0].split(" ", 1)[1]  # 按第一个空格分割，取第二部分
        if new_prg[utils.话原标题] == "":
            new_prg[utils.话原标题] = None

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
                new_prg[utils.话中文标题] = value
            elif key == "首播":
                new_prg[utils.话首播时间] = value
            elif key == "时长":
                new_prg[utils.话时长] = value

        prg_list.append(new_prg)

    return [anime信息字典, *prg_list]
