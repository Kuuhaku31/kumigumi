# Desc: 爬取 bangumi.tv 的番剧信息
import csv  # noqa: E402
import json  # noqa: E402
import urllib.request  # noqa: E402

from lxml import etree

headers = ["url", "名称", "中文名", "别名", "话数", "放送开始", "放送星期", "官方网站", "封面"]


# 解析番剧信息
def prase_anime_info(url):

    print(f"正在解析 {url}")

    # 请求
    res = urllib.request.urlopen(url)
    if res.status != 200:
        print(f"请求失败：{url}")

    tree = etree.HTML(res.read().decode("utf-8"))

    # 初始化字典
    anime_infos = {}
    for header in headers:
        anime_infos[header] = ""
    anime_infos["url"] = url
    anime_infos["名称"] = tree.xpath("//h1/a/text()")[0]
    anime_infos["中文名"] = tree.xpath("//h1/a/text()")[0]

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

    print(f"解析完成：{anime_infos['名称']}")

    return anime_infos


# 根据url在线更新 csv 文件
def update_csv(file_name):

    data = []
    new_data = []

    # 读取 json 文件
    with open(file_name, "r", encoding="utf-8") as f:
        data = json.load(f)

    # 遍历csv每一行
    for row in data:
        new_info = prase_anime_info(row)
        new_data.append(new_info)

    # 保存到 csv 文件
    with open("./data.csv", "w+", newline="", encoding="utf-8") as f:
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
