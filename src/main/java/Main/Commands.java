package Main;

import java.util.List;
import java.util.Set;

import Database.SQLiteAccess;
import Database.Info.BaseInfo;
import Database.Info.RSSInfo;
import Database.Info.EpisodeRecordInfo;
import Utils.ColorCode;

import static Utils.UtilityFunctions.color;


final class Commands {

    // private final ExcelResult excelResult;

    // private final Map<String, DatabaseBatch> dbItemMap = new HashMap<>();
    // private final Map<String, Set<Task>>     taskMap   = new HashMap<>();

    private Commands() {}

    static void printBlock(MainApplication mainApp, List<String> cmd) {

        if(cmd.size() < 2) {
            System.out.println("Invalid command format for PRINT_BLOCK. Expected: PRINT_BLOCK <block_name1> [<block_name2> ...]");
            return;
        }

        var block_names = cmd.subList(1, cmd.size());
        for(var blockName : block_names) {
            var blockData = mainApp.excelResult.getBlockDataByName(blockName);
            if(blockData != null) {
                System.out.println(color("Block: " + blockName, ColorCode.BOLD_BLUE));
                System.out.println(blockData.toPrintString());
            } else {
                System.out.println(color("Block not found: " + blockName, ColorCode.BOLD_RED));
            }
        }

    }

    static void makeEpisodeRecordItem(MainApplication mainApp, List<String> cmd) {

        if(cmd.size() < 3) {
            System.out.println("Invalid command format for MAKE_EPISODE_RECORD_ITEM. Expected: MAKE_EPISODE_RECORD_ITEM <item_name> <block_name1> [<block_name2> ...]");
            return;
        }

        var item_name      = cmd.get(1);
        var block_names    = cmd.subList(2, cmd.size());
        var requiredBlocks = mainApp.getBlockDataByNames(block_names, mainApp.excelResult.tableDataList());

        // 解析表格数据并创建 EpisodeRecordInfo 对象
        Set<EpisodeRecordInfo> episodeRecords = new java.util.HashSet<>();
        for(var blockData : requiredBlocks)
            episodeRecords.addAll(EpisodeRecordInfo.ParseEpisodeRecordInfoByTableData(blockData));
        mainApp.variables.put(item_name, episodeRecords);
    }

    static void makeRSSItem(MainApplication mainApp, List<String> cmd) {

        if(cmd.size() < 3) {
            System.out.println("Invalid command format for MAKE_RSS_ITEM. Expected: MAKE_RSS_ITEM <item_name> <block_name1> [<block_name2> ...]");
            return;
        }

        var item_name      = cmd.get(1);
        var block_names    = cmd.subList(2, cmd.size());
        var requiredBlocks = mainApp.getBlockDataByNames(block_names, mainApp.excelResult.tableDataList());

        // 解析表格数据并创建 RSSInfo 对象
        Set<RSSInfo> rssItems = new java.util.HashSet<>();
        for(var blockData : requiredBlocks)
            rssItems.addAll(RSSInfo.ParseRSSInfoByTableData(blockData));
        mainApp.variables.put(item_name, rssItems);
    }

    static void toDB(MainApplication mainApp, List<String> cmd) {
        if(cmd.size() < 2) {
            System.out.println("Invalid command format for TO_DB. Expected: TO_DB <item_name1> [<item_name2> ...]");
            return;
        }

        var item_names = cmd.subList(1, cmd.size());

        Set<BaseInfo> to_db_info_set = new java.util.HashSet<>();
        for(var item_name : item_names) {
            var item = mainApp.variables.get(item_name);
            if(item instanceof Set<?> itemSet) for(var obj : itemSet) {
                if(obj instanceof BaseInfo info) to_db_info_set.add(info);
            }
        }

        try(var db = new SQLiteAccess(mainApp.DATABASE_PATH)) {
            db.UpsertInfo(to_db_info_set);
            System.out.println("Database synchronization completed for items: " + item_names);
        } catch(Exception e) {
            System.err.println("Database operation error: " + e.getMessage());
        }
    }

    // void run(List<String> cmd) {
    //     switch(cmd.get(0)) {
    //     case "_item_ani", "_item_anime"                 -> itemAnime(cmd);
    //     case "_item_epi", "_item_episode"               -> itemEpisode(cmd);
    //     case "_item_episode_record"                     -> itemEpisodeRecord(cmd);
    //     case "_item_rss"                                -> itemRSS(cmd);
    //     case "_item_torrent_page"                       -> itemTorrentPage(cmd);
    //     case "_fetch_task_ani", "_fetch_anime"          -> fetchAnime(cmd);
    //     case "_fetch_task_epi", "_fetch_episode"        -> fetchEpisode(cmd);
    //     case "_fetch_task_tor", "_fetch_torrent_page"   -> fetchTorrentPage(cmd);
    //     case "_run_fetch_task"                          -> runTaskSet(cmd);
    //     case "_download_torrent"                        -> downloadTorrent(cmd);
    //     case "_to_db"                                   -> toDatabase(cmd);
    //     case "_item_ani_store", "_item_epi_store", "_item_tor_store" -> unsupportedLegacyCommand(cmd);
    //     default                                         -> unknownCommand(cmd);
    //     }
    // }

