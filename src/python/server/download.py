# download.py

import requests

base_url = "http://192.168.1.100:8000/"

file_list = ["file1.txt", "file2.jpg", "file3.pdf"]

for filename in file_list:
    url = base_url + filename
    print(f"正在下载：{filename}")
    r = requests.get(url)
    if r.status_code == 200:
        with open(filename, "wb") as f:
            f.write(r.content)
        print("下载成功")
    else:
        print(f"❌ 失败：{r.status_code}")
