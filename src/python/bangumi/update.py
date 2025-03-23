# 更新csv文件

import json

import bangumi.headers as hs
import bangumi.prase_html as ph
import utils


# 传入url列表，解析每个url的信息，保存到csv文件
def update_csv(config_file: str, anime_csv_file: str, ep_csv_file: str):

    url_list = []  # url列表
    anime_info_list = []  # 动画信息列表
    anime_ep_info_list = []  # 单集信息列表

    with open(config_file, encoding="utf-8") as f:
        config_info = json.load(f)
        url_list = [anime_item["链接"]["bangumi"] for anime_item in config_info["动画信息"]]

    # 遍历csv每一行
    for url in url_list:  # 解析出动画信息和单集信息
        html_str = utils.request_html(url)
        anime_info = ph.prase_html(html_str)
        anime_info_list += anime_info["动画信息"]
        anime_ep_info_list += anime_info["单集信息"]

    # 保存到 csv 文件 headers_anime 和 headers_ep
    utils.save_csv(anime_csv_file, hs.anime_headers, anime_info_list)
    utils.save_csv(ep_csv_file, hs.episode_headers, anime_ep_info_list)


# 补全配置文件
def update_config(config_file: str):

    config_info = {}  # 配置信息
    anime_info_list = []  # 动画信息列表
    anime_ep_info_list = []  # 单集信息列表

    with open(config_file, encoding="utf-8") as f:
        config_info = json.load(f)

        for anime_item in config_info["动画信息"]:
            if "链接" not in anime_item:
                print(f"没有链接信息：{anime_item['名称']}")
                continue
            elif "bangumi" not in anime_item["链接"]:
                print(f"没有bangumi链接：{anime_item['名称']}")
                continue

            # 获取bangumi页面信息
            bangumi_url = anime_item["链接"]["bangumi"]
            anime_info_dict = ph.prase_html(utils.request_html(bangumi_url))
            anime_item["名称"] = anime_info_dict["动画信息"][0][hs.original_name]

            # 获取单集信息
            anime_ep_info_list += anime_info_dict["单集信息"]
            anime_info_list += anime_info_dict["动画信息"]

    with open(config_file, "w", encoding="utf-8") as f:
        json.dump(config_info, f, ensure_ascii=False, indent=4)

    # 保存到 csv 文件 headers_anime 和 headers_ep
    utils.save_csv(config_info["动画信息"], hs.anime_headers, anime_info_list)
    utils.save_csv(config_info["单集信息"], hs.episode_headers, anime_ep_info_list)