    // static void itemAnime(ExcelResult excelResult, List<String> cmd) {
    //     var batch = buildBatchFromBlocks(excelResult, cmd.subList(2, cmd.size()), Commands::toAnimeInfo);
    //     putBatch(cmd.get(1), batch);
    // }

    // private void itemEpisode(List<String> cmd) {
    //     var batch = buildBatchFromBlocks(cmd.subList(2, cmd.size()), Commands::toEpisodeInfo);
    //     putBatch(cmd.get(1), batch);
    // }

    // private void itemEpisodeRecord(List<String> cmd) {
    //     var batch = new DatabaseBatch();
    //     for(var blockData : excelResult.getBlockDataByNames(cmd.subList(2, cmd.size()))) {
    //         batch.episodeRecords.addAll(EpisodeRecordInfo.ParseEpisodeRecordInfoByTableData(blockData));
    //     }
    //     putBatch(cmd.get(1), batch);
    // }

    // private void itemRSS(List<String> cmd) {
    //     var batch = new DatabaseBatch();
    //     for(var blockData : excelResult.getBlockDataByNames(cmd.subList(2, cmd.size()))) {
    //         batch.rssItems.addAll(RSSInfo.ParseRSSInfoByTableData(blockData));
    //     }
    //     putBatch(cmd.get(1), batch);
    // }

    // private void itemTorrentPage(List<String> cmd) {
    //     var batch = buildBatchFromBlocks(cmd.subList(2, cmd.size()), Commands::toTorrentPageInfo);
    //     putBatch(cmd.get(1), batch);
    // }

    // static DatabaseBatch buildBatchFromBlocks(ExcelResult excelResult, List<String> blockNames, RowMapper mapper) {
    //     var batch = new DatabaseBatch();
    //     for(var blockData : excelResult.getBlockDataByNames(blockNames)) {
    //         for(var rowIndex = 0; rowIndex < blockData.GetRowSize(); rowIndex++) {
    //             mapper.map(TableDataRows.rowToMap(blockData, rowIndex), batch);
    //         }
    //     }
    //     return batch;
    // }

    // private static void toAnimeInfo(Map<String, String> row, DatabaseBatch batch) {
    //     try {
    //         batch.animeItems.add(new AnimeInfo(row));
    //     } catch(IllegalArgumentException _) {
    //         // Ignore rows that do not contain a valid anime record.
    //     }
    // }

    // private static void toEpisodeInfo(Map<String, String> row, DatabaseBatch batch) {
    //     try {
    //         batch.episodeItems.add(new EpisodeInfo(row));
    //     } catch(IllegalArgumentException _) {
    //         // Ignore rows that do not contain a valid episode record.
    //     }
    // }

    // private static void toTorrentPageInfo(Map<String, String> row, DatabaseBatch batch) {
    //     try {
    //         batch.torrentPageItems.add(new TorrentPageInfo(row));
    //     } catch(IllegalArgumentException _) {
    //         // Ignore rows that do not contain a valid torrent page record.
    //     }
    // }

    // private void fetchAnime(List<String> cmd) {
    //     var tasks = new LinkedHashSet<Task>();
    //     for(var blockData : excelResult.getBlockDataByNames(cmd.subList(2, cmd.size()))) {
    //         for(var aniId : TableDataRows.getAnimeIds(blockData)) tasks.add(new FetchAnimeInfoTask(aniId));
    //     }
    //     putTasks(cmd.get(1), tasks);
    // }

    // private void fetchEpisode(List<String> cmd) {
    //     var tasks = new LinkedHashSet<Task>();
    //     for(var blockData : excelResult.getBlockDataByNames(cmd.subList(2, cmd.size()))) {
    //         for(var aniId : TableDataRows.getAnimeIds(blockData)) tasks.add(new FetchEpisodeInfoTask(aniId));
    //     }
    //     putTasks(cmd.get(1), tasks);
    // }

    // private void fetchTorrentPage(List<String> cmd) {
    //     var tasks = new LinkedHashSet<Task>();
    //     for(var blockData : excelResult.getBlockDataByNames(cmd.subList(2, cmd.size()))) {
    //         for(var rssUrl : TableDataRows.getRSSUrls(blockData)) tasks.add(new FetchTorrentPageTask(rssUrl));
    //     }
    //     putTasks(cmd.get(1), tasks);
    // }

    // private void runTaskSet(List<String> cmd) {
    //     var varName = cmd.get(1);
    //     var batch   = new DatabaseBatch();

    //     for(var i = 2; i < cmd.size(); i++) {
    //         var taskName = cmd.get(i);
    //         var tasks    = taskMap.get(taskName);
    //         if(tasks == null || tasks.isEmpty()) continue;

