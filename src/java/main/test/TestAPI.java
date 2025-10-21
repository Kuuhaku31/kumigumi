// TestAPI.java

package test;

import Database.MySQLAccess;
import utils.Headers;
import utils.TableData;
import utils.Task;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public
class TestAPI
{
    static
    void main(String[] args) throws SQLException
    {

        IO.println(Arrays.toString(args));
        IO.println("----- Creating Task -----");

        Task[] task_list = {
            // new Task(455454, "https://mikanani.me/RSS/Bangumi?bangumiId=3698"),
            // new Task(507634, "https://mikanani.me/RSS/Bangumi?bangumiId=3774")
            new Task(508958, "https://mikanani.me/RSS/Bangumi?bangumiId=3783")
        };

        IO.println("----- Running Task -----");

        for(Task task : task_list) task.GetInfo();

        IO.println("----- Finished Task -----");

        ArrayList<ArrayList<String>> anime_info_list   = new ArrayList<>();
        ArrayList<ArrayList<String>> episode_info_list = new ArrayList<>();
        ArrayList<ArrayList<String>> torrent_info_list = new ArrayList<>();

        for(Task task : task_list)
        {
            anime_info_list.addAll(task.anime_info_list);
            episode_info_list.addAll(task.episode_info_list);
            torrent_info_list.addAll(task.torrent_info_list);
        }


        String[][] anime_table_data = new String[anime_info_list.size()][Headers.ANIME_HEADERS_SRC.length];
        for(int i = 0; i < anime_info_list.size(); i++)
        {
            for(int j = 0; j < anime_info_list.get(i).size(); j++)
            {
                anime_table_data[i][j] = anime_info_list.get(i).get(j);
            }
        }
        TableData anime_table = new TableData("anime", Headers.ANIME_HEADERS_SRC, anime_table_data);

        String[][] episode_table_data = new String[episode_info_list.size()][Headers.EPISODE_HEADERS_SRC.length];
        for(int i = 0; i < episode_info_list.size(); i++)
        {
            for(int j = 0; j < episode_info_list.get(i).size(); j++)
            {
                episode_table_data[i][j] = episode_info_list.get(i).get(j);
            }
        }
        TableData episode_table = new TableData("episode", Headers.EPISODE_HEADERS_SRC, episode_table_data);

        String[][] torrent_table_data = new String[torrent_info_list.size()][Headers.TORRENT_HEADERS_SRC.length];
        for(int i = 0; i < torrent_info_list.size(); i++)
        {
            for(int j = 0; j < torrent_info_list.get(i).size(); j++)
            {
                torrent_table_data[i][j] = torrent_info_list.get(i).get(j);
            }
        }
        TableData torrent_table = new TableData("torrent", Headers.TORRENT_HEADERS_SRC, torrent_table_data);

        // 插入数据库
        MySQLAccess dba = new MySQLAccess();
        dba.Open();

        dba.Upsert(anime_table);
        dba.Upsert(episode_table);
        dba.Upsert(torrent_table);

        dba.Close();
    }


}
