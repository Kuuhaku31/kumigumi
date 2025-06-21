# 更新csv文件


from concurrent.futures import ThreadPoolExecutor, as_completed

from tqdm import tqdm  # 引入 tqdm 进度条库

import bangumi.headers as hs
import bangumi.prase_html as ph
import utils.utils as utils


def 请求HTML并解析(url):
    html_str = utils.request_html(url)
    info = ph.prase_html(html_str)
    return info


def update_csv(动画URL列表: list, 动画信息CSV文件路径: str, 单集信息CSV文件路径: str):

    动画信息列表 = []
    单集信息列表 = []

    # 使用线程池并行处理 URL 请求和解析
    with ThreadPoolExecutor(max_workers=5) as 线程池:  # 设置线程数，例如 5
        # 提交任务到线程池
        futures = [线程池.submit(请求HTML并解析, url) for url in 动画URL列表]

        # 使用 tqdm 显示进度条
        for future in tqdm(as_completed(futures), total=len(futures), desc="处理进度"):
            info = future.result()  # 获取任务结果
            动画信息列表 += info["动画信息"]
            单集信息列表 += info["单集信息"]

    # 保存到 CSV 文件
    utils.save_csv(动画信息CSV文件路径, hs.anime_headers, 动画信息列表)
    utils.save_csv(单集信息CSV文件路径, hs.episode_headers, 单集信息列表)
