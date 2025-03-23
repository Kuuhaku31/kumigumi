# main.py

import bangumi.update as up
import mikananime.prase_rss_html as mk_ph
import utils


def f1():
    url = "https://mikanani.me/RSS/Bangumi?bangumiId=2702"
    html_str = utils.request_html(url)
    dict = mk_ph.prase(html_str)

    torrent_list = []
    for group in dict:
        for item in dict[group]:
            item["字幕组"] = group
            torrent_list.append(item)

    for torrent in torrent_list:
        print(torrent)

    utils.save_csv("./data/torrent.csv", ["字幕组", "标题", "描述", "发布日期", "链接"], torrent_list)


def f2():
    up.update_csv(
        "./data/kumigumi.json",
        "./data/anime.csv",
        "./data/episode.csv",
    )


f1()
