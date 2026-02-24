package Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Database.Item.DatabaseItem;
import Database.Item.UpdateItem;
import Database.Item.UpsertItem;
import Database.SQLiteAccess;
import Excel.ExcelReader;
import Excel.ExcelResult;
import FetchTask.FetchTaskManager;
import MetaData.TestMetaData;
import Util.DatabaseItemBuilder;
import Util.StoreItemBuilderAni;
import Util.StoreItemBuilderAniTor;
import Util.StoreItemBuilderEpi;
import Util.Util;


public class Main {

    // 保存结果
    private final static Map<String, List<? extends DatabaseItem>> dbItemMap    = new HashMap<>();
    private final static Map<String, FetchTaskManager>             fetchTaskMap = new HashMap<>(); // 任务列表

    private static ExcelResult excelResult;


    public static void main(String[] args) throws IOException {
        System.out.println("Main");

        Files.createDirectories(Path.of(TestMetaData.LOG_PATH)); // 确保日志目录存在

        // 读取 Excel 文件并解析数据
        excelResult = ReadExcel(TestMetaData.EXCEL_FILE_KG_PATH);
        Util.WriteStringToFile(excelResult.toString(), TestMetaData.LOG_PATH + "01.excel_result.txt");
        System.out.println(excelResult.getVariables());
        System.out.println(excelResult.getCommandsInfo());

        // 依次执行命令
        // 根据命令执行对应的操作
        var cmds = excelResult.commands();
        var it   = cmds.iterator();
        while(it.hasNext()) {
            var cmd = it.next();
            switch(cmd.get(0)) {
            case "_item_ani_store" -> item_ani_store(cmd);
            case "_item_epi_store" -> item_epi_store(cmd);
            case "_item_tor_store" -> item_ani_tor_store(cmd);
            case "_fetch_task_ani" -> fetch_task_ani(cmd);
            case "_fetch_task_epi" -> fetch_task_epi(cmd);
            case "_fetch_task_tor" -> fetch_task_ani_tor(cmd);
            case "_run_fetch_task" -> run_fetch_task(cmd);
            case "_to_db"          -> to_db(cmd);
            default                -> unknown_command(cmd);
            }
        }

       System.out.println("完成");
    }

    private static void item_ani_store(List<String> cmd) {
        var varName    = cmd.get(1);                 // 变量名
        var blockNames = cmd.subList(2, cmd.size()); // 表格数据块名称列表
        var builder    = new StoreItemBuilderAni();     // 构建器

        buildDatabaseItemsFromBlocks(builder, blockNames, varName);
    }

    private static void item_epi_store(List<String> cmd) {
        var varName    = cmd.get(1);
        var blockNames = cmd.subList(2, cmd.size());
        var builder    = new StoreItemBuilderEpi();
       
        buildDatabaseItemsFromBlocks(builder, blockNames, varName);
    }

    private static void item_ani_tor_store(List<String> cmd) {
        var varName    = cmd.get(1);
        var blockNames = cmd.subList(2, cmd.size());
        var builder    = new StoreItemBuilderAniTor();

        buildDatabaseItemsFromBlocks(builder, blockNames, varName);   
    }

    private static void fetch_task_ani(List<String> cmd) {
        var varName       = cmd.get(1);
        var blockNames    = cmd.subList(2, cmd.size());

        var fetchTaskItem = new FetchTaskManager();
        var blockDataList = excelResult.getBlockDataByNames(blockNames);

        fetchTaskItem.addFetchTaskAni(blockDataList);
        if(!fetchTaskItem.isEmpty()) fetchTaskMap.put(varName, fetchTaskItem);
    }

    private static void fetch_task_epi(List<String> cmd) {
        var varName       = cmd.get(1);
        var blockNames    = cmd.subList(2, cmd.size());

        var fetchTaskItem = new FetchTaskManager();
        var blockDataList = excelResult.getBlockDataByNames(blockNames);

        fetchTaskItem.addFetchTaskEpi(blockDataList);
        if(!fetchTaskItem.isEmpty()) fetchTaskMap.put(varName, fetchTaskItem);
    }

    private static void fetch_task_ani_tor(List<String> cmd) {
        var varName       = cmd.get(1);
        var blockNames    = cmd.subList(2, cmd.size());

        var fetchTaskItem = new FetchTaskManager();
        var blockDataList = excelResult.getBlockDataByNames(blockNames);

        fetchTaskItem.addFetchTaskAniTor(blockDataList);
        if(!fetchTaskItem.isEmpty()) fetchTaskMap.put(varName, fetchTaskItem);
    }

