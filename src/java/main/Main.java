// Main.java


import utils.TableData;

import static Excel.ExcelReader.Read;
import static utils.Method.*;


// 解析出种子下载链接列表

ArrayList<String> PraseTorrentDownloadList(ArrayList<TableData> block_list)
{
    ArrayList<String> dt_list = new ArrayList<>();
    for(var data : block_list)
    {
        // 对于每个块
        int i_dt_url  = -1;
        int i_t_state = -1;
        for(int i = 0; i < data.headers().length; i++)
        {
            if(data.headers()[i].equals("TOR_URL")) i_dt_url = i;
            if(data.headers()[i].equals("status_download")) i_t_state = i;
        }
        if(i_dt_url == -1 || i_t_state == -1) continue;

        // 每个块的各个 TOR_URL : status_download
        for(var data_row : data.data()) if(data_row[i_t_state].equals("未下载")) dt_list.add(data_row[i_dt_url]);
    }
    return dt_list;
}


void main(String[] args) throws IOException
{
    System.setProperty("java.net.useSystemProxies", "true"); // 设置全局代理
    
    String help_msg       = "Usage: kumigumi fetch -a<anime_id> [-r<rss_link>] [...]";
    String def_excel_path = "D:/OneDrive/kumigumi.xlsx";

    IO.println("Hello, kumigumi!?");
    if(args.length > 0) IO.println(Arrays.toString(args));
    else return;

    String mode = args[0];
    if(mode.equals("help")) IO.println(help_msg);
    else if(mode.equals("fetch"))
    {
        IO.println("Fetching...");
        var data = RunFetchArgs(args);
        UpsertDatabase(data);
    }
    else
    {
        // 先一次性读取 Excel 全部数据块，并分类
        ArrayList<TableData> block_list_fetch            = new ArrayList<>();
        ArrayList<TableData> block_list_import           = new ArrayList<>();
        ArrayList<TableData> block_list_torrent_download = new ArrayList<>();

        var block_list = Read(args.length > 1 ? args[1] : def_excel_path);
        for(var block : block_list)
        {
            switch(block.table_name())
            {
            case "fetch" -> block_list_fetch.add(block);
            case "torrent" ->
            {
                block_list_import.add(block);
                block_list_torrent_download.add(block);
            }
            default -> block_list_import.add(block);
            }
        }

        switch(mode)
        {
        case "import":
        {
            IO.println("Importing from Excel...");
            UpsertDatabase(block_list_import);
            break;
        }
        case "fetch_excel":
        {
            IO.println("Fetching from Excel...");

            ArrayList<TableData> data = RunFetchBlocks(block_list_fetch);
            UpsertDatabase(data);
            break;
        }
        case "dt":
        {
            var dt_url_list = PraseTorrentDownloadList(block_list_torrent_download);

            break;
        }
        case "all":
        {
            IO.println("Fetching & Importing...");

            ArrayList<TableData> data = new ArrayList<>();
            data.addAll(block_list_import);
            data.addAll(RunFetchBlocks(block_list_fetch));
            UpsertDatabase(data);
            break;
        }

        default: break;
        }
    }

    IO.println("Done.");
}