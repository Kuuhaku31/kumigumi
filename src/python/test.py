import requests

url = "http://192.168.100.197:8000/"

utf8_str = ""

res = requests.get(url)

if res.status_code == 200:
    utf8_str = res.content.decode("utf-8")

    with open("response.html", "w", encoding="utf-8") as f:
        f.write(utf8_str)
