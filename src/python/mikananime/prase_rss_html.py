#
from lxml import etree

# 按照字幕组分类
"""
dict:{
    "字幕组A":[
    种子:{
    "链接":"...",
    "标题":"...",
    "描述":"...",
    "发布日期":"...",
    }
    ]
    "字幕组B":[...]
    "字幕组C":[...]
}
"""
"""
 <item>
      <guid isPermaLink="false">[喵萌奶茶屋&amp;LoliHouse] 群花绽放，仿如修罗 / Hana wa Saku Shura no Gotoku - 11 [WebRip 1080p HEVC-10bit AAC][简繁日内封字幕]</guid>
      <link>https://mikanani.me/Home/Episode/9d22370519e85dde9c9521a289812d30b7b0321b</link>
      <title>[喵萌奶茶屋&amp;LoliHouse] 群花绽放，仿如修罗 / Hana wa Saku Shura no Gotoku - 11 [WebRip 1080p HEVC-10bit AAC][简繁日内封字幕]</title>
      <description>[喵萌奶茶屋&amp;LoliHouse] 群花绽放，仿如修罗 / Hana wa Saku Shura no Gotoku - 11 [WebRip 1080p HEVC-10bit AAC][简繁日内封字幕][364.66 MB]</description>
      <torrent xmlns="https://mikanani.me/0.1/">
        <link>https://mikanani.me/Home/Episode/9d22370519e85dde9c9521a289812d30b7b0321b</link>
        <contentLength>382373728</contentLength>
        <pubDate>2025-03-23T10:02:52.301</pubDate>
      </torrent>
      <enclosure type="application/x-bittorrent" length="382373728" url="https://mikanani.me/Download/20250323/9d22370519e85dde9c9521a289812d30b7b0321b.torrent" />
    </item>

"""


# 传入html字符串，解析html文件为字典
def prase(html_str: str) -> dict:
    tree = etree.HTML(html_str.encode("utf-8"))

    # 遍历每个 <item> 元素
    mikan_items = {}
    for item in tree.xpath("//item"):

        torrent_item = {}  # 用字典存储一个item的所有信息

        # 获取种子链接
        torrent_item["链接"] = item.xpath("./enclosure")[0].get("url") if item.xpath("./enclosure") else ""
        # 获取种子标题
        torrent_item["标题"] = item.xpath("./title")[0].text if item.xpath("./title") else ""
        # 获取种子描述
        torrent_item["描述"] = item.xpath("./description")[0].text if item.xpath("./description") else ""
        # 获取发布日期
        torrent_item["发布日期"] = item.xpath("./torrent/pubdate")[0].text if item.xpath("./torrent/pubdate") else ""

        # 提取字幕组信息
        group_name = ""
        if torrent_item["标题"][0] == "[":
            group_name = torrent_item["标题"].split("]")[0][1:]
        elif torrent_item["标题"][0] == "【":
            group_name = torrent_item["标题"].split("】")[0][1:]
        else:
            group_name = "未知字幕组"

        # 添加到字典
        if group_name not in mikan_items:
            mikan_items[group_name] = [torrent_item]
        else:
            mikan_items[group_name].append(torrent_item)

    return mikan_items
