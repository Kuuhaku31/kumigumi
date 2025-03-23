import csv

from bangumi import ba


def f1():
    url = "https://bangumi.tv/subject/389156"

    print("test")
    html = ba.get_html(url)

    with open("data/test.html", "w", encoding="utf-8") as f:
        f.write(html)


def f2():
    res = None

    with open("data/test.html", "r", encoding="utf-8") as f:
        html = f.read()
        res = ba.prase_html(html)

    for key, value in res.items():
        print(f"{key}: {value}")

    with open("data/data.csv", "w+", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(ba.headers_anime)
        writer.writerow(res.values())


# f1()
# f2()
ba.update_csv("data/urls.json", "data/data.csv", "data/data_ep.csv")
