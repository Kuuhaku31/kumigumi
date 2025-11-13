package Main;


import utils.task.KGTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static utils.Util.Multithreading;

public
class Main
{
    // kumigumi 入口函数
    void main(String[] args)
    {
        var kg = new Kumigumi();

        System.setProperty("java.net.useSystemProxies", "true"); // 设置全局代理

        var help_msg   = "Usage: kumigumi fetch -a<anime_id> [-r<rss_link>] [...]";
        var excel_path = args.length > 1 ? Path.of(args[1]) : Path.of("D:/OneDrive/kumigumi.xlsx");

        // 创建路径
        var dt_path = Path.of("D:/Downloads/dt/");
        if(Files.notExists(dt_path))
        {
            try { Files.createDirectories(dt_path); }
            catch(IOException e) { System.err.println("无法创建下载路径: " + e.getMessage()); }
        }

        System.out.println("Hello, kumigumi!?");
        if(args.length > 0) System.out.println(Arrays.toString(args));
        else return;

        String mode = args[0];
        if(mode.equals("help")) System.out.println(help_msg);
        else if(mode.equals("fetch"))
        {
            System.out.println("Fetching...");
            var tasks = kg.ParseTaskFromArgs(args);
            Multithreading(tasks);
            kg.UpsertDatabase();
        }
        else
        {
            // 先一次性读取 Excel 全部数据块，并分类
            kg.ReadExcel(excel_path);

            switch(mode)
            {
            case "import":
            {
                System.out.println("Importing from Excel...");
                // kg.UpsertExcelData();
                break;
            }
            case "fetch_excel":
            {
                System.out.println("Fetching from Excel...");

                var tasks = kg.ParseFetchTaskFromBlock();
                Multithreading(tasks);
                // DataBuffer.SaveDataList(tasks);
                kg.UpsertDatabase();
                break;
            }
            case "dt":
            {
                System.out.println("Downloading torrents...");

                var tasks = kg.PraseTorrentDownloadTaskList(dt_path, "未下载");
                Multithreading(tasks);

                break;
            }
            case "all":
            {
                System.out.println("Fetching & Importing...");

                List<KGTask> tasks = new ArrayList<>();
                tasks.addAll(kg.ParseFetchTaskFromBlock());                       // 添加所有 fetch 任务
                tasks.addAll(kg.PraseTorrentDownloadTaskList(dt_path, "未下载")); // 添加 dt 任务

                Multithreading(tasks); // 一次性执行所有任务

                for(var task : tasks) if(!task.IsCompleted()) System.out.println(task);

                kg.UpsertDatabase(); // 更新数据库

                kg.SaveLog();

                break;
            }

            default: break;
            }
        }

        System.out.println("Done.");
    }
}