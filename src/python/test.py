import bangumi
import utils

url = "https://bangumi.tv/subject/436738"

html_str = utils.request_html(url)

anime_info, episode_info = bangumi.解析BangumiHTML_str(html_str)

print("动画信息:")
for key, value in anime_info.items():
    print(f"{key}: {value}")

print("\n单集信息:")
for episode in episode_info:
    for key, value in episode.items():
        print(f"  {key}: {value}")
