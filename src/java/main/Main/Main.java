package Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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
import Excel.ExcelReader;
import Excel.ExcelResult;
import MetaData.ARGS;
import Task.FetchAnimeInfoTask;
import Task.FetchEpisodeInfoTask;
import Task.FetchTorrentInfoTask;
import Task.FetchTorrentPageTask;
import Task.Task;
import Util.TableData;
import Util.Util;


public class Main {

    private static final Map<String, DatabaseBatch> dbItemMap = new HashMap<>();
    private static final Map<String, Set<Task>>     taskMap   = new HashMap<>();

    private static ExcelResult excelResult;

    public static void main(String[] args) throws IOException {
        parseArgs(args);

        String nowTimeStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        ARGS.LOG_PATH += nowTimeStr + "/";

        System.out.println("Excel File Path: " + ARGS.EXCEL_FILE_PATH);
        System.out.println("Database Path: " + ARGS.DATABASE_PATH);
        System.out.println("Log Path: " + ARGS.LOG_PATH);

        Files.createDirectories(Path.of(ARGS.LOG_PATH));

        excelResult = ReadExcel(ARGS.EXCEL_FILE_PATH);
        Util.WriteStringToFile(excelResult.toString(), ARGS.LOG_PATH + "01.excel_result.txt");

        for(var cmd : excelResult.commands()) {
            if(cmd == null || cmd.isEmpty()) continue;
            runCommand(cmd);
        }

        System.out.println("完成");
    }

    private static void parseArgs(String[] args) {
        for(int i = 0; i < args.length; i++) {
            switch(args[i]) {
            case "--excel_file_path", "-ex" -> {
                if(i + 1 < args.length) ARGS.EXCEL_FILE_PATH = args[++i];
            }
            case "--database_path", "-db" -> {
                if(i + 1 < args.length) ARGS.DATABASE_PATH = args[++i];
            }
            case "--log_path" -> {
                if(i + 1 < args.length) ARGS.LOG_PATH = args[++i];
            }
            default -> System.out.println("Unknown argument: " + args[i]);
            }
        }
    }

    private static void runCommand(List<String> cmd) {
        switch(cmd.get(0)) {
        case "_item_ani", "_item_anime"                 -> itemAnime(cmd);
        case "_item_epi", "_item_episode"               -> itemEpisode(cmd);
        case "_item_episode_record"                     -> itemEpisodeRecord(cmd);
        case "_item_rss"                                -> itemRSS(cmd);
        case "_item_torrent_page"                       -> itemTorrentPage(cmd);
        case "_fetch_task_ani", "_fetch_anime"          -> fetchAnime(cmd);
        case "_fetch_task_epi", "_fetch_episode"        -> fetchEpisode(cmd);
        case "_fetch_task_tor", "_fetch_torrent_page"   -> fetchTorrentPage(cmd);
        case "_run_fetch_task"                          -> runTaskSet(cmd);
        case "_download_torrent"                        -> downloadTorrent(cmd);
        case "_to_db"                                   -> toDatabase(cmd);
        case "_item_ani_store", "_item_epi_store", "_item_tor_store" -> unsupportedLegacyCommand(cmd);
        default                                         -> unknownCommand(cmd);
        }
    }

    private static void itemAnime(List<String> cmd) {
        var batch = buildBatchFromBlocks(cmd.subList(2, cmd.size()), Main::toAnimeInfo);
        putBatch(cmd.get(1), batch);
    }

    private static void itemEpisode(List<String> cmd) {
        var batch = buildBatchFromBlocks(cmd.subList(2, cmd.size()), Main::toEpisodeInfo);
        putBatch(cmd.get(1), batch);
    }

    private static void itemEpisodeRecord(List<String> cmd) {
        var batch = new DatabaseBatch();
        for(var blockData : excelResult.getBlockDataByNames(cmd.subList(2, cmd.size()))) {
            batch.episodeRecords.addAll(EpisodeRecordInfo.ParseEpisodeRecordInfoByTableData(blockData));
        }
        putBatch(cmd.get(1), batch);
    }

