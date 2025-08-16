# mikananime update.py


import lxml
import lxml.etree
from lxml import etree

import utils as utils

"""
## ✅`torrent` 表字段对应

| 中文键名          | 英文键名                  |          |
| ----------------- | ------------------------- | -------- |
| `种子下载链接`    | `torrent_download_url`    | 主键     |
| `番组bangumi链接` | `anime_bangumi_url`       |          |
| `发布日期`        | `air_date`                |          |
| `种子页面链接`    | `torrent_page_url`        |          |
| `种子字幕组`      | `torrent_subtitle_group`  |          |
| `种子标题`        | `torrent_title`           |          |
| `种子描述`        | `torrent_description`     |          |
| `种子大小`        | `torrent_size`            |          |
| `种子大小_字节`   | `torrent_size_bytes`      |          |
| `种子下载情况`    | `torrent_download_status` | 手动维护 |
| `备注`            | `note`                    | 手动维护 |
"""


# 传入html字符串，解析html文件为数组
def 解析mikanRSS_XML(bangumi_url: str, rss_html_str: str) -> list[dict]:
    tree: lxml.etree._Element = etree.HTML(rss_html_str.encode("utf-8"))

    # 遍历每个 <item> 元素
    种子信息列表: list[dict] = []
    for item in tree.xpath("//item"):

        种子信息 = {"番组bangumi链接": bangumi_url}  # 用字典存储一个item的所有信息

        种子信息["种子下载链接"] = item.xpath("./enclosure")[0].get("url") if item.xpath("./enclosure") else ""
        种子信息["种子标题"] = item.xpath("./title")[0].text if item.xpath("./title") else ""
        种子信息["发布日期"] = item.xpath("./torrent/pubdate")[0].text if item.xpath("./torrent/pubdate") else ""
        种子信息["种子页面链接"] = item.xpath("./link")[0].tail if item.xpath("./link") else ""
        种子信息["种子描述"] = item.xpath("./description")[0].text if item.xpath("./description") else ""

        # 提取字幕组信息
        group_name = ""
        if 种子信息["种子标题"][0] == "[":
            group_name = 种子信息["种子标题"].split("]")[0][1:]
        elif 种子信息["种子标题"][0] == "【":
            group_name = 种子信息["种子标题"].split("】")[0][1:]
        elif len(group_name) > 255:
            group_name = "未知字幕组"
        else:
            group_name = "未知字幕组"

        种子信息["种子字幕组"] = group_name

        # 解析字幕组信息
        # if group_name in pt.解析方法字典:
        #     种子信息.update(pt.解析方法字典[种子信息["种子字幕组"]](种子信息["种子标题"]))

        # 提取大小
        content_length = item.xpath("./torrent/contentlength")
        content_length_int = int(content_length[0].text) if content_length else -1
        种子信息["种子大小_字节"] = content_length_int

        # 解析成 KB, MB, GB 等格式
        content_length_str = "-"
        if content_length_int >= 0:
            if content_length_int < 1024:
                content_length_str = f"{content_length_int} B"
            elif content_length_int < 1024 * 1024:
                content_length_str = f"{content_length_int / 1024:.2f} KB"
            elif content_length_int < 1024 * 1024 * 1024:
                content_length_str = f"{content_length_int / (1024 * 1024):.2f} MB"
            else:
                content_length_str = f"{content_length_int / (1024 * 1024 * 1024):.2f} GB"
        种子信息["种子大小"] = content_length_str

        种子信息["种子大小_字节"] = str(种子信息["种子大小_字节"])  # 确保是字符串类型

        # 添加到列表
        种子信息列表.append(种子信息)

    return 种子信息列表
