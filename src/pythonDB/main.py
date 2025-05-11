# Python + SQLite

# import bangumi
import ctrlDB

# import utils

数据库地址 = "D:/Projects/kumigumi/src/pythonDB/test.db"

# ctrlDB.初始化数据库(数据库地址)

# res = bangumi.解析bangumiHTML(utils.获取HTML内容("https://bangumi.tv/subject/485936"))

# ctrlDB.插入或更新表(数据库地址, "anime", utils.anime表表头, res[0])
# for i in range(1, len(res)):
#     ctrlDB.插入或更新表(数据库地址, "episodes", utils.episodes表表头, res[i])

# 查询

ID = "485936"
print(ctrlDB.利用ID查询话番组计划网址(数据库地址, ID))
