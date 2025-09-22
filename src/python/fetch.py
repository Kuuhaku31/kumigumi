# fetch.py


import os
import sys
import threading
from concurrent.futures import ThreadPoolExecutor, as_completed
from typing import List, Tuple

import requests
from bangumi.bangumi import 解析BangumiHTML_str
from mikananime.mikananime import 解析mikanRSS_XML
from tqdm import tqdm
from utils.utils import request_html

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
        future_to_url = {executor.submit(request_html, url): url for url in url_list}

        for future in tqdm(as_completed(future_to_url), total=len(future_to_url), desc="获取番组数据进度"):
            url = future_to_url[future]
            try:
                html_str = future.result()
                anime_info, episode_info = 解析BangumiHTML_str(html_str)
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

            if mikan_rss_url is None or mikan_rss_url == "":
                return []
            try:
                rss_html_str = request_html(mikan_rss_url)
            except Exception as e:
                print(f"❌ 获取 {bgm_url}: {mikan_rss_url} 时发生错误: {e}")
                return []

            return 解析mikanRSS_XML(bgm_url, rss_html_str)

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


def 批量下载种子(download_path: str, 种子下载链接列表: list[str]):
    """
    批量下载种子
    :param 种子下载链接列表: 包含多个种子下载链接的列表
    :return: None
    """

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


def 下载种子(url, 保存地址, 进度条, 失败列表):
    try:
        # 获取文件名
        文件名 = url.split("/")[-1]
        文件路径 = os.path.join(保存地址, 文件名)

        # 下载种子文件
        response = requests.get(url, stream=True, timeout=10)
        if response.status_code == 200:
            with open(文件路径, "wb") as file:
                for chunk in response.iter_content(chunk_size=1024):
                    file.write(chunk)
        else:
            失败列表.append(url)
    except Exception:
        失败列表.append(url)
    finally:
        进度条.update(1)  # 每完成一个任务，更新进度条


def 依据列表文件下载种子(下载列表文件地址, 保存地址):
    with open(下载列表文件地址, "r") as file:
        urls = file.readlines()

    # 创建保存目录
    if not os.path.exists(保存地址):
        os.makedirs(保存地址)

    线程池 = []
    失败列表 = []  # 用于记录下载失败的 URL
    进度条 = tqdm(total=len(urls), desc="下载进度")  # 初始化进度条

    # 创建线程池并启动线程
    for url in urls:
        url = url.strip()
        线程 = threading.Thread(target=下载种子, args=(url, 保存地址, 进度条, 失败列表))
        线程池.append(线程)

    for 线程 in 线程池:
        线程.start()
    for 线程 in 线程池:
        线程.join()

    进度条.close()  # 关闭进度条

    # 将失败的 URL 写回文件
    with open(下载列表文件地址, "w") as file:
        file.writelines(f"{url}\n" for url in 失败列表)

    # 显示结果
    任务数量 = len(urls)
    成功数量 = len(urls) - len(失败列表)
    print(f"下载完成: {成功数量}/{任务数量} 个文件成功下载")


# 主程序
# 获取用户传入参数：下载列表文件地址和保存地址
if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("用法: python dt.py <下载列表文件地址> <保存地址>")
        sys.exit(1)

    下载列表文件地址 = sys.argv[1]
    保存地址 = sys.argv[2]

    print(f"下载列表文件地址: {下载列表文件地址}")
    print(f"保存地址: {保存地址}")

    依据列表文件下载种子(下载列表文件地址, 保存地址)
