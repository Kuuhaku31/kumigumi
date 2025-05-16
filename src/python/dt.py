# 批量下载种子文件
# 打开 dt.txt
# 每行是一个下载地址
# 多线程进行下载
# 记录成功或者失败
# 下载完成后，删除 dt.txt 相对应的记录
#  失败的记录保留

import os
import sys
import threading

import requests  # 引入 requests 库用于发送 HTTP 请求
from tqdm import tqdm  # 引入 tqdm 进度条库


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
