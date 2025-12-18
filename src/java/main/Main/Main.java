package Main;


import Task.TaskManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            // TODO: ...
            System.out.println("Fetching...");
            // kg.addTaskFromArgs(args);
            // TaskManager.runTasks();
            // kg.UpsertDatabase();
        }
        else
        {
            // 先一次性读取 Excel 全部数据块，并分类
            kg.ReadExcel(excel_path);

            System.exit(0);
            switch(mode)
            {
            case "import":
            {
                // System.out.println("Importing from Excel...");
                // kg.UpsertExcelData();
                break;
            }
            case "fetch_excel":
            {
                // System.out.println("Fetching from Excel...");
                //
                // var tasks = kg.ParseFetchTaskFromBlock();
                // Multithreading(tasks);
                // // DataBuffer.SaveDataList(tasks);
                // kg.UpsertDatabase();
                break;
            }
            case "dt":
            {
                // System.out.println("Downloading torrents...");
                //
                // var tasks = kg.PraseTorrentDownloadTaskList(dt_path, "未下载");
                // Multithreading(tasks);

                break;
            }
            case "all":
            {

                System.out.println("Fetching & Importing...");

                // Step01. 创建下载任务
                List<TaskManager.Task> tasks = new ArrayList<>();
                tasks.addAll(kg.getFetchTask());               // 添加所有 fetch 任务
                tasks.addAll(kg.getDTTask(dt_path, "未下载")); // 添加 dt 任务

                TaskManager.addTask(tasks);

                // Step02. 执行执下载行任务，将获取的数据存到缓冲区
                TaskManager.runTasks();

                // 获取运行结果
                var completed_tasks = TaskManager.getCompletedTask();
                var failed_tasks    = TaskManager.getFailedTasks();
                if(!failed_tasks.isEmpty())
                {
                    System.err.println("以下任务执行失败:");
                    for(var task : failed_tasks)
                    {
                        System.err.println(task);
                    }
                }
                kg.addTaskRes(completed_tasks);

                // 执行数据库更新
                kg.toDatabase();

                kg.SaveLog();

                break;
            }

            default: break;
            }
        }

        System.out.println("Done.");
    }
}