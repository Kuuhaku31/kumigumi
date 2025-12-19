package Main;

import Task.TaskManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Main {

    Main() {
        excel_path = Path.of(System.getenv("KG_EXCEL_PATH"));
        database_path = Path.of(System.getenv("KG_DATABASE_PATH"));
        dt_path = Path.of(System.getenv("KG_DT_PATH"));
    }

    private Path excel_path; // Excel 文件路径
    private Path database_path; // 数据库文件路径
    private Path dt_path; // 下载路径
    private String mode = null; // 运行模式

    private final String help_msg = "Usage: kumigumi fetch -a<anime_id> [-r<rss_link>] [...]";

    // 解析命令行参数
    private void parseArgs(String[] args) {
        if (args.length <= 0) {
            System.out.println(help_msg);
            return;
        }

        System.out.println(Arrays.toString(args));
        for (int i = 0; i < args.length; i++) {
            var arg = args[i];
            if (arg.startsWith("-ex")) {
                excel_path = Path.of(arg.substring(3));
            } else if (arg.startsWith("-db")) {
                database_path = Path.of(arg.substring(3));
            } else if (arg.startsWith("-dt")) {
                dt_path = Path.of(arg.substring(3));
            } else if (arg.equals("--help") || arg.equals("-h")) {
                mode = "help";
            } else if (i == 0) {
                mode = arg;
            }
        }
    }

    // 创建路径
    private void createDTPath() {
        if (Files.notExists(dt_path)) {
            try {
                Files.createDirectories(dt_path);
            } catch (IOException e) {
                System.err.println("无法创建下载路径: " + e.getMessage());
            }
        }
    }

    private void mode() {
        var kg = new Kumigumi();

        kg.ReadExcel(excel_path); // 先一次性读取 Excel 全部数据块，并分类

        switch (mode) {
            case "import": {
                // System.out.println("Importing from Excel...");
                // kg.UpsertExcelData();
                break;
            }
            case "fetch_excel": {
                // System.out.println("Fetching from Excel...");
                //
                // var tasks = kg.ParseFetchTaskFromBlock();
                // Multithreading(tasks);
                // // DataBuffer.SaveDataList(tasks);
                // kg.UpsertDatabase();
                break;
            }
            case "dt": {
                // System.out.println("Downloading torrents...");
                //
                // var tasks = kg.PraseTorrentDownloadTaskList(dt_path, "未下载");
                // Multithreading(tasks);

                break;
            }
            case "all": {

                System.out.println("Fetching & Importing...");

                // Step01. 创建下载任务
                List<TaskManager.Task> tasks = new ArrayList<>();
                tasks.addAll(kg.getFetchTask()); // 添加所有 fetch 任务
                tasks.addAll(kg.getDTTask(dt_path, "未下载")); // 添加 dt 任务

                TaskManager.addTask(tasks);

                // Step02. 执行执下载行任务，将获取的数据存到缓冲区
                TaskManager.runTasks();

                // 获取运行结果
                var completed_tasks = TaskManager.getCompletedTask();
                var failed_tasks = TaskManager.getFailedTasks();
                if (!failed_tasks.isEmpty()) {
                    System.err.println("以下任务执行失败:");
                    for (var task : failed_tasks) {
                        System.err.println(task);
                    }
                }
                kg.addTaskRes(completed_tasks);

                // 执行数据库更新
                kg.toDatabase();

                kg.SaveLog();

                break;
            }

            default:
                break;
        }
    }

    // kumigumi 入口函数
    void main(String[] args) {
        System.out.println("Hello, kumigumi!?");

        System.setProperty("java.net.useSystemProxies", "true"); // 设置全局代理

        parseArgs(args);

        if (mode == null)
            System.out.println(help_msg);
        else if (mode.equals("help"))
            System.out.println(help_msg);
        else {
            createDTPath();
            mode();
        }
        System.out.println("Done.");
    }
}