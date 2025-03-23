# 请求html页面

import urllib.request  # noqa: E402


# 获取html页面
def request(url: str) -> str:
    print(f"正在请求 {url}")

    res = urllib.request.urlopen(url)
    if res.status != 200:
        print(f"请求失败：{url}")
        return None
    else:
        print(f"请求成功：{url}")
        return res.read().decode("utf-8")
