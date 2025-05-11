# 读写文件
地址 = "D:/Projects/kumigumi/src/pythonDB/console.txt"

# 读取文件
内容 = ""
with open(地址, "r", encoding="utf-8") as f:
    内容 = f.read()

# 遍历每一行
for 行 in 内容.split("\n"):
    if 行.strip() == "" or not 行.startswith("//"):
        continue

    # 如果行不为空
    行 = 行[2:].split("/")
    print(行)
