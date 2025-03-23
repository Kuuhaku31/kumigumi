# main.py

import mikananime.config as mk_config
import mikananime.prase_rss_html as mk_ph
import mikananime.print_torrent_items as mk_pti
import request_html.request_html as rh

mk_config.init("data/config.json")
json = mk_config.get_config()
url = json["url_list"][0]

html_str = rh.request(url)
dict = mk_ph.prase(html_str)
mk_pti.print_dict(dict)
