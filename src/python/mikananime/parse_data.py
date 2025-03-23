# 解析 XML 数据

import json
import re

import log.save_log as sl
from lxml import etree

import mikananime.__init__ as init

"""
数据库表结构

create table torrents(

ID                      int primary key auto_increment,
type                    varchar(255),
title                   varchar(500) not null,
description             text,
link                    varchar(255),
enclosureLink           varchar(255) not null,
infoHash                varchar(40) not null,
pubDate                 varchar(255) not null
savePath                varchar(255)

);

"""

mode_mikan_config = init.config["modes"]["mikan"]

torrent_save_path = mode_mikan_config["torrent_save_path"]


# 解析 XML 数据，返回字典列表
def get_data(xml, json_save_path, type="mikan"):
    print("-" * 80)
    print("mikan is parsing XML data...")

    try:
        root = etree.fromstring(xml)

        mikan_items = []

        namespaces = {"ns": "https://mikanani.me/0.1/"}  # 定义命名空间映射

        # 遍历每个 <item> 元素
        for item in root.xpath("//item"):
            # 用字典存储一个item的所有信息
            mikan_item = {}

            # 提取基本信息
            mikan_item["guid"] = item.find("guid").text if item.find("guid") is not None else ""
            mikan_item["guid_isPermaLink"] = (
                item.find("guid").get("isPermaLink") if item.find("guid") is not None else ""
            )
            mikan_item["link"] = item.find("link").text if item.find("link") is not None else ""
            mikan_item["title"] = item.find("title").text if item.find("title") is not None else ""
            mikan_item["description"] = item.find("description").text if item.find("description") is not None else ""

            # 提取 <torrent> 内的信息，需要处理命名空间
            torrent_element = item.find("ns:torrent", namespaces)
            if torrent_element is not None:
                mikan_item["torrent_link"] = (
                    torrent_element.find("ns:link", namespaces).text
                    if torrent_element.find("ns:link", namespaces) is not None
                    else ""
                )
                mikan_item["torrent_contentLength"] = (
                    torrent_element.find("ns:contentLength", namespaces).text
                    if torrent_element.find("ns:contentLength", namespaces) is not None
                    else ""
                )
                mikan_item["torrent_pubDate"] = (
                    torrent_element.find("ns:pubDate", namespaces).text
                    if torrent_element.find("ns:pubDate", namespaces) is not None
                    else ""
                )

            # 提取 <enclosure> 的属性
            enclosure = item.find("enclosure")
            if enclosure is not None:
                mikan_item["enclosure_type"] = enclosure.get("type", "")
                mikan_item["enclosure_length"] = enclosure.get("length", "")
                mikan_item["enclosure_url"] = enclosure.get("url", "")
            else:
                mikan_item["enclosure_type"] = ""
                mikan_item["enclosure_length"] = ""
                mikan_item["enclosure_url"] = ""

            # 打包到 torrent_datas
            torrent_data = {
                "type": type,
                "title": mikan_item["title"],
                "description": mikan_item["description"],
                "link": mikan_item["link"],
                "enclosureLink": mikan_item["enclosure_url"],
                "infoHash": "",
                "pubDate": mikan_item["torrent_pubDate"],
            }

            # 生成savePath
            part01 = f"[{torrent_data["type"]}]"
            length01 = len(part01)
            part02 = f"[{torrent_data["title"]}]"
            length02 = len(part02)
            part03 = f"[{torrent_data["pubDate"]}]"
            length03 = len(part03)

            # 限制文件名长度
            length = length01 + length02 + length03
            if length > 255:
                part02 = part02[: 255 - length01 - length03]
            file_name = part01 + part02 + part03

            # 处理文件名中的非法字符
            file_name = re.sub(r"[\/:*?\"<>|\\]", "-", file_name)

            # 保存路径
            torrent_data["savePath"] = torrent_save_path + file_name + ".torrent"
            mikan_item["torrent_data"] = torrent_data

            # 打包到 mikan_items
            mikan_items.append(mikan_item)

        # 保存到 json 文件
        with open(json_save_path, "w", encoding="utf-8") as file:
            # 清空文件
            file.truncate()
            json.dump(mikan_items, file, ensure_ascii=False, indent=4)

        # 保存log
        sl.log(mikan_items)

        print("mikan done parsing XML data, " + str(len(mikan_items)) + " items found")
        print("=" * 80)

    except Exception as e:
        print("mikan parse XML data failed: " + str(e))
        print("=" * 80)
        return []
