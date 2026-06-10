package Main;

import java.util.Map;
import java.util.Set;

import Database.AnimeInfo;
import Database.EpisodeInfo;
import Database.EpisodeRecordInfo;
import Database.RSSInfo;
import Database.SQLiteAccess;
import Database.TorrentDownloader;
import Database.TorrentInfo;
import Database.TorrentPageInfo;
import Task.FetchAnimeInfoTask;
import Task.FetchEpisodeInfoTask;
import Task.FetchTorrentInfoTask;
import Task.FetchTorrentPageTask;
import Task.Task;
import Util.TableData;


public class TestMain {
    public static void main(String[] args) throws Exception {
        System.out.println("TestExcel...");

        // 从Excel读取数据
        Map<String, TableData> blockDataList = new java.util.HashMap<>();
        {
            var e_reader = new Excel.ExcelReader();
            var res      = e_reader.Read("./db/test_exc.xlsx");

            System.out.println("从Excel读取的数据:");
            blockDataList = res.blockDataList();
            for(var entry : blockDataList.entrySet()) {
                System.out.println("Block Name: " + entry.getKey());
                System.out.println("Block Data:\n" + entry.getValue().toString());
            }
        }

        // 获取 EpisodeRecordInfo 实例
        Set<EpisodeRecordInfo> epi_recode_info_set = new java.util.HashSet<>();
        {
            epi_recode_info_set = EpisodeRecordInfo.ParseEpisodeRecordInfoByTableData(blockDataList.get("BlockEpisode"));
            System.out.println("从Excel创建的EpisodeRecordInfo实例:");
            for(var epi_record_info : epi_recode_info_set) {
                System.out.println(epi_record_info.toString());
            }
        }

        // 获取 RSSInfo 实例
        Set<RSSInfo> rss_info_set = new java.util.HashSet<>();
        {
            rss_info_set = RSSInfo.ParseRSSInfoByTableData(blockDataList.get("BlockRSS"));
            System.out.println("从Excel创建的RSSInfo实例:");
            for(var rss_info : rss_info_set) {
                System.out.println(rss_info.toString());
            }
        }

        // 获取信息
        System.out.println("开始获取 torrent page, anime 和 episode 信息...");
        Set<Task>            fetch_task_set        = new java.util.HashSet<>();
        Set<TorrentPageInfo> torrent_page_info_set = new java.util.HashSet<>();
        Set<AnimeInfo>       anime_info_set        = new java.util.HashSet<>();
        Set<EpisodeInfo>     episode_info_set      = new java.util.HashSet<>();
        {

            // 创建任务
            for(var rss_info : rss_info_set) {
                var anime_id = rss_info.ANI_ID;
                fetch_task_set.add(new FetchAnimeInfoTask(anime_id));
                fetch_task_set.add(new FetchEpisodeInfoTask(anime_id));
                fetch_task_set.add(new FetchTorrentPageTask(rss_info.URL_RSS));
            }

            // 并行执行任务
            Task.ParallelExecution(fetch_task_set);

            // 收集结果
            for(var task : fetch_task_set) {
                if(task instanceof FetchAnimeInfoTask) {

                    var result = ((FetchAnimeInfoTask)task).getResult();
                    if(result != null) anime_info_set.add(result);

                } else if(task instanceof FetchEpisodeInfoTask) {

                    var result = ((FetchEpisodeInfoTask)task).getResult();
                    if(result != null) episode_info_set.addAll(result);

                } else if(task instanceof FetchTorrentPageTask) {

                    var result_set = ((FetchTorrentPageTask)task).getResultSet();
                    if(result_set != null) torrent_page_info_set.addAll(result_set);
                }
            }
        }

        // 导入数据库
        try(var db = new SQLiteAccess("db/test.db")) {
            for(var epi_record_info : epi_recode_info_set) {
                db.UpsertEpisodeRecord(epi_record_info);
            }
            for(var rss_info : rss_info_set) {
                db.UpsertRSSInfo(rss_info);
            }
            for(var torrent_page_info : torrent_page_info_set) {
                db.UpsertTorrentPageInfo(torrent_page_info);
            }
            for(var anime_info : anime_info_set) {
                db.UpsertAnimeInfo(anime_info);
            }
            for(var episode_info : episode_info_set) {
                db.UpsertEpisodeInfo(episode_info);
            }
        }

        // 获取不在 torrent 表内的 TorrentPageInfo hash_set
        Set<String>            torrent_hash_set = null;
        Set<TorrentDownloader> dt_set           = null; // 获取下载链接
        {
            Set<String> hash = new java.util.HashSet<>();
            for(var torrent_page_info : torrent_page_info_set)
                hash.add(torrent_page_info.TOR_HASH);
            try(var db = new SQLiteAccess("db/test.db")) {
                torrent_hash_set = db.GetTorrentHashNotExist(hash);
                dt_set           = db.GetDownloaderByHash(torrent_hash_set);
            }
        }

        // 多线程下载种子
        // 全部下载完成前阻塞
        // 等待下载完成后再继续执行后续代码
        System.out.println("开始下载种子文件...");
        Set<FetchTorrentInfoTask> fetch_torrent_task_set = new java.util.HashSet<>();
        Set<TorrentInfo>          torrent_file_set       = new java.util.HashSet<>();
        {
            // 初始化任务集合
            for(var dt : dt_set) fetch_torrent_task_set.add(new FetchTorrentInfoTask(dt));

            // 并行执行任务
            Task.ParallelExecution(fetch_torrent_task_set);

            // 收集结果
            for(var task : fetch_torrent_task_set) {
                var result = task.getResult();
                if(result != null) {
                    torrent_file_set.add(new TorrentInfo(result));
                }
            }
        }

        // 把下载的种子保存到数据库
        System.out.println("\n正在保存下载的种子文件到数据库...");
        {
            var count = 0;
            try(var db = new SQLiteAccess("db/test.db")) {
                for(var torrent_file : torrent_file_set) {
                    db.UpsertTorrentInfo(torrent_file);
                    System.out.print("\r当前已保存 " + (++count) + " / " + torrent_file_set.size() + " 个种子文件");
                }
            }
        }

        System.out.println("打印所有任务状态");
        {
            System.out.println("FetchTask1: ");
            for(var task : fetch_task_set) System.out.println(task);
            System.out.println();

            System.out.println("FetchTorrentInfoTask: ");
            for(var task : fetch_torrent_task_set) System.out.println(task);
            System.out.println();
        }

        System.out.println("完成");
    }
}