    private static void run_fetch_task(List<String> cmd) {
        var varUpdateName = cmd.get(2);

        // 运行任务
        List<FetchTaskManager> runTaskManagerList = new ArrayList<>();
        for(var i = 3; i < cmd.size(); i++) {
            var taskName      = cmd.get(i);
            var fetchTaskItem = fetchTaskMap.get(taskName);
            if(fetchTaskItem != null) try {
                runTaskManagerList.add(fetchTaskItem);

                System.out.println("Running FetchTask: " + taskName);

                fetchTaskItem.runAllTasks();
                fetchTaskItem.saveLog(TestMetaData.LOG_PATH, taskName); // 保存日志
            }
            catch(Exception e) {
                System.err.println("Main: 运行任务 " + taskName + " 时发生错误: " + e.getMessage());
            }
        }

        // 合并结果
        List<UpdateItem> combinedUpdate = new ArrayList<>();
        for(var fetchTaskItem : runTaskManagerList) {
            combinedUpdate.addAll(fetchTaskItem.getUpdateItemList());
        }

        // 保存结果
        if(!combinedUpdate.isEmpty()) dbItemMap.put(varUpdateName, combinedUpdate);

        // // 获取所有 InfoAniTorFetch 项
        // List<InfoAniTorFetch> infoAniTorFetchList = new ArrayList<>();
        // for(var item : combinedUpdate) {
        //     if(item instanceof InfoAniTorFetch) {
        //         infoAniTorFetchList.add((InfoAniTorFetch)item);
        //     }
        // }

        // // 检查数据库中不存在的 TOR_HASH 列表
        // List<InfoAniTorFetch> notExistInfoAniTorFetchList = new ArrayList<>();
        // if(!infoAniTorFetchList.isEmpty()) {
        //     try(var db = new SQLiteAccess(TestMetaData.DATABASE_PATH)) {
        //         notExistInfoAniTorFetchList = db.getTorrentHashNotExist(infoAniTorFetchList);
        //     } catch(SQLException e) {
        //         System.err.println("数据库操作失败: " + e.getMessage());
        //     }
        // }

        // // 下载不存在的 TOR_HASH 对应的种子文件，并保存到 dbItemMap 中
        // if(!notExistInfoAniTorFetchList.isEmpty()) {
        //     var downloadFetchTask = new FetchTaskManager();
        //     downloadFetchTask.addFetchTaskTor(notExistInfoAniTorFetchList);
        //     try {
        //         System.out.println("开始下载种子文件: " + notExistInfoAniTorFetchList.size() + " 个");
        //         downloadFetchTask.runAllTasks();
        //     } catch(Exception e) {
        //         System.err.println("下载种子文件时发生错误: " + e.getMessage());
        //     }

        //     var downloadedUpsert = downloadFetchTask.getUpsertItemList();
        //     var downloadedUpdate = downloadFetchTask.getUpdateItemList();

        //     // 保存到 dbItemMap 中
        //     if(!downloadedUpsert.isEmpty()) dbItemMap.put("tor_upsert", downloadedUpsert);
        //     if(!downloadedUpdate.isEmpty()) dbItemMap.put("tor_update", downloadedUpdate);
        // }
    }

    private static void to_db(List<String> cmd) {

        System.out.print("正在同步到数据库");

        List<DatabaseItem> dbItems = new ArrayList<>();
        for(var i = 1; i < cmd.size(); i++) {
            var varName = cmd.get(i);
            var items   = dbItemMap.get(varName);
            if(items != null && !items.isEmpty()) {
                dbItems.addAll(items);
            }
        }
        ToDatabase(dbItems, TestMetaData.DATABASE_PATH);

        System.out.println(" - 完成\n");
    }

    private static void unknown_command(List<String> cmd) {
        System.out.println("Unknown Command: " + cmd.get(0));
    }

    private static void buildDatabaseItemsFromBlocks(DatabaseItemBuilder builder, List<String> blockNames, String varName) {

        // 从 blockDataList 中找到对应 blockName 的数据块，转换成 DatabaseItem 对象，并返回列表
        List<DatabaseItem> items         = new ArrayList<>();
        var                blockDataList = excelResult.getBlockDataByNames(blockNames);
        for(var blockData : blockDataList) {
            var converted = builder.build(blockData);
            if(converted != null && !converted.isEmpty()) items.addAll(converted);
        }
        if(!items.isEmpty()) dbItemMap.put(varName, items);

        Util.WriteItemListToFile(items, TestMetaData.LOG_PATH + varName + "_store.txt");
    }

    // 读取表格
    public static ExcelResult ReadExcel(String excelFilePath) throws IOException {
        System.out.println("Reading excel file...");
        var excelResult = new ExcelReader().Read(excelFilePath);

        // 将 excelReader.commands 保存到文件
        System.out.println("Saving commands to file...");
        try(var writer = Files.newBufferedWriter(Path.of(TestMetaData.OUTPUT_EXCEL_CMDS))) {
            writer.write(excelResult.getCommandsInfo());
        }

        // 将 blockDataList 保存到文件
        System.out.println("Saving block data...");
        try(var writer = Files.newBufferedWriter(Path.of(TestMetaData.OUTPUT_EXCEL_BLOCKS))) {
            writer.write(excelResult.getBlocksInfo());
        }

        return excelResult;
    }

    // 保存到数据库
    public static void ToDatabase(List<? extends DatabaseItem> items, String databasePath) {

        List<UpsertItem> upsertList = new ArrayList<>();
        List<UpdateItem> updateList = new ArrayList<>();
        for(var item : items) {
            if(item instanceof UpsertItem) upsertList.add((UpsertItem)item);
            if(item instanceof UpdateItem) updateList.add((UpdateItem)item);
        }

        try(var db = new SQLiteAccess(databasePath)) {
            db.Upsert(upsertList);
            db.Update(updateList);
        } catch(SQLException e) {
            System.err.println("Database operation error: " + e.getMessage());
        }
    }
}
