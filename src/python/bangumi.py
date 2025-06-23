# bangumi.py

from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

from lxml import etree
from tqdm import tqdm  # 引入 tqdm 进度条库

import utils.utils as utils

# 表头

# 共同信息
bangumi_url = "url"  # bangumi.tv 链接
original_name = "名称"  # 作品名
chinese_name = "中文名"  # 作品中文名
broadcast_day = "放送星期"

# 动画信息
alternate_name = "别名"  # 别名
total_episodes = "话数"  # 总话数
start_date = "放送开始"  # 上线时间
official_site = "官方网站"
cover_image = "封面"

# 单集信息
episode_index = "索引"  # 第几集
episode_title = "标题"
episode_chinese_title = "中文标题"
episode_air_date = "首播"  # 首播时间
duration = "时长"

# 表头
anime_headers = [
    bangumi_url,
    original_name,
    chinese_name,
    alternate_name,
    total_episodes,
    start_date,
    broadcast_day,
    official_site,
    cover_image,
]

episode_headers = [
    bangumi_url,
    original_name,
    chinese_name,
    broadcast_day,
    episode_index,
    episode_title,
    episode_chinese_title,
    episode_air_date,
    duration,
]


# 解析番剧信息
# 字典：{动画信息列表（只有一个元素）, 单集信息列表}
def 解析HTML(html_str: str) -> dict:
    tree = etree.HTML(html_str)

    # 初始化字典
    anime_infos = {}
    for header in anime_headers:
        anime_infos[header] = ""
    anime_infos[bangumi_url] = "https://bangumi.tv" + tree.xpath("//h1/a/@href")[0]
    anime_infos[original_name] = tree.xpath("//h1/a/text()")[0]
    anime_infos[chinese_name] = tree.xpath("//h1/a/text()")[0]
    anime_infos[episode_index] = -1

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
            "中文名": chinese_name,
            "别名": alternate_name,
            "话数": total_episodes,
            "放送开始": start_date,
            "放送星期": broadcast_day,
        }.get(key, key)

        # 获取 <a> 标签内的文本（如果有）或者 li 内部的文本内容（去掉 <span class="tip">）
        value = None
        links = li.xpath(".//a/text()")
        sub_ul = li.xpath(".//ul/li/text()")
        if links:  # 获取 <a> 标签内的文本（如果有）
            if key == "官方网站":  # 如果key是官方网站
                key = official_site
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
    anime_infos[broadcast_day] = {
        "星期日": 0,
        "星期一": 1,
        "星期二": 2,
        "星期三": 3,
        "星期四": 4,
        "星期五": 5,
        "星期六": 6,
    }.get(anime_infos[broadcast_day], -1)

    # 获取封面
    cover = tree.xpath('//img[@class="cover"]/@src')
    if cover:
        anime_infos[cover_image] = "https:" + cover[0]

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
        new_prg[bangumi_url] = "https://bangumi.tv" + tag_a.xpath("@href")[0]
        new_prg[original_name] = anime_infos[original_name]
        new_prg[chinese_name] = anime_infos[chinese_name]
        new_prg[broadcast_day] = anime_infos[broadcast_day]
        new_prg[episode_index] = tag_a.xpath("text()")[0]
        new_prg[episode_title] = tag_a.xpath("@title")[0].split(" ", 1)[1]  # 按第一个空格分割，取第二部分

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
                new_prg[episode_chinese_title] = value
            elif key == "首播":
                new_prg[episode_air_date] = value
            elif key == "时长":
                new_prg[duration] = value

        prg_list.append(new_prg)

    return {"动画信息": [anime_infos], "单集信息": prg_list}


def 更新CSV文件(动画URL列表: list[str], 动画信息CSV文件地址: Path, 单集信息CSV文件地址: Path):

    动画信息列表 = []
    单集信息列表 = []

    # 使用线程池并行处理 URL 请求和解析
    with ThreadPoolExecutor(max_workers=5) as 线程池:  # 设置线程数，例如 5

        # 定义请求 HTML 并解析的函数
        def 请求HTML并解析(url):
            html_str = utils.request_html(url)
            info = 解析HTML(html_str)
            return info

        # 提交任务到线程池
        futures = [线程池.submit(请求HTML并解析, url) for url in 动画URL列表]

        # 使用 tqdm 显示进度条
        for future in tqdm(as_completed(futures), total=len(futures), desc="处理进度"):
            info = future.result()  # 获取任务结果
            动画信息列表 += info["动画信息"]
            单集信息列表 += info["单集信息"]

    # 保存到 CSV 文件
    utils.save_csv(动画信息CSV文件地址, anime_headers, 动画信息列表)
    utils.save_csv(单集信息CSV文件地址, episode_headers, 单集信息列表)
