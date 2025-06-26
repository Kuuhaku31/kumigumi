import bangumi
import headers
import utils

url = "https://bangumi.tv/subject/328609"

html_str = utils.request_html(url)

anime_info, episode_info = bangumi.解析BangumiHTML_str(html_str)

print("动画信息:")
for key, value in anime_info.items():
    print(f"{key}: {value}")

print("\n单集信息:")
for episode in episode_info:
    print("\n单集:")
    for key, value in episode.items():
        print(f"  {key}: {value}")


utils.保存CSV文件("anime.csv", headers.番组表头, [anime_info])
utils.保存CSV文件("episode.csv", headers.单集表头, episode_info)
