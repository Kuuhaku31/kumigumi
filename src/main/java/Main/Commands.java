package Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import Database.Info.BaseInfo;
import Database.Info.EpisodeRecordInfo;
import Database.Info.RSSInfo;
import Database.Info.TorrentPageInfo;
import Database.SQLiteAccess;
import Database.TorrentDownloader;
import Main.FetchTask.FetchAnimeInfoTask;
import Main.FetchTask.FetchEpisodeInfoTask;
import Main.FetchTask.FetchInfoTask;
import Main.FetchTask.FetchTorrentInfoTask;
import Main.FetchTask.FetchTorrentPageTask;
import Utils.ColorCode;
import Utils.Task;

import static Utils.UtilityFunctions.color;


final class Commands {

    private Commands() {}


    static void printMessage(List<String> cmd) {

        // 参数检查
        if(cmd.size() < 2) {
            var msg = "Invalid command format for PRINT_MESSAGE. Expected: PRINT_MESSAGE <message>";
            System.out.println(color(msg, ColorCode.BOLD_RED));
            return;
        }

        // 输出内容
        System.out.println(String.join(" ", cmd.subList(1, cmd.size())));
    }

    static void printVariable(MainApplication mainApp, List<String> cmd) {

        // 参数检查
        if(cmd.size() < 2) {
            var msg = "Invalid command format for PRINT_VARIABLE. Expected: PRINT_VARIABLE <variable_name1> [<variable_name2> ...]";
            System.out.println(color(msg, ColorCode.BOLD_RED));
            return;
        }

        // 构建输出内容
        var requested_variable_names = cmd.subList(1, cmd.size());
        var str                      = mainApp.getVariableAsString(requested_variable_names, true);

        // 输出内容
        System.out.println(str);
    }

    static void saveLog(MainApplication mainApp, List<String> cmd) {

        // 参数检查
        if(cmd.size() < 3) {
            var msg = "Invalid command format for SAVE_LOG. Expected: SAVE_LOG <log_file_name> <var_name1> [<var_name2> ...]";
            System.out.println(color(msg, ColorCode.BOLD_RED));
            return;
        }

        // 路径解析
        Path logPath;
        {
            // 获取日志根目录绝对路径
            // 解析日志文件路径并规范化
            var logRoot = Path.of(mainApp.LOG_PATH).toAbsolutePath().normalize();
            logPath     = logRoot.resolve(cmd.get(1)).normalize();

            // 安全检查：确保日志文件路径在日志根目录下，防止路径遍历攻击
            if(!logPath.startsWith(logRoot)) {
                System.out.println(color("Unsafe log path rejected: " + cmd.get(1), ColorCode.BOLD_RED));
                logPath = null;
            }
        }
        if(logPath == null) {
            System.out.println(color("Invalid log file name: " + cmd.get(1), ColorCode.BOLD_RED));
            return;
        }

        // 构建日志内容
        var requested_variable_names = cmd.subList(2, cmd.size());
        var str                      = mainApp.getVariableAsString(requested_variable_names, false);

        // 写入日志文件
        try {
            var parent = logPath.getParent();
            if(parent != null) Files.createDirectories(parent);
            Files.writeString(logPath, str);
            System.out.println(color("Log written to: " + logPath, ColorCode.BOLD_GREEN));
        } catch(IOException e) {
            System.err.println(color("Failed to write log: " + e.getMessage(), ColorCode.BOLD_RED));
        }
    }

    static void makeEpisodeRecordItem(MainApplication mainApp, List<String> cmd) {

        // 参数检查
        if(cmd.size() < 3) {
            var msg = "Invalid command format for MAKE_INFO_EPISODE_RECORD. Expected: MAKE_INFO_EPISODE_RECORD <item_name> <variable_name1> [<variable_name2> ...]";
            System.out.println(color(msg, ColorCode.BOLD_RED));
            return;
        }

        // 创建 InfoSetItem 并合并数据
        var item = new InfoSetItem();
        for(var dataBlock : mainApp.getDataBlockByNames(cmd.subList(2, cmd.size()))) {
            item.data.addAll(EpisodeRecordInfo.ParseEpisodeRecordInfoByDataBlock(dataBlock));
        }

        // 合并到变量
        mainApp.putOrMergeItem(cmd.get(1), item);
    }

