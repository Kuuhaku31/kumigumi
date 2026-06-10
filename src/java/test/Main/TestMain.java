package Main;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import Database.AnimeInfo;
import Database.EpisodeInfo;
import Database.EpisodeRecordInfo;
import Database.RSSInfo;
import Database.SQLiteAccess;
import Database.TorrentDownloader;
import Database.TorrentInfo;
import Database.TorrentPageInfo;
import NetAccess.NetAccess;

public class TestMain {
    public static void main(String[] args) throws IOException, SQLException, URISyntaxException {
        System.out.println("TestExcel...");

        var e_reader = new Excel.ExcelReader();
        var res      = e_reader.Read("./db/test_exc.xlsx");

        System.out.println("从Excel读取的数据:");
        var blockDataList = res.blockDataList();
        for(var entry : blockDataList.entrySet()) {
            System.out.println("Block Name: " + entry.getKey());
            System.out.println("Block Data:\n" + entry.getValue().toString());
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

        // 获取 torrent page 信息
        Set<TorrentPageInfo> torrent_page_info_set = new java.util.HashSet<>();
        {
            var total_count   = rss_info_set.size();
            var current_count = 0;
            for(var rss_info : rss_info_set) {
                var new_torrent_page_info_set = NetAccess.FetchTorrentPageInfoSet(rss_info.URL_RSS);

                // 添加新获取的 Torrent Page 信息到集合中
                if(new_torrent_page_info_set != null && !new_torrent_page_info_set.isEmpty()) {
                    torrent_page_info_set.addAll(new_torrent_page_info_set);
                }

                System.out.print("\r当前已处理 " + (++current_count) + " / " + total_count + " 个 RSS 链接");
            }
            System.out.println("\n总共获取了 " + torrent_page_info_set.size() + " 条 Torrent Page 信息。");
        }

        // 获取 AnimeInfo 和 EpisodeInfo 实例
        Set<AnimeInfo> anime_info_set   = new java.util.HashSet<>();
        Set<EpisodeInfo> episode_info_set = new java.util.HashSet<>();
        {
            var total_count   = rss_info_set.size();
            var current_count = 0;
            for(var rss_info : rss_info_set) {
                var anime_id             = rss_info.ANI_ID;
                var new_anime_info       = NetAccess.FetchAnimeInfo(anime_id);
                var new_episode_info_set = NetAccess.FetchEpisodeInfoSet(anime_id);

                // 添加新获取的 AnimeInfo 和 EpisodeInfo 到集合中
                if(new_anime_info != null) {
                    anime_info_set.add(new_anime_info);
                }
                if(new_episode_info_set != null) {
                    episode_info_set.addAll(new_episode_info_set);
                }

                System.out.print("\r当前已处理 " + (++current_count) + " / " + total_count + " 个 RSS 链接");
            }
        }

        // 测试导入数据库
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
        Set<String> torrent_hash_set = null;
        Set<TorrentDownloader> dt_set = null; // 获取下载链接
        {
            Set<String> hash = new java.util.HashSet<>();
            for(var torrent_page_info : torrent_page_info_set) 
                hash.add(torrent_page_info.TOR_HASH);
            try(var db = new SQLiteAccess("db/test.db")) {
                torrent_hash_set = db.GetTorrentHashNotExist(hash);
                dt_set = db.GetDownloaderByHash(torrent_hash_set);
            }
        }

        // 多线程下载种子
        // 全部下载完成前阻塞
        // 等待下载完成后再继续执行后续代码
        System.out.println("开始下载种子文件...");
        Set<TorrentInfo> torrent_file_set = new java.util.HashSet<>();
        {
            var total_count   = dt_set.size();
            var current_count = new AtomicInteger(0);
            var executor      = Executors.newFixedThreadPool(5); // 创建一个固定线程池
            var futures       = new java.util.ArrayList<java.util.concurrent.Future<TorrentInfo>>();

            for(var dt : dt_set) {
                // 提交下载任务到线程池
                futures.add(executor.submit(() -> {
                    try {
                        var torrent_data = NetAccess.DownloadFile(dt.url_download_list().get(0));
                        return new TorrentInfo(torrent_data);
                    } catch (Exception e) {
                        System.err.println("下载失败: " + dt.TOR_HASH());
                        return null;
                    } finally {
                        System.out.print("\r当前已下载 " + current_count.incrementAndGet() + " / " + total_count + " 个种子文件");
                    }
                }));
            }

            // 等待所有下载任务完成并收集结果
            for(var future : futures) {
                try {
                    var torrent_info = future.get(); // 阻塞直到下载完成
                    if(torrent_info != null) {
                        torrent_file_set.add(torrent_info);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("下载任务执行失败: " + e.getMessage());
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

        System.out.println("完成");
    }
}