    private static void itemRSS(List<String> cmd) {
        var batch = new DatabaseBatch();
        for(var blockData : excelResult.getBlockDataByNames(cmd.subList(2, cmd.size()))) {
            batch.rssItems.addAll(RSSInfo.ParseRSSInfoByTableData(blockData));
        }
        putBatch(cmd.get(1), batch);
    }

    private static void itemTorrentPage(List<String> cmd) {
        var batch = buildBatchFromBlocks(cmd.subList(2, cmd.size()), Main::toTorrentPageInfo);
        putBatch(cmd.get(1), batch);
    }

    private static DatabaseBatch buildBatchFromBlocks(List<String> blockNames, RowMapper mapper) {
        var batch = new DatabaseBatch();
        for(var blockData : excelResult.getBlockDataByNames(blockNames)) {
            for(var row : blockData.GetData()) {
                mapper.map(rowToMap(blockData, row), batch);
            }
        }
        return batch;
    }

    private static void toAnimeInfo(Map<String, String> row, DatabaseBatch batch) {
        try {
            batch.animeItems.add(new AnimeInfo(row));
        } catch(IllegalArgumentException _) {
            // Ignore rows that do not contain a valid anime record.
        }
    }

    private static void toEpisodeInfo(Map<String, String> row, DatabaseBatch batch) {
        try {
            batch.episodeItems.add(new EpisodeInfo(row));
        } catch(IllegalArgumentException _) {
            // Ignore rows that do not contain a valid episode record.
        }
    }

    private static void toTorrentPageInfo(Map<String, String> row, DatabaseBatch batch) {
        try {
            batch.torrentPageItems.add(new TorrentPageInfo(row));
        } catch(IllegalArgumentException _) {
            // Ignore rows that do not contain a valid torrent page record.
        }
    }

    private static void fetchAnime(List<String> cmd) {
        var tasks = new LinkedHashSet<Task>();
        for(var blockData : excelResult.getBlockDataByNames(cmd.subList(2, cmd.size()))) {
            for(var aniId : getAnimeIds(blockData)) tasks.add(new FetchAnimeInfoTask(aniId));
        }
        putTasks(cmd.get(1), tasks);
    }

    private static void fetchEpisode(List<String> cmd) {
        var tasks = new LinkedHashSet<Task>();
        for(var blockData : excelResult.getBlockDataByNames(cmd.subList(2, cmd.size()))) {
            for(var aniId : getAnimeIds(blockData)) tasks.add(new FetchEpisodeInfoTask(aniId));
        }
        putTasks(cmd.get(1), tasks);
    }

    private static void fetchTorrentPage(List<String> cmd) {
        var tasks = new LinkedHashSet<Task>();
        for(var blockData : excelResult.getBlockDataByNames(cmd.subList(2, cmd.size()))) {
            for(var rssUrl : getRSSUrls(blockData)) tasks.add(new FetchTorrentPageTask(rssUrl));
        }
        putTasks(cmd.get(1), tasks);
    }

    private static void runTaskSet(List<String> cmd) {
        var varName = cmd.get(1);
        var batch   = new DatabaseBatch();

        for(var i = 2; i < cmd.size(); i++) {
            var taskName = cmd.get(i);
            var tasks    = taskMap.get(taskName);
            if(tasks == null || tasks.isEmpty()) continue;

            System.out.println("Running Task Set: " + taskName);
            Task.ParallelExecution(tasks);
            collectTaskResults(tasks, batch);
            Util.WriteItemListToFile(new ArrayList<>(tasks), ARGS.LOG_PATH + taskName + "_task_log.txt");
        }

        putBatch(varName, batch);
        Util.WriteItemListToFile(batch.allItems(), ARGS.LOG_PATH + varName + "_fetch_result.txt");
    }