    static void makeRSSItem(MainApplication mainApp, List<String> cmd) {

        // 参数检查
        if(cmd.size() < 3) {
            var msg = "Invalid command format for MAKE_INFO_RSS. Expected: MAKE_INFO_RSS <item_name> <variable_name1> [<variable_name2> ...]";
            System.out.println(color(msg, ColorCode.BOLD_RED));
            return;
        }

        // 创建 InfoSetItem 并合并数据
        var item = new InfoSetItem();
        for(var dataBlock : mainApp.getDataBlockByNames(cmd.subList(2, cmd.size()))) {
            item.data.addAll(RSSInfo.ParseRSSInfoByDataBlock(dataBlock));
        }

        // 合并到变量
        mainApp.putOrMergeItem(cmd.get(1), item);
    }

    static void makeFetchTaskAnime(MainApplication mainApp, List<String> cmd) {

        // 参数检查
        if(cmd.size() < 3) {
            var msg = "Invalid command format for MAKE_TASK_FETCH_ANIME. Expected: MAKE_TASK_FETCH_ANIME <item_name> <variable_name1> [<variable_name2> ...]";
            System.out.println(color(msg, ColorCode.BOLD_RED));
            return;
        }

        // 创建任务集合
        var item = new TaskSetItem();
        for(var dataBlock : mainApp.getDataBlockByNames(cmd.subList(2, cmd.size()))) {
            item.data.addAll(FetchAnimeInfoTask.ParseFetchAnimeInfoTaskByDataBlock(dataBlock));
        }

        // 合并到变量
        mainApp.putOrMergeItem(cmd.get(1), item);
    }

    static void makeFetchTaskEpisode(MainApplication mainApp, List<String> cmd) {

        // 参数检查
        if(cmd.size() < 3) {
            var msg = "Invalid command format for MAKE_TASK_FETCH_EPISODE. Expected: MAKE_TASK_FETCH_EPISODE <item_name> <variable_name1> [<variable_name2> ...]";
            System.out.println(color(msg, ColorCode.BOLD_RED));
            return;
        }

        // 创建任务集合
        var item = new TaskSetItem();
        for(var dataBlock : mainApp.getDataBlockByNames(cmd.subList(2, cmd.size()))) {
            item.data.addAll(FetchEpisodeInfoTask.ParseFetchEpisodeInfoTaskByDataBlock(dataBlock));
        }

        // 合并到变量
        mainApp.putOrMergeItem(cmd.get(1), item);
    }

    static void makeFetchTaskTorrentPage(MainApplication mainApp, List<String> cmd) {

        // 参数检查
        if(cmd.size() < 3) {
            var msg = "Invalid command format for MAKE_TASK_FETCH_TORRENT_PAGE. Expected: MAKE_TASK_FETCH_TORRENT_PAGE <item_name> <variable_name1> [<variable_name2> ...]";
            System.out.println(color(msg, ColorCode.BOLD_RED));
            return;
        }

        // 创建任务集合
        var item = new TaskSetItem();
        for(var dataBlock : mainApp.getDataBlockByNames(cmd.subList(2, cmd.size()))) {
            item.data.addAll(FetchTorrentPageTask.ParseFetchTorrentPageTaskByDataBlock(dataBlock));
        }

        // 合并到变量
        mainApp.putOrMergeItem(cmd.get(1), item);
    }

    static void runTask(MainApplication mainApp, List<String> cmd) {

        // 参数检查
        if(cmd.size() < 3) {
            var msg = "Invalid command format for RUN_TASK. Expected: RUN_TASK <result_item_name> <variable_name1> [<variable_name2> ...]";
            System.out.println(color(msg, ColorCode.BOLD_RED));
            return;
        }
        var result_item_name         = cmd.get(1);
        var requested_variable_names = cmd.subList(2, cmd.size());

        // 构建任务集合
        var tasks = new LinkedHashSet<FetchInfoTask>();
        for(var variable_name : requested_variable_names) {

            // 不存在
            if(!mainApp.variables.containsKey(variable_name)) {
                System.out.println(color("Variable not found: " + variable_name, ColorCode.BOLD_RED));
                continue;
            }

            // 确保类型正确
            var item = mainApp.variables.get(variable_name);
            if(!(item instanceof TaskSetItem i))
                System.out.println(color("Variable " + variable_name + " is not TaskSetItem; skipped.", ColorCode.BOLD_RED));
            else
                tasks.addAll(i.data);
        }

        // 执行任务
        try {
            Task.ParallelExecution(tasks);
        } catch(Exception e) {
            System.out.println(color("Error running tasks: " + e.getMessage(), ColorCode.BOLD_RED));
        }

        // 收集结果
        var result = new InfoSetItem();
        for(var task : tasks) result.data.addAll(task.GetInfoSet());

        // 合并到变量
        mainApp.putOrMergeItem(result_item_name, result);
    }

