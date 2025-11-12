// Kumigumi.java


import Database.KG_SQLiteAccess;
import Excel.ExcelReader;
import utils.TableData.BlockData;
import utils.TableData.TableData;
import utils.task.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static utils.DataBuffer.SaveDataList;


public
class Kumigumi
{
    // 创建任务所需的数据表
    private final String[] ANIME_HEADERS_FETCH   = new String[] {
        "ANI_ID",
        "air_date",
        "title",
        "title_cn",
        "aliases",
        "description",
        "episode_count",
        "url_official_site",
        "url_cover",
    };
    private final String[] EPISODE_HEADERS_FETCH = new String[] {
        "EPI_ID",
        "ANI_ID",
        "sort",
        "air_date",
        "duration",
        "ep",
        "title",
        "title_cn",
        "description",
    };
    private final String[] TORRENT_HEADERS_FETCH = new String[] {
        "TOR_URL",
        "ANI_ID",
        "air_datetime",
        "size",
        "url_page",
        "title",
        "subtitle_group",
        "description",
    };

    private final TableData td_anime_fetch   = new TableData(ANIME_HEADERS_FETCH);
    private final TableData td_episode_fetch = new TableData(EPISODE_HEADERS_FETCH);
    private final TableData td_torrent_fetch = new TableData(TORRENT_HEADERS_FETCH);


    private final List<BlockData> block_list_fetch  = new ArrayList<>();
    private final List<BlockData> block_list_update = new ArrayList<>();

    public
    void ReadExcel(Path excel_path)
    {
        // 先一次性读取 Excel 全部数据块，并分类
        var blocks = ExcelReader.Read(excel_path);
        for(var block : blocks)
        {
            if(block.block_name.equals("fetch")) block_list_fetch.add(block);
            else block_list_update.add(block);
        }
    }


    // 将所有表数据保存到日志文件
    public
    void SaveLog()
    {
        List<TableData> list = new ArrayList<>();
        list.add(td_anime_fetch);
        list.add(td_episode_fetch);
        list.add(td_torrent_fetch);
        list.addAll(block_list_fetch);
        list.addAll(block_list_update);
        SaveDataList(list);
    }


    public
    void UpsertDatabase()
    {
        KG_SQLiteAccess dba = new KG_SQLiteAccess();
        dba.Open();

        dba.Upsert(KG_SQLiteAccess.TableName.anime, td_anime_fetch);
        dba.Upsert(KG_SQLiteAccess.TableName.episode, td_episode_fetch);
        dba.Upsert(KG_SQLiteAccess.TableName.torrent, td_torrent_fetch);

        // 将 Excel 的数据块写入数据库
        for(var block : block_list_update)
        {
            var table_name = KG_SQLiteAccess.TableName.Get(block.block_name);
            if(table_name != null) dba.Upsert(table_name, block);
        }

        dba.Close();
    }

    // 将 Excel 的数据块解析成 fetch 任务列表
    public
    List<KGTask> ParseFetchTaskFromBlock(BlockData block)
    {
        // 获取对应列号
        var i_ani_id  = block.GetHeaderIndex("ANI_ID");
        var i_rss_url = block.GetHeaderIndex("url_rss");

        // 生成不同任务
        List<KGTask> tasks = new ArrayList<>();
        if(i_ani_id >= 0)
            for(var data_row : block.GetData())
            {
                tasks.add(new TaskFetchAni(td_anime_fetch, Integer.parseInt(data_row[i_ani_id])));
                tasks.add(new TaskFetchEpi(td_episode_fetch, Integer.parseInt(data_row[i_ani_id])));
            }
        if(i_rss_url >= 0)
            for(var data_row : block.GetData())
            {
                tasks.add(new TaskFetchTor(td_torrent_fetch, Integer.parseInt(data_row[i_ani_id]), data_row[i_rss_url]));
            }
        return tasks;
    }

    public
    List<KGTask> ParseFetchTaskFromBlock()
    {
        List<KGTask> tasks = new ArrayList<>();
        for(var block : block_list_fetch)
        {
            tasks.addAll(ParseFetchTaskFromBlock(block));
        }
        return tasks;
    }

    // 将 Excel 的数据块解析成种子下载任务列表
    private
    List<KGTask> PraseTorrentDownloadTaskList(BlockData block, Path dt_path, String state)
    {
        // 获取列号
        var i_dt_url  = block.GetHeaderIndex("TOR_URL");
        var i_t_state = block.GetHeaderIndex("status_download");
        var data      = block.GetData();

        // 每个块的各个 TOR_URL : status_download
        List<KGTask> tasks = new ArrayList<>();
        for(var data_row : data)
        {
            if(data_row[i_t_state].equals(state)) tasks.add(new TaskDT(dt_path, data_row[i_dt_url]));
        }
        return tasks;
    }

    public
    List<KGTask> PraseTorrentDownloadTaskList(Path dt_path, String state)
    {
        List<KGTask> tasks = new ArrayList<>();
        for(var block : block_list_update)
        {
            if(block.block_name.equals("torrent")) tasks.addAll(PraseTorrentDownloadTaskList(block, dt_path, state));
        }
        return tasks;
    }

    // 将参数解析成任务列表
    public
    List<KGTask> ParseTaskFromArgs(String[] args)
    {
        List<KGTask> tasks = new ArrayList<>();
        for(int i = 0; i < args.length; i++)
        {
            if(args[i].startsWith("-a")) // 如果开头两个字符是 "-a"
            {
                // 获取 anime_id
                var ani_id = Integer.parseInt(args[i].substring(2));

                // 继续判断下一个参数开头是不是 "-r"
                String rss_link_str = null;
                if(i + 1 < args.length && args[i + 1].startsWith("-r")) { rss_link_str = args[i + 1].substring(2); }

                // 添加不同类型任务
                tasks.add(new TaskFetchAni(td_anime_fetch, ani_id));
                tasks.add(new TaskFetchEpi(td_episode_fetch, ani_id));
                if(rss_link_str != null) tasks.add(new TaskFetchTor(td_torrent_fetch, ani_id, rss_link_str));
            }
        }
        return tasks;
    }
}