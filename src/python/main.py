import hashlib

import bencode

with open("5e47d539c1793538aaf9f3dcd02fdc77e118fa9a.torrent", "rb") as f:
    torrent: dict = bencode.bdecode(f.read())

# 列出 torrent 文件中的所有键
print("所有建:", list(torrent.keys()))
# print(torrent)

info = torrent["info"]
info_bencoded = bencode.bencode(info)

info_hash = hashlib.sha1(info_bencoded).hexdigest()
print(info_hash)
