// Main.java

import Database.MySQLAccess;
import utils.Headers;
import utils.TableData;
import utils.Task;

import java.sql.SQLException;

static
String[][] MergeArray(String[][] a, String[][] b)
{
    String[][] temp = new String[a.length + b.length][];
    System.arraycopy(a, 0, temp, 0, a.length);
    System.arraycopy(b, 0, temp, a.length, b.length);
    return temp;
}


TableData[] GetTableDataList(Task[] task_list)
{
    // 执行
    for(Task task : task_list) task.GetInfo();

    // 合并数据
    String[][] anime_table_data   = new String[task_list.length][];
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
    TableData anime_table   = new TableData("anime", Headers.ANIME_HEADERS_SRC, anime_table_data);
    TableData episode_table = new TableData("episode", Headers.EPISODE_HEADERS_SRC, episode_table_data);
    TableData torrent_table = new TableData("torrent", Headers.TORRENT_HEADERS_SRC, torrent_table_data);

    return new TableData[]{anime_table, episode_table, torrent_table};
}


void UpsertDatabase(TableData[] upsert_data_list)
{
    // 插入数据库
    MySQLAccess dba = new MySQLAccess();
    try
    {
        dba.Open();
        dba.Upsert(upsert_data_list);
        dba.Close();

    }
    catch(SQLException e)
    {
        throw new RuntimeException(e);
    }
}

Task[] PraseTasks(String[] args)
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

    return tasks.toArray(new Task[0]);
}


void main(String[] args)
{
    IO.println("Hello, kumigumi!?");
    if(args.length > 0) IO.println(Arrays.toString(args));

    // Task[] tasks = {
    //     // new Task(455454, "https://mikanani.me/RSS/Bangumi?bangumiId=3698"),
    //     // new Task(507634, "https://mikanani.me/RSS/Bangumi?bangumiId=3774"),
    //     // new Task(508958, "https://mikanani.me/RSS/Bangumi?bangumiId=3783"),
    //     new Task(539395)
    // };

    Task[] tasks = PraseTasks(args);
    var    data  = GetTableDataList(tasks);
    UpsertDatabase(data);

    IO.println("Done.");
}
