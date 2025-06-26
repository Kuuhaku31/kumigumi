# test.py


from concurrent.futures import ThreadPoolExecutor, as_completed
from typing import Tuple

from tqdm import tqdm

import bangumi
import headers
import utils


def 批量获取数据() -> Tuple[list[dict], list[dict]]:

    url_list = []
    with open("data/urls.txt", "r", encoding="utf-8") as f:
        for line in f:
            url = line.strip()
            if url:
                url_list.append(url)

    anime_info_list = []
    episode_info_list = []

    # 多线程实现
    with ThreadPoolExecutor() as executor:
        future_to_url = {executor.submit(utils.request_html, url): url for url in url_list}

        for future in tqdm(as_completed(future_to_url), total=len(future_to_url), desc="获取进度"):
            url = future_to_url[future]
            try:
                html_str = future.result()
                anime_info, episode_info = bangumi.解析BangumiHTML_str(html_str)
                anime_info_list.append(anime_info)
                episode_info_list.extend(episode_info)
            except Exception as e:
                print(f"❌ 获取 {url} 时发生错误: {e}")

    # 返回动画信息和单集信息
    return anime_info_list, episode_info_list


if __name__ == "__main__":

    # url = "https://bangumi.tv/subject/328609"

    # html_str = utils.request_html(url)

    anime_info, episode_info = 批量获取数据()

    utils.保存CSV文件("anime.csv", headers.番组表头_src, anime_info)
    utils.保存CSV文件("episode.csv", headers.单集表头_src, episode_info)
