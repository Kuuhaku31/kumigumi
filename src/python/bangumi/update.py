# 更新csv文件


import bangumi.headers as hs
import bangumi.prase_html as ph
import utils.utils as utils


# 传入url列表，解析每个url的信息，保存到csv文件
def update_csv(url_list: list, anime_csv_file: str, ep_csv_file: str):

    anime_info_list = []  # 动画信息列表
    anime_ep_info_list = []  # 单集信息列表

    # 遍历csv每一行
    for url in url_list:  # 解析出动画信息和单集信息
        html_str = utils.request_html(url)
        anime_info = ph.prase_html(html_str)
        anime_info_list += anime_info["动画信息"]
        anime_ep_info_list += anime_info["单集信息"]

    # 保存到 csv 文件 headers_anime 和 headers_ep
    utils.save_csv(anime_csv_file, hs.anime_headers, anime_info_list)
    utils.save_csv(ep_csv_file, hs.episode_headers, anime_ep_info_list)
