# mikananime update.py

from concurrent.futures import ThreadPoolExecutor

import mikananime.prase_rss_html as mikan_prase
import mikananime.torrent_headers as hs
import utils.utils as utils


def 请求HTML并解析(MikanAnimate任务):
    rss_html_str = utils.request_html(MikanAnimate任务["蜜柑计划RSS源"])
    info = mikan_prase.prase(MikanAnimate任务["动画名称"], rss_html_str)
    return info


def update_csv(MikanAnimate任务列表, CSV文件地址):

    种子信息列表 = []
    # 使用线程池并行处理 URL 请求和解析

    with ThreadPoolExecutor(max_workers=5) as 线程池:  # 设置线程数，例如 5
        results = 线程池.map(请求HTML并解析, MikanAnimate任务列表)  # 并行处理

    # 收集解析结果
    for info in results:
        种子信息列表 += info

    # 保存到 CSV 文件
    utils.save_csv(CSV文件地址, hs.种子信息表头, 种子信息列表)
