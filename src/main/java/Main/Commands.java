package Main;

import java.util.List;
import java.util.Set;

import Database.SQLiteAccess;
import Database.Info.BaseInfo;
import Database.Info.EpisodeRecordInfo;
import Database.Info.RSSInfo;
import Database.Info.TorrentInfo;
import Task.FetchAnimeInfoTask;
import Task.FetchEpisodeInfoTask;
import Task.FetchTorrentInfoTask;
import Task.FetchTorrentPageTask;
import Task.Task;
import Utils.ColorCode;
import Utils.UtilityFunctions;

import static Utils.UtilityFunctions.color;


final class Commands {

    private Commands() {}

    static void printVariable(MainApplication mainApp, List<String> cmd) {

        if(cmd.size() < 2) {
            System.out.println("Invalid command format for PRINT_VARIABLE. Expected: PRINT_VARIABLE <variable_name1> [<variable_name2> ...]");
            return;
        }

        for(var variableName : cmd.subList(1, cmd.size())) {
            if(!mainApp.variables.containsKey(variableName)) {
                System.out.println(color("Variable not found: " + variableName, ColorCode.BOLD_RED));
                continue;
            }

            System.out.println(color("Variable: " + variableName, ColorCode.BOLD_BLUE));
            var sb = new StringBuilder();
            appendLogValue(sb, mainApp.variables.get(variableName));
            System.out.print(sb);
        }
    }

    static void printMessage(List<String> cmd) {

        if(cmd.size() < 2) {
            System.out.println("Invalid command format for PRINT_MESSAGE. Expected: PRINT_MESSAGE <message>");
            return;
        }

        System.out.println(String.join(" ", cmd.subList(1, cmd.size())));
    }

    static void makeEpisodeRecordItem(MainApplication mainApp, List<String> cmd) {

        if(cmd.size() < 3) {
            System.out.println("Invalid command format for MAKE_INFO_EPISODE_RECORD. Expected: MAKE_INFO_EPISODE_RECORD <item_name> <variable_name1> [<variable_name2> ...]");
            return;
        }

        var itemName = cmd.get(1);
        if(!canMergeBaseInfoSet(mainApp, itemName)) return;

        var episodeRecords = new java.util.LinkedHashSet<BaseInfo>();
        for(var blockData : mainApp.getBlockDataByNames(cmd.subList(2, cmd.size()))) {
            episodeRecords.addAll(EpisodeRecordInfo.ParseEpisodeRecordInfoByTableData(blockData));
        }

        mergeBaseInfoSet(mainApp, itemName, episodeRecords);
    }

    static void makeRSSItem(MainApplication mainApp, List<String> cmd) {

        if(cmd.size() < 3) {
            System.out.println("Invalid command format for MAKE_INFO_RSS. Expected: MAKE_INFO_RSS <item_name> <variable_name1> [<variable_name2> ...]");
            return;
        }

        var itemName = cmd.get(1);
        if(!canMergeBaseInfoSet(mainApp, itemName)) return;

        var rssItems = new java.util.LinkedHashSet<BaseInfo>();
        for(var blockData : mainApp.getBlockDataByNames(cmd.subList(2, cmd.size()))) {
            rssItems.addAll(RSSInfo.ParseRSSInfoByTableData(blockData));
        }

        mergeBaseInfoSet(mainApp, itemName, rssItems);
    }

    static void toDB(MainApplication mainApp, List<String> cmd) {

        if(cmd.size() < 2) {
            System.out.println("Invalid command format for TO_DB. Expected: TO_DB <variable_name1> [<variable_name2> ...]");
            return;
        }

        var toDbInfoSet = new java.util.LinkedHashSet<BaseInfo>();
        for(var variableName : cmd.subList(1, cmd.size())) {
            var infoSet = getBaseInfoSetVariable(mainApp, variableName);
            if(infoSet == null) return;
            toDbInfoSet.addAll(infoSet);
        }

        try(var db = new SQLiteAccess(mainApp.DATABASE_PATH)) {
            db.UpsertInfo(toDbInfoSet);
            System.out.println("Database synchronization completed for variables: " + cmd.subList(1, cmd.size()));
        } catch(Exception e) {
            System.err.println("Database operation error: " + e.getMessage());
        }
    }

    static void makeFetchTaskAnime(MainApplication mainApp, List<String> cmd) {

        if(cmd.size() < 3) {
            System.out.println("Invalid command format for MAKE_TASK_FETCH_ANIME. Expected: MAKE_TASK_FETCH_ANIME <item_name> <variable_name1> [<variable_name2> ...]");
            return;
        }

        var itemName = cmd.get(1);
        if(!canMergeTaskSet(mainApp, itemName)) return;

        var tasks = new java.util.LinkedHashSet<Task>();
        for(var blockData : mainApp.getBlockDataByNames(cmd.subList(2, cmd.size()))) {
            for(var aniId : TableDataRows.getAnimeIds(blockData)) tasks.add(new FetchAnimeInfoTask(aniId));
        }

        mergeTaskSet(mainApp, itemName, tasks);
    }

