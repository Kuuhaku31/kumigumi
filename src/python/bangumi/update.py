# 更新csv文件


from concurrent.futures import ThreadPoolExecutor

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
        results = 线程池.map(请求HTML并解析, 动画URL列表)

    # 收集解析结果
    for info in results:
        动画信息列表 += info["动画信息"]
        单集信息列表 += info["单集信息"]

    # 保存到 CSV 文件
    utils.save_csv(动画信息CSV文件路径, hs.anime_headers, 动画信息列表)
    utils.save_csv(单集信息CSV文件路径, hs.episode_headers, 单集信息列表)
