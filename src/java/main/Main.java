// Main.java


import utils.TableData;

import static Excel.ExcelReader.Read;
import static utils.Method.*;


void main(String[] args)
{
    System.setProperty("java.net.useSystemProxies", "true"); // 设置全局代理

    var help_msg       = "Usage: kumigumi fetch -a<anime_id> [-r<rss_link>] [...]";
    var dt_path        = Path.of("C:/Users/kuuhaku-kzs/Downloads/dt/");
    var def_excel_path = Path.of("D:/OneDrive/kumigumi.xlsx");

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
        var block_list_fetch            = new ArrayList<TableData>();
        var block_list_import           = new ArrayList<TableData>();
        var block_list_torrent_download = new ArrayList<TableData>();

        var block_list = Read(args.length > 1 ? Path.of(args[1]) : def_excel_path);
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

            var data = RunFetchBlocks(block_list_fetch);
            UpsertDatabase(data);
            break;
        }
        case "dt":
        {
            System.out.println("Downloading torrents...");

            var dt_url_list = PraseTorrentDownloadList(block_list_torrent_download);
            var failed_urls = DownloadAll(dt_url_list, dt_path);

            for(var url : failed_urls)
            {
                System.err.println("下载失败: " + url);
            }

            break;
        }
        case "all":
        {
            IO.println("Fetching & Importing...");

            var data = new ArrayList<TableData>();
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