    static void makeFetchTaskEpisode(MainApplication mainApp, List<String> cmd) {

        if(cmd.size() < 3) {
            System.out.println("Invalid command format for MAKE_TASK_FETCH_EPISODE. Expected: MAKE_TASK_FETCH_EPISODE <item_name> <variable_name1> [<variable_name2> ...]");
            return;
        }

        var itemName = cmd.get(1);
        if(!canMergeTaskSet(mainApp, itemName)) return;

        var tasks = new java.util.LinkedHashSet<Task>();
        for(var blockData : mainApp.getBlockDataByNames(cmd.subList(2, cmd.size()))) {
            for(var aniId : TableDataRows.getAnimeIds(blockData)) tasks.add(new FetchEpisodeInfoTask(aniId));
        }

        mergeTaskSet(mainApp, itemName, tasks);
    }

    static void makeFetchTaskTorrentPage(MainApplication mainApp, List<String> cmd) {

        if(cmd.size() < 3) {
            System.out.println("Invalid command format for MAKE_TASK_FETCH_TORRENT_PAGE. Expected: MAKE_TASK_FETCH_TORRENT_PAGE <item_name> <variable_name1> [<variable_name2> ...]");
            return;
        }

        var itemName = cmd.get(1);
        if(!canMergeTaskSet(mainApp, itemName)) return;

        var tasks = new java.util.LinkedHashSet<Task>();
        for(var blockData : mainApp.getBlockDataByNames(cmd.subList(2, cmd.size()))) {
            for(var rssUrl : TableDataRows.getRSSUrls(blockData)) tasks.add(new FetchTorrentPageTask(rssUrl));
        }

        mergeTaskSet(mainApp, itemName, tasks);
    }

    static void runTask(MainApplication mainApp, List<String> cmd) {

        if(cmd.size() < 3) {
            System.out.println("Invalid command format for RUN_TASK. Expected: RUN_TASK <result_item_name> <variable_name1> [<variable_name2> ...]");
            return;
        }

        var resultItemName = cmd.get(1);
        if(!canMergeBaseInfoSet(mainApp, resultItemName)) return;

        var tasks = new java.util.LinkedHashSet<Task>();
        for(var variableName : cmd.subList(2, cmd.size())) {
            var taskSet = getTaskSetVariable(mainApp, variableName);
            if(taskSet == null) return;
            tasks.addAll(taskSet);
        }

        Task.ParallelExecution(tasks);
        mergeBaseInfoSet(mainApp, resultItemName, collectTaskResults(tasks));
        UtilityFunctions.WriteItemListToFile(new java.util.ArrayList<>(tasks), mainApp.LOG_PATH + resultItemName + "_task_log.txt");
    }

