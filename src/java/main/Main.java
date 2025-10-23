// Main.java


import utils.TableData;

import static Excel.ExcelReader.Read;
import static utils.Method.*;


void main(String[] args) throws IOException
{
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
            case "dt" -> block_list_torrent_download.add(block);
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