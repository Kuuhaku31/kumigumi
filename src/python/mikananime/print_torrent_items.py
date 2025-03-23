# 打印字典里的种子信息
def print_dict(dict):
    for key, value in dict.items():
        print(f"{key}:")
        for item in value:
            for k, v in item.items():
                print(f"{k}: {v}")
        print()