    static void saveLog(MainApplication mainApp, List<String> cmd) {

        if(cmd.size() < 3) {
            System.out.println("Invalid command format for SAVE_LOG. Expected: SAVE_LOG <log_file_name> <var_name1> [<var_name2> ...]");
            return;
        }

        var logRoot = java.nio.file.Path.of(mainApp.LOG_PATH).toAbsolutePath().normalize();
        var logPath = logRoot.resolve(cmd.get(1)).normalize();

        if(!logPath.startsWith(logRoot)) {
            System.out.println(color("Unsafe log path rejected: " + cmd.get(1), ColorCode.BOLD_RED));
            return;
        }

        var sb = new StringBuilder();
        for(var variableName : cmd.subList(2, cmd.size())) {
            sb.append("# ").append(variableName).append("\n");
            if(!mainApp.variables.containsKey(variableName)) {
                sb.append("Variable not found: ").append(variableName).append("\n\n");
                continue;
            }

            appendLogValue(sb, mainApp.variables.get(variableName));
            sb.append("\n");
        }

        try {
            var parent = logPath.getParent();
            if(parent != null) java.nio.file.Files.createDirectories(parent);
            java.nio.file.Files.writeString(logPath, sb.toString());
            System.out.println("Log written to: " + logPath);
        } catch(java.io.IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }

    static void updateTorrent(MainApplication mainApp, List<String> cmd) {

        if(cmd.size() < 2) {
            System.out.println("Invalid command format for UPDATE_TORRENT. Expected: UPDATE_TORRENT <block_name1> [<block_name2> ...]");
            return;
        }

        var requiredBlockNames = cmd.subList(1, cmd.size());
        mainApp.getBlockDataByNames(requiredBlockNames);
    }


    private static Set<BaseInfo> collectTaskResults(Set<Task> tasks) {

        var result = new java.util.LinkedHashSet<BaseInfo>();
        for(var task : tasks) {
            if(task instanceof FetchAnimeInfoTask fetchAnimeTask) {
                var info = fetchAnimeTask.getResult();
                if(info != null) result.add(info);
            } else if(task instanceof FetchEpisodeInfoTask fetchEpisodeTask) {
                var infoSet = fetchEpisodeTask.getResult();
                if(infoSet != null) result.addAll(infoSet);
            } else if(task instanceof FetchTorrentPageTask fetchTorrentPageTask) {
                var infoSet = fetchTorrentPageTask.getResultSet();
                if(infoSet != null) result.addAll(infoSet);
            } else if(task instanceof FetchTorrentInfoTask fetchTorrentInfoTask) {
                var torrentData = fetchTorrentInfoTask.getResult();
                if(torrentData != null && torrentData.length > 0) result.add(new TorrentInfo(torrentData));
            }
        }
        return result;
    }

    private static boolean canMergeBaseInfoSet(MainApplication mainApp, String variableName) {

        if(!mainApp.variables.containsKey(variableName)) return true;
        if(asBaseInfoSet(mainApp.variables.get(variableName)) != null) return true;
        System.out.println(color("Variable " + variableName + " exists but is not Set<? extends BaseInfo>; command skipped.", ColorCode.BOLD_RED));
        return false;
    }

    private static boolean canMergeTaskSet(MainApplication mainApp, String variableName) {

        if(!mainApp.variables.containsKey(variableName)) return true;
        if(asTaskSet(mainApp.variables.get(variableName)) != null) return true;
        System.out.println(color("Variable " + variableName + " exists but is not Set<? extends Task>; command skipped.", ColorCode.BOLD_RED));
        return false;
    }

    private static Set<BaseInfo> getBaseInfoSetVariable(MainApplication mainApp, String variableName) {

        if(!mainApp.variables.containsKey(variableName)) {
            System.out.println(color("Variable not found: " + variableName, ColorCode.BOLD_RED));
            return null;
        }

        var result = asBaseInfoSet(mainApp.variables.get(variableName));
        if(result == null) {
            System.out.println(color("Variable " + variableName + " is not Set<? extends BaseInfo>; command skipped.", ColorCode.BOLD_RED));
        }
        return result;
    }

    private static Set<Task> getTaskSetVariable(MainApplication mainApp, String variableName) {

        if(!mainApp.variables.containsKey(variableName)) {
            System.out.println(color("Variable not found: " + variableName, ColorCode.BOLD_RED));
            return null;
        }

        var result = asTaskSet(mainApp.variables.get(variableName));
        if(result == null) {
            System.out.println(color("Variable " + variableName + " is not Set<? extends Task>; command skipped.", ColorCode.BOLD_RED));
        }
        return result;
    }

    private static void mergeBaseInfoSet(MainApplication mainApp, String variableName, Set<? extends BaseInfo> items) {

        var result = new java.util.LinkedHashSet<BaseInfo>();
        if(mainApp.variables.containsKey(variableName)) result.addAll(asBaseInfoSet(mainApp.variables.get(variableName)));
        result.addAll(items);
        mainApp.variables.put(variableName, result);
    }

    private static void mergeTaskSet(MainApplication mainApp, String variableName, Set<? extends Task> tasks) {

        var result = new java.util.LinkedHashSet<Task>();
        if(mainApp.variables.containsKey(variableName)) result.addAll(asTaskSet(mainApp.variables.get(variableName)));
        result.addAll(tasks);
        mainApp.variables.put(variableName, result);
    }

    private static Set<BaseInfo> asBaseInfoSet(Object value) {

        if(!(value instanceof Set<?> set)) return null;

        var result = new java.util.LinkedHashSet<BaseInfo>();
        for(var item : set) {
            if(!(item instanceof BaseInfo info)) return null;
            result.add(info);
        }
        return result;
    }

    private static Set<Task> asTaskSet(Object value) {

        if(!(value instanceof Set<?> set)) return null;

        var result = new java.util.LinkedHashSet<Task>();
        for(var item : set) {
            if(!(item instanceof Task task)) return null;
            result.add(task);
        }
        return result;
    }

    private static void appendLogValue(StringBuilder sb, Object value) {

        if(value == null) {
            sb.append("null\n");
        } else if(value instanceof Iterable<?> iterable) {
            for(var item : iterable) sb.append(String.valueOf(item)).append("\n");
        } else if(value instanceof java.util.Map<?, ?> map) {
            for(var entry : map.entrySet()) {
                sb.append(String.valueOf(entry.getKey()))
                    .append(": ")
                    .append(String.valueOf(entry.getValue()))
                    .append("\n");
            }
        } else if(value.getClass().isArray()) {
            var length = java.lang.reflect.Array.getLength(value);
            for(var i = 0; i < length; i++) {
                sb.append(String.valueOf(java.lang.reflect.Array.get(value, i))).append("\n");
            }
        } else {
            sb.append(String.valueOf(value)).append("\n");
        }
    }
}
