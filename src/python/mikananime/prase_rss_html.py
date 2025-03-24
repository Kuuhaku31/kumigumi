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


# 传入html字符串，解析html文件为数组
def prase(name: str, rss_html_str: str) -> list:
    tree = etree.HTML(rss_html_str.encode("utf-8"))

    print(f"正在解析 {tree.xpath('//title/text()')[0]}")

    # 遍历每个 <item> 元素
    种子信息列表 = []
    for item in tree.xpath("//item"):

        torrent_item = {"名称": name}  # 用字典存储一个item的所有信息

        # 获取种子链接
        torrent_item["链接"] = item.xpath("./enclosure")[0].get("url") if item.xpath("./enclosure") else ""
        # 获取种子标题
        torrent_item["标题"] = item.xpath("./title")[0].text if item.xpath("./title") else ""
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

        torrent_item["字幕组"] = group_name

        # 提取大小
        size_str = item.xpath("./description")[0].text.split("[")[-1].split("]")[0]

        # 将大小转换为字节（Byte）
        float_num = float(size_str[:-2].strip())
        if "GB" in size_str:
            float_num *= 1024 * 1024 * 1024
        elif "MB" in size_str:
            float_num *= 1024 * 1024
        elif "KB" in size_str:
            float_num *= 1024

        torrent_item["大小"] = size_str
        torrent_item["大小（Byte）"] = int(float_num)

        # 添加到列表
        种子信息列表.append(torrent_item)

    print(f"解析完成：{tree.xpath('//title/text()')[0]}")

    return 种子信息列表
