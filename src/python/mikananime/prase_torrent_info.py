# prase_torrent_info.py


# 解析整理种子信息


def prase(group: str, torrent_info: dict) -> dict:

    # 字符串转数组
    disc_list: list = torrent_info["描述"].replace("【", "]").replace("】", "]").replace("[", "]").split("]")

    for i in disc_list:  # 去除空字符串
        if i.strip() == "":
            disc_list.remove(i)

    torrent_info["大小"] = disc_list.pop()

    if group == "喵萌奶茶屋":
        disc_list.pop()
        torrent_info["字幕"] = disc_list.pop()
        torrent_info["分辨率"] = disc_list.pop()
        torrent_info["集数"] = disc_list.pop()
    elif group == "ANi":
        torrent_info["格式"] = disc_list.pop()
        disc_list.pop()
        disc_list.pop()
        disc_list.pop()
        torrent_info["片源"] = disc_list.pop()
        torrent_info["分辨率"] = disc_list.pop()

    return torrent_info