    //         System.out.println("Running Task Set: " + taskName);
    //         Task.ParallelExecution(tasks);
    //         collectTaskResults(tasks, batch);
    //         UtilityFunctions.WriteItemListToFile(new ArrayList<>(tasks), ARGS.LOG_PATH + taskName + "_task_log.txt");
    //     }

    //     putBatch(varName, batch);
    //     UtilityFunctions.WriteItemListToFile(batch.allItems(), ARGS.LOG_PATH + varName + "_fetch_result.txt");
    // }

    // private void downloadTorrent(List<String> cmd) {
    //     var varName = cmd.get(1);
    //     var hashes  = new LinkedHashSet<String>();

    //     for(var i = 2; i < cmd.size(); i++) {
    //         var batch = dbItemMap.get(cmd.get(i));
    //         if(batch == null) continue;
    //         for(var torrentPageInfo : batch.torrentPageItems) {
    //             if(torrentPageInfo.TOR_HASH != null && !torrentPageInfo.TOR_HASH.isBlank()) hashes.add(torrentPageInfo.TOR_HASH);
    //         }
    //     }

    //     if(hashes.isEmpty()) return;

    //     try(var db = new SQLiteAccess(ARGS.DATABASE_PATH)) {
    //         var notExistHashes = db.GetTorrentHashNotExist(hashes);
    //         var downloaders    = db.GetDownloaderByHash(notExistHashes);
    //         var tasks          = new LinkedHashSet<Task>();
    //         for(TorrentDownloader downloader : downloaders) {
    //             if(downloader.getUrlList() != null && !downloader.getUrlList().isEmpty()) {
    //                 tasks.add(new FetchTorrentInfoTask(downloader));
    //             }
    //         }

    //         Task.ParallelExecution(tasks);

    //         var batch = new DatabaseBatch();
    //         collectTaskResults(tasks, batch);
    //         putBatch(varName, batch);
    //         UtilityFunctions.WriteItemListToFile(batch.torrentItems, ARGS.LOG_PATH + varName + "_torrent_result.txt");
    //     } catch(SQLException e) {
    //         System.err.println("数据库操作失败: " + e.getMessage());
    //     }
    // }

    // private void toDatabase(List<String> cmd) {
    //     var batch = new DatabaseBatch();
    //     for(var i = 1; i < cmd.size(); i++) {
    //         var items = dbItemMap.get(cmd.get(i));
    //         if(items != null) batch.addAll(items);
    //     }

    //     try(var db = new SQLiteAccess(ARGS.DATABASE_PATH)) {
    //         var infoSet = new java.util.HashSet<Info>(batch.allItems());
    //         db.UpsertInfo(infoSet);
    //     } catch(SQLException e) {
    //         System.err.println("Database operation error: " + e.getMessage());
    //     }

    //     UtilityFunctions.WriteItemListToFile(batch.allItems(), ARGS.LOG_PATH + "db_upsert.txt");
    //     System.out.println("数据库同步完成");
    // }

    // private static void collectTaskResults(Set<Task> tasks, DatabaseBatch batch) {
    //     for(var task : tasks) {
    //         if(task instanceof FetchAnimeInfoTask fetchAnimeTask) {
    //             var result = fetchAnimeTask.getResult();
    //             if(result != null) batch.animeItems.add(result);
    //         } else if(task instanceof FetchEpisodeInfoTask fetchEpisodeTask) {
    //             var result = fetchEpisodeTask.getResult();
    //             if(result != null) batch.episodeItems.addAll(result);
    //         } else if(task instanceof FetchTorrentPageTask fetchTorrentPageTask) {
    //             var result = fetchTorrentPageTask.getResultSet();
    //             if(result != null) batch.torrentPageItems.addAll(result);
    //         } else if(task instanceof FetchTorrentInfoTask fetchTorrentInfoTask) {
    //             var result = fetchTorrentInfoTask.getResult();
    //             if(result != null && result.length > 0) batch.torrentItems.add(new TorrentInfo(result));
    //         }
    //     }
    // }

    // private void putBatch(String varName, DatabaseBatch batch) {
    //     if(batch != null && !batch.isEmpty()) dbItemMap.put(varName, batch);
    // }

    // private void putTasks(String varName, Set<Task> tasks) {
    //     if(tasks != null && !tasks.isEmpty()) taskMap.put(varName, tasks);
    // }

    // private static void unsupportedLegacyCommand(List<String> cmd) {
    //     System.err.println("Unsupported legacy command after schema migration: " + cmd.get(0));
    // }

    // private static void unknownCommand(List<String> cmd) {
    //     System.out.println("Unknown Command: " + cmd.get(0));
    // }

    // @FunctionalInterface
    // private interface RowMapper {
    //     void map(Map<String, String> row, DatabaseBatch batch);
    // }
}
