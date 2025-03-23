import json
import re

import __init__ as init
import log.save_log as sl
from lxml import etree

config = init.config

torrent_save_path = config["modes"]["mikan"]["torrent_save_path"]

"""
<tr>&#13;

    <td>2010/01/13 15:04</td>&#13;
    
    <td>&#13;
        <a href="/Home/PublishGroup/38" target="_blank" class="magnet-link-wrap">天使字幕组</a>&#13;
    </td>&#13;

    <td>
        <a href="/Home/Episode/3280e2a298f13f848b1bceb0f0ca0feb158c3801" target="_blank" class="magnet-link-wrap">[天使字幕组][AngelSub][管家后宫学园 Ladies versus Butlers! れでぃ×ばと!][02][848x480][BIG5][RV10][RMVB]</a>
        <a data-clipboard-text="magnet:?xt=urn:btih:3280e2a298f13f848b1bceb0f0ca0feb158c3801&amp;tr=http%3a%2f%2ft.nyaatracker.com%2fannounce&amp;tr=http%3a%2f%2ftracker.kamigami.org%3a2710%2fannounce&amp;tr=http%3a%2f%2fshare.camoe.cn%3a8080%2fannounce&amp;tr=http%3a%2f%2fopentracker.acgnx.se%2fannounce&amp;tr=http%3a%2f%2fanidex.moe%3a6969%2fannounce&amp;tr=http%3a%2f%2ft.acg.rip%3a6699%2fannounce&amp;tr=https%3a%2f%2ftr.bangumi.moe%3a9696%2fannounce&amp;tr=udp%3a%2f%2ftr.bangumi.moe%3a6969%2fannounce&amp;tr=http%3a%2f%2fopen.acgtracker.com%3a1096%2fannounce&amp;tr=udp%3a%2f%2ftracker.opentrackr.org%3a1337%2fannounce" class="js-magnet magnet-link">[复制磁连]</a>
    </td>&#13;

    <td>105.6MB</td>&#13;

    <td>
        <a href="/Download/20100113/3280e2a298f13f848b1bceb0f0ca0feb158c3801.torrent">
            <img src="/images/download_icon_blue.svg" style="margin-left: 2px;width: 20px;height:15px;"/>
        </a>
    </td>&#13;

    <td>
        <a href="https://mypikpak.com/drive/url-checker?url=magnet:?xt.1=urn:btih:3280e2a298f13f848b1bceb0f0ca0feb158c3801">
            <i class="fa fa-play-circle" style="color: #47c1c5; margin-left: 4px; font-size: 18px;"/>
        </a>
    </td>&#13;

</tr>&#13;
"""


def parse_page(xml, json_save_path, type="mikanh"):
    print("-" * 80)
    print("mikan is parsing XML data...")

    try:
        root = etree.HTML(xml)

        mikan_items = {"items": []}

        # 遍历每个 <tr> 元素
        for tr in root.xpath("//tr"):
            # 用字典存储一个item的所有信息
            mikan_item = {}

            # 提取基本信息
            list = tr.xpath("./td[1]")
            mikan_item["date"] = list[0].text if len(list) > 0 else ""
            if not mikan_item["date"]:
                continue

            list = tr.xpath("./td[2]/a")
            mikan_item["group"] = list[0].text if len(list) > 0 else ""
            mikan_item["group_link"] = list[0].get("href") if len(list) > 0 else ""

            list = tr.xpath("./td[3]/a")
            mikan_item["title"] = list[0].text if len(list) > 0 else ""
            mikan_item["title_link"] = list[0].get("href") if len(list) > 0 else ""

            list = tr.xpath("./td[4]")
            mikan_item["size"] = list[0].text if len(list) > 0 else ""

            list = tr.xpath("./td[5]/a")
            mikan_item["torrent_link"] = list[0].get("href") if len(list) > 0 else ""

            list = tr.xpath("./td[6]/a")
            mikan_item["magnet_link"] = list[0].get("data-clipboard-text") if len(list) > 0 else ""

            # 打包到 torrent_datas
            torrent_data = {
                "type": type,
                "title": mikan_item["title"],
                "description": mikan_item["group"],
                "link": "https://mikanani.me/" + mikan_item["title_link"],
                "enclosureLink": "https://mikanani.me/" + mikan_item["torrent_link"],
                "infoHash": "",
                "pubDate": mikan_item["date"],
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
            mikan_items["items"].append(mikan_item)

        # 保存为 JSON 文件
        with open(json_save_path, "w", encoding="utf-8") as f:
            f.truncate()
            json.dump(mikan_items, f, ensure_ascii=False, indent=4)

        # log
        sl.log(mikan_items["items"])

        print("mikan done parsing XML data, " + str(len(mikan_items["items"])) + " items found")
        print("=" * 80)

    except Exception as e:
        print(f"Error: {e}")

    print("=" * 80)


if __name__ == "__main__":
    with open(config["xml_buffer_file"], "rb") as f:
        parse_page(f.read(), config["json_buffer_file"])
