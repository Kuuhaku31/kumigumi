# fetch.py


import os
from concurrent.futures import ThreadPoolExecutor, as_completed
from typing import List, Tuple

import bangumi
import mikananime.mikananime as mikananime
import requests
import utils
from tqdm import tqdm

# 获取数据


def 批量获取番组及单集数据(url_list: list[str]) -> Tuple[List[dict], List[dict]]:
    """
    批量获取动画信息和单集信息

    :param url_list: 包含多个 Bangumi URL 的列表
    :return: 返回动画信息列表和单集信息列表
    """

    anime_info_list = []
    episode_info_list = []

    # 多线程实现
    with ThreadPoolExecutor() as executor:
        future_to_url = {executor.submit(utils.request_html, url): url for url in url_list}

        for future in tqdm(as_completed(future_to_url), total=len(future_to_url), desc="获取番组数据进度"):
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


def 批量获取种子数据(data: dict[str, str]) -> list[dict]:
    """
    批量获取种子数据
    :param data: 包含多个番组链接和对应 RSS 订阅链接的字典列表 即 番组链接 : RSS订阅链接
    :return: 返回所有种子数据的列表
    """

    # 使用多线程批量获取种子数据
    种子数据列表: list[dict] = []
    with ThreadPoolExecutor() as executor:

        def 获取种子数据(bgm_url: str, mikan_rss_url: str) -> list[dict]:

            if mikan_rss_url is None:
                return []
            try:
                rss_html_str = utils.request_html(mikan_rss_url)
            except Exception as e:
                print(f"❌ 获取 {bgm_url}: {mikan_rss_url} 时发生错误: {e}")
                return []

            return mikananime.解析mikanRSS_XML(bgm_url, rss_html_str)

        futures = {
            executor.submit(获取种子数据, bgm_url, rss_url): (bgm_url, rss_url) for bgm_url, rss_url in data.items()
        }

        for future in tqdm(as_completed(futures), total=len(futures), desc="获取种子数据进度"):
            try:
                result = future.result()
                if result:
                    种子数据列表.extend(result)
            except Exception as e:
                print(f"❌ 获取种子数据时发生错误: {e}")

    # 返回所有种子数据
    return 种子数据列表


def 批量下载种子(种子下载链接列表: list[str]):
    """
    批量下载种子
    :param 种子下载链接列表: 包含多个种子下载链接的列表
    :return: None
    """

    download_path = utils.获取用户默认下载路径() + "/dt/"
    os.makedirs(download_path, exist_ok=True)

    # 单个种子下载函数
    def download_torrent(种子下载链接: str):
        try:
            file_name = os.path.basename(种子下载链接)
            file_path = os.path.join(download_path, file_name)
            resp = requests.get(种子下载链接, timeout=30)
            resp.raise_for_status()
            with open(file_path, "wb") as f:
                f.write(resp.content)
        except Exception as e:
            raise RuntimeError(f"下载失败: {种子下载链接}，原因: {e}")

    fail_url_list = []

    # 使用多线程批量下载种子
    with ThreadPoolExecutor() as executor:
        futures = {executor.submit(download_torrent, url): url for url in 种子下载链接列表}

        for future in tqdm(as_completed(futures), total=len(futures), desc="下载种子文件进度"):
            url = futures[future]
            try:
                future.result()  # 等待下载完成
            except Exception as e:
                print(f"❌ 下载种子 {url} 时发生错误: {e}")
                fail_url_list.append(url)

    # 将下载失败的链接保存到文件
    if fail_url_list:
        fail_file_path = os.path.join(download_path, "failed_downloads.txt")
        with open(fail_file_path, "w", encoding="utf-8") as f:
            for url in fail_url_list:
                f.write(url + "\n")
        print(f"❌ {len(fail_url_list)} 个种子下载失败，已保存到 {fail_file_path}")