    private static void collectTaskResults(Set<Task> tasks, DatabaseBatch batch) {
        for(var task : tasks) {
            if(task instanceof FetchAnimeInfoTask fetchAnimeTask) {
                var result = fetchAnimeTask.getResult();
                if(result != null) batch.animeItems.add(result);
            } else if(task instanceof FetchEpisodeInfoTask fetchEpisodeTask) {
                var result = fetchEpisodeTask.getResult();
                if(result != null) batch.episodeItems.addAll(result);
            } else if(task instanceof FetchTorrentPageTask fetchTorrentPageTask) {
                var result = fetchTorrentPageTask.getResultSet();
                if(result != null) batch.torrentPageItems.addAll(result);
            } else if(task instanceof FetchTorrentInfoTask fetchTorrentInfoTask) {
                var result = fetchTorrentInfoTask.getResult();
                if(result != null && result.length > 0) batch.torrentItems.add(new TorrentInfo(result));
            }
        }
    }

    private static void downloadTorrent(List<String> cmd) {
        var varName = cmd.get(1);
        var hashes  = new LinkedHashSet<String>();

        for(var i = 2; i < cmd.size(); i++) {
            var batch = dbItemMap.get(cmd.get(i));
            if(batch == null) continue;
            for(var torrentPageInfo : batch.torrentPageItems) {
                if(torrentPageInfo.TOR_HASH != null && !torrentPageInfo.TOR_HASH.isBlank()) hashes.add(torrentPageInfo.TOR_HASH);
            }
        }

        if(hashes.isEmpty()) return;

        try(var db = new SQLiteAccess(ARGS.DATABASE_PATH)) {
            var notExistHashes = db.GetTorrentHashNotExist(hashes);
            var downloaders    = db.GetDownloaderByHash(notExistHashes);
            var tasks          = new LinkedHashSet<Task>();
            for(TorrentDownloader downloader : downloaders) {
                if(downloader.getUrlList() != null && !downloader.getUrlList().isEmpty()) {
                    tasks.add(new FetchTorrentInfoTask(downloader));
                }
            }

            Task.ParallelExecution(tasks);

            var batch = new DatabaseBatch();
            collectTaskResults(tasks, batch);
            putBatch(varName, batch);
            Util.WriteItemListToFile(batch.torrentItems, ARGS.LOG_PATH + varName + "_torrent_result.txt");
        } catch(SQLException e) {
            System.err.println("数据库操作失败: " + e.getMessage());
        }
    }

    private static void toDatabase(List<String> cmd) {
        var batch = new DatabaseBatch();
        for(var i = 1; i < cmd.size(); i++) {
            var items = dbItemMap.get(cmd.get(i));
            if(items != null) batch.addAll(items);
        }

        try(var db = new SQLiteAccess(ARGS.DATABASE_PATH)) {
            db.UpsertAnimeInfo(batch.animeItems);
            db.UpsertEpisodeInfo(batch.episodeItems);
            db.UpsertEpisodeRecordInfo(batch.episodeRecords);
            db.UpsertRSSInfo(batch.rssItems);
            db.UpsertTorrentPageInfo(batch.torrentPageItems);
            db.UpsertTorrentInfo(batch.torrentItems);
        } catch(SQLException e) {
            System.err.println("Database operation error: " + e.getMessage());
        }

        Util.WriteItemListToFile(batch.allItems(), ARGS.LOG_PATH + "db_upsert.txt");
        System.out.println("数据库同步完成");
    }

    private static void putBatch(String varName, DatabaseBatch batch) {
        if(batch != null && !batch.isEmpty()) dbItemMap.put(varName, batch);
    }

    private static void putTasks(String varName, Set<Task> tasks) {
        if(tasks != null && !tasks.isEmpty()) taskMap.put(varName, tasks);
    }

    private static void unsupportedLegacyCommand(List<String> cmd) {
        System.err.println("Unsupported legacy command after schema migration: " + cmd.get(0));
    }

