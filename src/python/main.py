import base64
import hashlib
import json
import pprint
from collections.abc import Mapping

import bencode

with open("a.torrent", "rb") as f:
    torrent: dict = bencode.bdecode(f.read())

# 列出 torrent 文件中的所有键
print("所有建:", list(torrent.keys()))
# print(torrent)

info = torrent["info"]
info_bencoded = bencode.bencode(info)

info_hash = hashlib.sha1(info_bencoded).hexdigest()
print(info_hash)

print("-----------------------")

# 美观打印所有内容
pprint.pprint(torrent)


# 转换成 json 并保存
def to_json_safe(value):
    if isinstance(value, bytes):
        try:
            return value.decode("utf-8")
        except UnicodeDecodeError:
            return "base64:" + base64.b64encode(value).decode("ascii")
    if isinstance(value, Mapping):
        return {to_json_safe(k): to_json_safe(v) for k, v in value.items()}
    if isinstance(value, list):
        return [to_json_safe(item) for item in value]
    return value


with open("tmp_torrent.json", "w", encoding="utf-8") as f:
    json.dump(to_json_safe(torrent), f, indent=4, ensure_ascii=False)
