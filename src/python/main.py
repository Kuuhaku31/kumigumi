# main.py


import warnings

from database.database import 数据处理, 更新数据库
from excel_reader import ExcelReader
from fetch import 批量下载种子, 批量获取番组及单集数据, 批量获取种子数据
from utils import kumigumiPrint, safe_load, 合并数据, 获取OneDrive路径

if __name__ == "__main__":

    kumigumiPrint("开始执行脚本...")

    warnings.filterwarnings("ignore", category=UserWarning)

    excel_path = 获取OneDrive路径() / "kumigumi.xlsx"
    kumigumiPrint(f"📖 读取 Excel 文件: {excel_path}")

    excel_reader = ExcelReader(safe_load(excel_path))

    # 获取更新数据
    update_data: dict[str, list[list[list[str]]]] = {}  # 数据库表名 : [ 工作表数据1, 工作表数据2, ... ]
    for 数据库表名, 工作表名 in excel_reader.更新数据参数:
        data: list[list[str]] = excel_reader.获取工作表数据(工作表名)
        update_data.setdefault(数据库表名, []).append(data)

    # 合并工作表数据
    合并后的数据字典: dict[str, list[list[str]]] = {}
    for 数据库表名, 工作表数据列表 in update_data.items():
        for 工作表数据 in 工作表数据列表:
            合并后的数据字典.setdefault(数据库表名, [[]])
            合并后的数据字典[数据库表名] = 合并数据(合并后的数据字典[数据库表名], 工作表数据)

    # 更新数据库
    for 数据库表名, 工作表数据 in 合并后的数据字典.items():
        kumigumiPrint(f"更新数据库: {excel_reader.数据库地址} - {数据库表名}")
        更新数据库(excel_reader.数据库地址, 数据库表名, 工作表数据)

    # 获取工作表中的 bgm_url 和 rss_url 映射
    bgm_url_rss_映射: dict[str, str] = {}
    for 工作表名 in excel_reader.获取数据参数:
        bgm_url_rss_映射.update(excel_reader.读取sheet获取bgm_url_rss_映射(工作表名))

    # 批量获取数据
    kumigumiPrint("🔄 开始批量获取番组及单集数据...")
    动画信息列表, 单集信息列表 = 批量获取番组及单集数据(bgm_url_rss_映射.keys())
    种子信息列表 = 批量获取种子数据(bgm_url_rss_映射)
    动画数据, 单集数据, 种子数据 = 数据处理(动画信息列表, 单集信息列表, 种子信息列表)
    kumigumiPrint("🔄 批量获取番组及单集数据完成")

    # 更新数据库
    更新数据库(excel_reader.数据库地址, excel_reader.数据库anime表名, 动画数据)
    更新数据库(excel_reader.数据库地址, excel_reader.数据库episode表名, 单集数据)
    更新数据库(excel_reader.数据库地址, excel_reader.数据库torrent表名, 种子数据)

    # 获取需要下载的种子链接
    torrent_url_list = excel_reader.获取下载种子url列表()
    if len(torrent_url_list) > 0:
        kumigumiPrint(f"🔄 开始批量下载种子: {len(torrent_url_list)} 个链接")
        批量下载种子(excel_reader.种子下载地址, torrent_url_list)

    kumigumiPrint("所有操作完成")