    private static void unknownCommand(List<String> cmd) {
        System.out.println("Unknown Command: " + cmd.get(0));
    }

    private static Set<Integer> getAnimeIds(TableData tableData) {
        var result = new LinkedHashSet<Integer>();
        var index  = tableData.GetHeaderIndex("ANI_ID");
        if(index == -1) return result;

        for(var row : tableData.GetData()) {
            var parsed = parseInt(getCell(row, index));
            if(parsed != null) result.add(parsed);
        }
        return result;
    }

    private static Set<String> getRSSUrls(TableData tableData) {
        var result = new LinkedHashSet<String>();
        var index  = tableData.GetHeaderIndex("URL_RSS");
        if(index == -1) index = tableData.GetHeaderIndex("url_rss");
        if(index == -1) return result;

        for(var row : tableData.GetData()) {
            var value = getCell(row, index);
            if(value == null || value.isBlank()) continue;
            for(var item : value.split(";")) {
                var rssUrl = item.trim();
                if(!rssUrl.isEmpty()) result.add(rssUrl);
            }
        }
        return result;
    }

    private static Map<String, String> rowToMap(TableData tableData, String[] row) {
        var headers = tableData.GetHeaders();
        var result  = new HashMap<String, String>();
        for(var i = 0; i < headers.length; i++) {
            result.put(headers[i], getCell(row, i));
        }
        return result;
    }

    private static String getCell(String[] row, int index) {
        if(row == null || index < 0 || index >= row.length) return null;
        return row[index];
    }

    private static Integer parseInt(String value) {
        if(value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value);
        } catch(NumberFormatException _) {
            return null;
        }
    }

    public static ExcelResult ReadExcel(String excelFilePath) throws IOException {
        System.out.println("Reading excel file...");
        var result = new ExcelReader().Read(excelFilePath);

        System.out.println("Saving commands to file...");
        try(var writer = Files.newBufferedWriter(Path.of(ARGS.LOG_PATH + "00.commands.txt"))) {
            writer.write(result.getCommandsInfo());
        }

        System.out.println("Saving block data...");
        try(var writer = Files.newBufferedWriter(Path.of(ARGS.LOG_PATH + "00.blocks.txt"))) {
            writer.write(result.getBlocksInfo());
        }

        return result;
    }

    @FunctionalInterface
    private interface RowMapper {
        void map(Map<String, String> row, DatabaseBatch batch);
    }

    private static class DatabaseBatch {
        final List<AnimeInfo>         animeItems       = new ArrayList<>();
        final List<EpisodeInfo>       episodeItems     = new ArrayList<>();
        final List<EpisodeRecordInfo> episodeRecords   = new ArrayList<>();
        final List<RSSInfo>           rssItems         = new ArrayList<>();
        final List<TorrentPageInfo>   torrentPageItems = new ArrayList<>();
        final List<TorrentInfo>       torrentItems     = new ArrayList<>();

        void addAll(DatabaseBatch other) {
            animeItems.addAll(other.animeItems);
            episodeItems.addAll(other.episodeItems);
            episodeRecords.addAll(other.episodeRecords);
            rssItems.addAll(other.rssItems);
            torrentPageItems.addAll(other.torrentPageItems);
            torrentItems.addAll(other.torrentItems);
        }

        boolean isEmpty() {
            return animeItems.isEmpty()
                && episodeItems.isEmpty()
                && episodeRecords.isEmpty()
                && rssItems.isEmpty()
                && torrentPageItems.isEmpty()
                && torrentItems.isEmpty();
        }

        List<Object> allItems() {
            var result = new ArrayList<Object>();
            result.addAll(animeItems);
            result.addAll(episodeItems);
            result.addAll(episodeRecords);
            result.addAll(rssItems);
            result.addAll(torrentPageItems);
            result.addAll(torrentItems);
            return result;
        }
    }
}
