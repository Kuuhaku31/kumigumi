# Desc: 爬取 bangumi.tv 的番剧信息
import csv  # noqa: E402
import json  # noqa: E402
import urllib.request  # noqa: E402

from lxml import etree

header_str_url = "url"  # bangumi.tv 链接
header_str_name = "名称"  # 作品名
header_str_cn_name = "中文名"  # 作品中文名
header_str_alias = "别名"  # 别名
header_str_episode = "话数/时长"  # 如果是作品，是话数；如果是单集，是时长
header_str_start = "放送开始"  # 上线时间
header_str_weekday = "放送星期"
header_str_website = "官方网站"
header_str_cover = "封面"
header_str_ep_title = "标题"
header_str_ep_cn_title = "中文标题"
header_str_index = "索引"  # 如果是作品，-1；如果是单集，是第几集

# 表头
headers = [
    header_str_url,
    header_str_name,
    header_str_cn_name,
    header_str_alias,
    header_str_episode,
    header_str_start,
    header_str_weekday,
    header_str_website,
    header_str_cover,
    header_str_ep_title,
    header_str_ep_cn_title,
    header_str_index,
]


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


# 获取单集信息
# 话索引;话标题;中文标题;首播;时长;
# 返回字典数组
def prase_prg(tree: etree.Element, ani_info_dir: dict) -> list:
    # 获取单集信息
    # 话索引;话标题;中文标题;首播;时长;
    prg_list = []
    for prg in tree.xpath('//ul[@class="prg_list"]/li'):
        new_prg = ani_info_dir.copy()  # 复制字典
        new_prg[header_str_index] = prg.xpath(".//a/text()")[0]
        new_prg[header_str_ep_title] = prg.xpath(".//a/@title")[0].split(" ", 1)[1]  # 按第一个空格分割，取第二部分
        new_prg[header_str_url] = "https://bangumi.tv" + prg.xpath(".//a/@href")[0]

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
                new_prg[header_str_ep_cn_title] = value
            elif key == "首播":
                new_prg[header_str_start] = value
            elif key == "时长":
                new_prg[header_str_episode] = value

        prg_list.append(new_prg)

    return prg_list


# 解析番剧信息
# 返回字典数组
def prase_html(html: str) -> list:
    tree = etree.HTML(html)

    # 初始化字典
    anime_infos = {}
    for header in headers:
        anime_infos[header] = ""
    anime_infos[header_str_url] = "https://bangumi.tv" + tree.xpath("//h1/a/@href")[0]
    anime_infos[header_str_name] = tree.xpath("//h1/a/text()")[0]
    anime_infos[header_str_cn_name] = tree.xpath("//h1/a/text()")[0]
    anime_infos[header_str_index] = -1

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

        if key == "中文名":
            key = header_str_cn_name
        elif key == "别名":
            key = header_str_alias
        elif key == "话数":
            key = header_str_episode
        elif key == "放送开始":
            key = header_str_start
        elif key == "放送星期":
            key = header_str_weekday

        # 获取 <a> 标签内的文本（如果有）或者 li 内部的文本内容（去掉 <span class="tip">）
        value = None
        links = li.xpath(".//a/text()")
        sub_ul = li.xpath(".//ul/li/text()")
        if links:  # 获取 <a> 标签内的文本（如果有）
            if key == "官方网站":  # 如果key是官方网站
                key = header_str_website
                value = li.xpath(".//a/@href")[0]
            else:
                value = " / ".join(links)

        elif sub_ul:  # 获取 <ul> 标签内的文本（如果有）
            value = " / ".join(sub_ul)

        else:  # 获取 li 内部的文本内容（去掉 <span class="tip">）
            value = "".join(li.xpath(".//text()")).strip().replace(key_elem[0], "").strip()

        # 保存到字典
        anime_infos[key] = value

    # 格式化星期
    date_switch = {
        "星期日": 0,
        "星期一": 1,
        "星期二": 2,
        "星期三": 3,
        "星期四": 4,
        "星期五": 5,
        "星期六": 6,
    }
    anime_infos[header_str_weekday] = date_switch.get(anime_infos[header_str_weekday], "")

    # 获取封面
    cover = tree.xpath('//img[@class="cover"]/@src')
    if cover:
        anime_infos[header_str_cover] = "https:" + cover[0]

    # anime_data_list 添加 prg_list 和 anime_infos
    anime_data_list = []
    anime_data_list.append(anime_infos)
    anime_data_list += prase_prg(tree, anime_infos)  # 获取单集信息

    print(f"解析完成：{anime_infos['名称']}")

    return anime_data_list


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
        new_info_list = prase_anime_info(row)
        for new_info in new_info_list:
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
