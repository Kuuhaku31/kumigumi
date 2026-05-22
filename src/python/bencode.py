from datetime import datetime, timezone

import bencodepy


def try_decode(b):
    if not isinstance(b, bytes):
        return b
    try:
        return b.decode("utf-8")
    except UnicodeDecodeError:
        # 避免 pieces 等二进制污染输出
        return f"<binary:{len(b)} bytes>"


def pretty_print(obj, indent=0):
    prefix = "  " * indent

    if isinstance(obj, dict):
        for k, v in obj.items():
            key = try_decode(k)
            print(f"{prefix}{key}:")
            pretty_print(v, indent + 1)

    elif isinstance(obj, list):
        for i, item in enumerate(obj):
            print(f"{prefix}[{i}]:")
            pretty_print(item, indent + 1)

    else:
        value = try_decode(obj)
        print(f"{prefix}{value}")

# 测试代码
with open("a.torrent", "rb") as f:
    data = bencodepy.decode(f.read())
    if b"creation date" in data:
        ts = data[b"creation date"]
        dt = datetime.fromtimestamp(ts, tz=timezone.utc)
        print(dt)

pretty_print(data)
