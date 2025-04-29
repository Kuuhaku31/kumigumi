# mikananime update.py

from concurrent.futures import ThreadPoolExecutor, as_completed

from tqdm import tqdm  # 引入 tqdm 进度条库

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
        futures = [线程池.submit(请求HTML并解析, MikanAnimate任务) for MikanAnimate任务 in MikanAnimate任务列表]

        # 使用 tqdm 显示进度条
        for future in tqdm(as_completed(futures), total=len(futures), desc="处理进度"):
            info = future.result()  # 获取任务结果
            种子信息列表 += info

    # 保存到 CSV 文件
    utils.save_csv(CSV文件地址, hs.种子信息表头, 种子信息列表)
