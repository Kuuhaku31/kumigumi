// Kumigumi.java


import Database.MySQLAccess;
import utils.DataBuffer;
import utils.TableData;
import utils.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static Excel.ExcelReader.Read;


public
class Kumigumi
{
    // 控制台进度条显示函数
    private static synchronized
    void showProgress(int done, int total)
    {
        int    percent = (int) ((done * 100.0f) / total);
        int    barLen  = 30;
        int    filled  = percent * barLen / 100;
        String bar     = "=".repeat(filled) + " ".repeat(barLen - filled);
        System.out.printf("\r[%s] %3d%% (%d/%d)", bar, percent, done, total);
        if(done == total) System.out.println();
    }

    private static
    ArrayList<TableData> RunFetchBlocks(ArrayList<TableData> block_list)
    {
        ArrayList<Task> tasks = new ArrayList<>();
        for(var data : block_list)
        {
            // 对于每个块
            int i_ani_id  = -1;
            int i_rss_url = -1;
            for(int i = 0; i < data.headers().length; i++)
            {
                if(data.headers()[i].equals("ANI_ID")) i_ani_id = i;
                if(data.headers()[i].equals("url_rss")) i_rss_url = i;
            }
            if(i_ani_id == -1 || i_rss_url == -1) continue;

            // 每个块的各个 ani_id : rss_url
            for(var data_row : data.data())
            {
                tasks.add(new Task((int) Double.parseDouble(data_row[i_ani_id]), data_row[i_rss_url]));
            }
        }
        return RunTasks(tasks);
    }

    /**
     *
     * 批量执行任务，用多线程
     *
     */
    private static
    void Multithreading(ArrayList<Task> task_list) throws InterruptedException
    {
        IO.println("开始并发执行任务");

        int           total    = task_list.size();
        AtomicInteger finished = new AtomicInteger(0);

        // 固定大小线程池
        ExecutorService pool = Executors.newFixedThreadPool(4);

        // 提交任务
        for(var task : task_list)
        {
            pool.submit(() ->
            {
                task.Run();
                int done = finished.incrementAndGet();
                showProgress(done, total);
            });
        }

        pool.shutdown();
        var res = pool.awaitTermination(1, TimeUnit.HOURS); // 等待全部完成
        IO.println(res);

        IO.println("并发任务完成！");
    }

    private static
    void UpsertDatabase(List<TableData> upsert_data_list)
    {
        // 插入数据库
        MySQLAccess dba = new MySQLAccess();
        try
        {
            dba.Open();
            dba.Upsert(upsert_data_list.toArray(new TableData[0]));
            dba.Close();

        }
        catch(SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static
    ArrayList<TableData> RunTasks(ArrayList<Task> task_list)
    {
        // 表头
        String[] ANIME_HEADERS_SRC   = {"ANI_ID", "air_date", "title", "title_cn", "aliases", "episode_count", "url_official_site", "url_cover"};
        String[] EPISODE_HEADERS_SRC = {"EPI_ID", "ANI_ID", "air_date", "duration", "index", "title", "title_cn", "description"};
        String[] TORRENT_HEADERS_SRC = {"TOR_URL", "ANI_ID", "air_datetime", "size", "url_page", "title", "subtitle_group", "description"};

        // 执行
        try { Multithreading(task_list); }
        catch(InterruptedException e) { IO.println("Error: " + e); }

        // 合并数据
        String[][] anime_table_data   = new String[task_list.size()][];
        String[][] episode_table_data = new String[0][];
        String[][] torrent_table_data = new String[0][];

        int i = 0;
        for(Task task : task_list)
        {
            anime_table_data[i++] = task.anime_info_list;

            episode_table_data = MergeArray(episode_table_data, task.episode_info_list);
            torrent_table_data = MergeArray(torrent_table_data, task.torrent_info_list);
        }

        // 创建 TableData 对象
        ArrayList<TableData> data = new ArrayList<>();
        data.add(new TableData("anime", ANIME_HEADERS_SRC, anime_table_data));
        data.add(new TableData("episode", EPISODE_HEADERS_SRC, episode_table_data));
        data.add(new TableData("torrent", TORRENT_HEADERS_SRC, torrent_table_data));

        return data;
    }

    private static
    String[][] MergeArray(String[][] a, String[][] b)
    {
        String[][] temp = new String[a.length + b.length][];
        System.arraycopy(a, 0, temp, 0, a.length);
        System.arraycopy(b, 0, temp, a.length, b.length);
        return temp;
    }

    // 解析出种子下载链接列表
    private static
    List<String> PraseTorrentDownloadList(List<TableData> block_list)
    {
        var dt_list = new ArrayList<String>();
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

    /**
     * 下载所有 URL
     * <p>
     * 返回下载失败的 URL 列表
     */
    private static
    List<String> DownloadAll(List<String> urls, Path downloadDir)
    {
        // 创建文件夹
        try { Files.createDirectories(downloadDir); }
        catch(IOException e)
        {
            System.err.println("无法创建下载目录: " + e.getMessage());
            return urls; // 返回所有 URL 作为失败列表
        }

        var client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS) // 自动跟随重定向
            .connectTimeout(Duration.ofSeconds(10))      // 设置连接超时
            .build();

        var executor   = Executors.newFixedThreadPool(8); // 固定大小线程池
        var failedUrls = new ConcurrentLinkedQueue<String>();        // 用于保存下载失败的 URL

        // 进度跟踪
        int total    = urls.size();
        var finished = new AtomicInteger(0);

        // 提交下载任务
        for(String url : urls)
        {
            executor.submit(() ->
            {
                try
                {
                    // 构建 URI 和请求
                    var uri        = URI.create(url);
                    var fileName   = Paths.get(uri.getPath()).getFileName().toString();
                    var targetPath = downloadDir.resolve(fileName);

                    var request  = HttpRequest.newBuilder(uri).GET().build();
                    var response = client.send(request, HttpResponse.BodyHandlers.ofFile(targetPath));

                    if(response.statusCode() != 200)
                    {
                        failedUrls.add(url);
                    }
                }
                catch(Exception e)
                {
                    System.err.println("下载异常：" + url + " → " + e.getMessage());
                    failedUrls.add(url);
                }

                showProgress(finished.incrementAndGet(), total);
            });
        }

        // 等待所有任务完成
        executor.shutdown();

        try
        {
            if(executor.awaitTermination(1, TimeUnit.HOURS)) System.out.println("所有下载任务已完成");
            else System.out.println("下载任务超时");
        }
        catch(InterruptedException e) { System.err.println("等待下载任务完成时被中断: " + e.getMessage()); }

        // 返回失败列表
        return new ArrayList<>(failedUrls);
    }

    private static
    ArrayList<TableData> RunFetchArgs(String[] args)
    {
        var tasks = new ArrayList<Task>();
        for(int i = 0; i < args.length; i++)
        {
            // 如果开头两个字符是 "-a"
            if(args[i].startsWith("-a"))
            {
                String ani_id_str   = args[i].substring(2);
                int    ani_id       = Integer.parseInt(ani_id_str);
                String rss_link_str = null;

                // 继续判断下一个参数开头是不是 "-r"
                if(i + 1 < args.length && args[i + 1].startsWith("-r"))
                {
                    rss_link_str = args[i + 1].substring(2);
                }

                tasks.add(new Task(ani_id, rss_link_str));
            }
        }
        return RunTasks(tasks);
    }

    static
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
                DataBuffer.SaveDataList(data);
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
}