    static void toDB(MainApplication mainApp, List<String> cmd) {

        // 参数检查
        if(cmd.size() < 2) {
            var msg = "Invalid command format for TO_DB. Expected: TO_DB <variable_name1> [<variable_name2> ...]";
            System.out.println(color(msg, ColorCode.BOLD_RED));
            return;
        }

        // 构建待同步数据集合
        var to_db_info_set = new LinkedHashSet<BaseInfo>();
        for(var variable_name : cmd.subList(1, cmd.size())) {

            // 不存在
            if(!mainApp.variables.containsKey(variable_name)) {
                System.out.println(color("Variable not found: " + variable_name, ColorCode.BOLD_RED));
                continue;
            }

            // 确保类型正确
            if(mainApp.variables.get(variable_name) instanceof InfoSetItem i)
                to_db_info_set.addAll(i.data);
            else {
                System.out.println(color("Variable " + variable_name + " is not InfoSetItem; command skipped.", ColorCode.BOLD_RED));
            }
        }
        if(to_db_info_set.isEmpty()) {
            System.out.println(color("No valid InfoSetItem data found to synchronize to database.", ColorCode.YELLOW));
            return;
        }

        // 同步到数据库
        try(var db = new SQLiteAccess(mainApp.DATABASE_PATH)) {
            db.UpsertInfo(to_db_info_set);
            System.out.println("Database synchronization completed for variables: " + cmd.subList(1, cmd.size()));
        } catch(Exception e) {
            System.err.println("Database operation error: " + e.getMessage());
        }
    }

    static void updateTorrent(MainApplication mainApp, List<String> cmd) {

        // 参数检查
        if(cmd.size() < 2) {
            System.out.println(color("Invalid command format for UPDATE_TORRENT. Expected: UPDATE_TORRENT <block_name1> [<block_name2> ...]", ColorCode.BOLD_RED));
            return;
        }

        // 构建待更新的种子哈希集合
        var requested_tor_hash_set = new HashSet<String>();
        {
            var requested_variable_names = cmd.subList(1, cmd.size());
            var requested_items          = mainApp.getItemsByNames(requested_variable_names);
            for(var item : requested_items) {
                if(!(item instanceof InfoSetItem set_item)) {
                    System.out.println(color("Variable " + cmd.get(1) + " is not InfoSetItem; skipped.", ColorCode.BOLD_RED));
                    continue;
                } else for(var info : set_item.data) {
                    if(info instanceof TorrentPageInfo tor_info) {
                        requested_tor_hash_set.add(tor_info.TOR_HASH);
                    }
                }
            }
        }

        // 获取数据库中不存在的种子哈希集合
        Set<TorrentDownloader> not_exist_torrent_downloader_set = null;
        try(var db = new SQLiteAccess(mainApp.DATABASE_PATH)) {
            var not_exist_hash_set = db.GetTorrentHashNotExist(requested_tor_hash_set);
            not_exist_torrent_downloader_set = db.GetDownloaderByHash(not_exist_hash_set);
        } catch(Exception e) {
            System.err.println("Database operation error: " + e.getMessage());
        }
        if(not_exist_torrent_downloader_set == null || not_exist_torrent_downloader_set.isEmpty()) {
            System.out.println(color("No new torrents to update.", ColorCode.YELLOW));
            return;
        }

        // 构建下载任务
        var tasks = new LinkedHashSet<FetchInfoTask>();
        for(var downloader : not_exist_torrent_downloader_set) {
            var task = new FetchTorrentInfoTask(downloader);
            tasks.add(task);
        }

        // 执行任务
        try {
            Task.ParallelExecution(tasks);
        } catch(Exception e) {
            System.out.println(color("Error running tasks: " + e.getMessage(), ColorCode.BOLD_RED));
        }

        // 收集结果
        var result = new InfoSetItem();
        for(var task : tasks) result.data.addAll(task.GetInfoSet());

        // 更新到数据库
        try(var db = new SQLiteAccess(mainApp.DATABASE_PATH)) {
            db.UpsertInfo(result.data);
            System.out.println("Database update completed for " + result.data.size() + " torrents.");
        } catch(Exception e) {
            System.err.println("Database operation error: " + e.getMessage());
        }
    }
}
