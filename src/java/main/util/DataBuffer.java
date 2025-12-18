package util;


import util.TableData.TableData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 数据缓存
public
class DataBuffer
{
    public final List<TableData> table_fetch = new ArrayList<>();
    public final List<TableData> table_dt    = new ArrayList<>();

    public final List<TableData> table_update_ani = new ArrayList<>();
    public final List<TableData> table_update_epi = new ArrayList<>();
    public final List<TableData> table_update_tor = new ArrayList<>();

    // 缓存从网站获取的数据
    public final List<Map<String, String>> anime_fetch   = new ArrayList<>();
    public final List<Map<String, String>> episode_fetch = new ArrayList<>();
    public final List<Map<String, String>> torrent_fetch = new ArrayList<>();


    public
    void printExcelInfo()
    {
        for(var tableFetch : table_fetch) System.out.println(tableFetch.toString());
        for(var tableDt : table_dt) System.out.println(tableDt.toString());
    }
}

