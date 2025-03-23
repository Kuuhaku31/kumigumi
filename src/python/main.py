# main.py

import bangumi.update as up
import mikananime.config as mk_config
import mikananime.prase_rss_html as mk_ph
import mikananime.print_torrent_items as mk_pti
import utils


def f1():
    mk_config.init("data/config.json")
    json = mk_config.get_config()
    url = json["url_list"][0]

    html_str = utils.request_html(url)
    dict = mk_ph.prase(html_str)
    mk_pti.print_dict(dict)


up.update_csv(
    utils.get_json("./data/urls.json")["url_list"],
    "./data/anime.csv",
    "./data/episode.csv",
)
