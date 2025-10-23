// Method.java


package utils;

import Database.MySQLAccess;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public
class Method
{

    private static
    String[][] MergeArray(String[][] a, String[][] b)
    {
        String[][] temp = new String[a.length + b.length][];
        System.arraycopy(a, 0, temp, 0, a.length);
        System.arraycopy(b, 0, temp, a.length, b.length);
        return temp;
    }


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
        for(Task task : task_list)
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
    ArrayList<TableData> RunTasks(ArrayList<Task> task_list)
    {
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
        data.add(new TableData("anime", Headers.ANIME_HEADERS_SRC, anime_table_data));
        data.add(new TableData("episode", Headers.EPISODE_HEADERS_SRC, episode_table_data));
        data.add(new TableData("torrent", Headers.TORRENT_HEADERS_SRC, torrent_table_data));

        return data;
    }
    

    public static
    void UpsertDatabase(ArrayList<TableData> upsert_data_list)
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


    public static
    ArrayList<TableData> RunFetchArgs(String[] args)
    {
        ArrayList<Task> tasks = new ArrayList<>();
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


    public static
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
}
