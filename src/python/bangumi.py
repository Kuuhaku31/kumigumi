# bangumi.py


from lxml import etree

# 定义常量
作品bangumiURL: str = "作品bangumiURL"
作品mikanRSS: str = "作品mikanRSS"
作品原名: str = "作品原名"
作品中文名: str = "作品中文名"
作品别名: str = "作品别名"
作品话数: str = "作品话数"
作品放送开始: str = "作品放送开始"
作品放送星期: str = "作品放送星期"
作品官方网站: str = "作品官方网站"
作品封面URL: str = "作品封面URL"
话bangumiURL: str = "话bangumiURL"
话索引: str = "话索引"
话原标题: str = "话原标题"
话中文标题: str = "话中文标题"
话首播时间: str = "话首播时间"
话时长: str = "话时长"

# 表头
anime_headers = [
    作品bangumiURL,
    作品mikanRSS,
    作品原名,
    作品中文名,
    作品别名,
    作品话数,
    作品放送开始,
    作品放送星期,
    作品官方网站,
    作品封面URL,
]

episode_headers = [
    话bangumiURL,
    作品bangumiURL,
    话索引,
    话原标题,
    话中文标题,
    话首播时间,
    话时长,
]


# 解析番剧信息
# 字典：{动画信息列表（只有一个元素）, 单集信息列表}
def 解析HTML(html_str: str) -> dict:
    tree = etree.HTML(html_str)

    # 初始化字典
    anime_infos = {}
    for header in anime_headers:
        anime_infos[header] = ""
    anime_infos[作品bangumiURL] = "https://bangumi.tv" + tree.xpath("//h1/a/@href")[0]
    anime_infos[作品原名] = tree.xpath("//h1/a/text()")[0]
    anime_infos[作品中文名] = tree.xpath("//h1/a/text()")[0]
    anime_infos[话索引] = -1

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
            "中文名": 作品中文名,
            "别名": 作品别名,
            "话数": 作品话数,
            "放送开始": 作品放送开始,
            "放送星期": 作品放送星期,
        }.get(key, key)

        # 获取 <a> 标签内的文本（如果有）或者 li 内部的文本内容（去掉 <span class="tip">）
        value = None
        links = li.xpath(".//a/text()")
        sub_ul = li.xpath(".//ul/li/text()")
        if links:  # 获取 <a> 标签内的文本（如果有）
            if key == "官方网站":  # 如果key是官方网站
                key = 作品官方网站
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
    anime_infos[作品放送星期] = {
        "星期日": 0,
        "星期一": 1,
        "星期二": 2,
        "星期三": 3,
        "星期四": 4,
        "星期五": 5,
        "星期六": 6,
    }.get(anime_infos[作品放送星期], -1)

    # 获取封面
    cover = tree.xpath('//img[@class="cover"]/@src')
    if cover:
        anime_infos[作品封面URL] = "https:" + cover[0]

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
        for header in episode_headers:
            new_prg[header] = ""

        # 通过a标签获取信息
        new_prg[话bangumiURL] = "https://bangumi.tv" + tag_a.xpath("@href")[0]
        new_prg[作品bangumiURL] = anime_infos[作品bangumiURL]
        new_prg[话原标题] = anime_infos[作品原名]
        new_prg[话中文标题] = anime_infos[作品中文名]
        new_prg[话首播时间] = anime_infos[作品放送开始]
        new_prg[话索引] = tag_a.xpath("text()")[0]
        new_prg[话原标题] = tag_a.xpath("@title")[0].split(" ", 1)[1]  # 按第一个空格分割，取第二部分

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
                new_prg[话中文标题] = value
            elif key == "首播":
                new_prg[话首播时间] = value
            elif key == "时长":
                new_prg[话时长] = value

        prg_list.append(new_prg)

    return {"动画信息": [anime_infos], "单集信息": prg_list}
