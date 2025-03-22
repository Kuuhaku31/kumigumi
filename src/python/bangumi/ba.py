# Desc: 爬取 bangumi.tv 的番剧信息
import csv  # noqa: E402
import json  # noqa: E402
import urllib.request  # noqa: E402

from lxml import etree

headers = ["url", "名称", "中文名", "别名", "话数", "放送开始", "放送星期", "官方网站", "封面", "话列表"]


# 获取html页面
def get_html(url: str) -> str:
    print(f"正在请求 {url}")

    res = urllib.request.urlopen(url)
    if res.status != 200:
        print(f"请求失败：{url}")
        return None
    else:
        print(f"请求成功：{url}")
        return res.read().decode("utf-8")


# 解析番剧信息
def prase_html(html: str) -> dict:
    tree = etree.HTML(html)

    # 初始化字典
    anime_infos = {}
    for header in headers:
        anime_infos[header] = ""
    anime_infos["url"] = "https://bangumi.tv" + tree.xpath("//h1/a/@href")[0]
    anime_infos["名称"] = tree.xpath("//h1/a/text()")[0]
    anime_infos["中文名"] = tree.xpath("//h1/a/text()")[0]

    print(f"正在解析 {anime_infos['名称']}")

    # 查找 ul#infobox 内的所有 li
    infobox_lis = tree.xpath('//ul[@id="infobox"]/li')
    for li in infobox_lis:
        # 获取 <span class="tip"> 标签的文本（键）
        key_elem = li.xpath('.//span[@class="tip"]/text()')
        if not key_elem:
            continue

        # 获取 <span class="tip"> 标签的文本（键）
        key = key_elem[0].strip().replace(":", "")  # 去除冒号

        # 获取 <a> 标签内的文本（如果有）或者 li 内部的文本内容（去掉 <span class="tip">）
        value = None
        links = li.xpath(".//a/text()")
        sub_ul = li.xpath(".//ul/li/text()")
        if links:  # 获取 <a> 标签内的文本（如果有）
            if key == "官方网站":  # 如果key是官方网站
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
        anime_infos["封面"] = "https:" + cover[0]

    # 获取单集信息
    prg_list = []
    prg_lis = tree.xpath('//ul[@class="prg_list"]/li')
    for prg in prg_lis:
        new_prg = {}
        new_prg["话索引"] = prg.xpath(".//a/text()")[0]
        new_prg["话标题"] = prg.xpath(".//a/@title")[0].split(" ", 1)[1]  # 按第一个空格分割，取第二部分

        id = prg.xpath(".//a/@rel")[0].replace("#", "")
        span = tree.xpath(f'//div[@id="{id}"]/span')
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
        for line in tip_text.split("<br/>"):
            if ":" in line:
                key, value = line.split(":", 1)
                new_prg[key.strip()] = value.strip()

        prg_list.append(new_prg)

    anime_infos["话列表"] = prg_list

    print(f"解析完成：{anime_infos['名称']}")

    return anime_infos


def prase_anime_info(url):

    html = get_html(url)
    if not html:
        return None

    anime_info = prase_html(html)
    return anime_info


# 根据url在线更新 csv 文件
def update_csv(json_file: str, csv_file: str):

    data = []
    new_data = []

    # 读取 json 文件
    with open(json_file, "r", encoding="utf-8") as f:
        data = json.load(f)

    # 遍历csv每一行
    for row in data:
        new_info = prase_anime_info(row)
        new_data.append(new_info)

    # 保存到 csv 文件
    with open(csv_file, "w+", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(headers)
        for row in new_data:
            new_row = []
            for header in headers:
                elem = row.get(header)
                new_row.append(elem)
            writer.writerow(new_row)


if __name__ == "__main__":

    # 保存到 csv
    update_csv("./urls.json")
