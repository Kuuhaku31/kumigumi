package Main;

import Database.Item.DatabaseItem;
import Database.Item.UpdateItem;
import Database.Item.UpsertItem;
import Excel.BlockData;
import Excel.ExcelReader;
import FetchTask.FetchTask;
import MetaData.TestMetaData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import util.TableData;


public class Main {

    // 任务容器
    private static class FetchTaskItem {
        List<FetchTask>  fetchTasks   = new ArrayList<>();
        List<UpsertItem> bufferUpsert = new ArrayList<>();
        List<UpdateItem> bufferUpdate = new ArrayList<>();
    }

    @FunctionalInterface
    private interface FetchTaskFactory {
        List<? extends FetchTask> create(
            List<UpsertItem> upsertBuffer,
            List<UpdateItem> updateBuffer,
            BlockData        blockData);
    }

    private static Map<String, BlockData> indexBlocks(List<BlockData> blockDataList) {
        var map = new HashMap<String, BlockData>();
        for(var block : blockDataList) {
            map.putIfAbsent(block.block_name, block);
        }
        return map;
    }

    private static <T extends DatabaseItem> void handleItemStore(
        String                                    varName,
        List<String>                              blockNames,
        Map<String, BlockData>                    blockLookup,
        Map<String, List<? extends DatabaseItem>> dbItemMap,
        Function<TableData, List<T>>              converter) {

        var items = new ArrayList<T>();
        for(var blockName : blockNames) {
            var blockData = blockLookup.get(blockName);
            if(blockData == null)
                continue;
            var converted = converter.apply(blockData);
            if(converted != null && !converted.isEmpty())
                items.addAll(converted);
        }
        if(!items.isEmpty())
            dbItemMap.put(varName, items);
    }

    private static void handleFetchTask(
        String                     varName,
        List<String>               blockNames,
        Map<String, BlockData>     blockLookup,
        Map<String, FetchTaskItem> fetchTaskMap,
        FetchTaskFactory           factory) {

        var fetchTaskItem = new FetchTaskItem();
        for(var blockName : blockNames) {
            var blockData = blockLookup.get(blockName);
            if(blockData == null)
                continue;
            var tasks = factory.create(fetchTaskItem.bufferUpsert, fetchTaskItem.bufferUpdate, blockData);
            if(tasks != null && !tasks.isEmpty())
                fetchTaskItem.fetchTasks.addAll(tasks);
        }
        if(!fetchTaskItem.fetchTasks.isEmpty())
            fetchTaskMap.put(varName, fetchTaskItem);
    }

    private static ExcelReader            excelReader;
    private static List<BlockData>        blockDataList;
    private static Map<String, BlockData> blockLookup;

    static {
        try {
            excelReader   = new ExcelReader(TestMetaData.EXCEL_FILE_KG_PATH);
            blockDataList = excelReader.getBlockDataList();
            blockLookup   = indexBlocks(blockDataList);
        } catch(IOException e) {
            throw new RuntimeException("Failed to initialize ExcelReader", e);
        }
    }

    // 保存结果
    private static Map<String, List<? extends DatabaseItem>> dbItemMap    = new HashMap<>();
    private static Map<String, FetchTaskItem>                fetchTaskMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Main");

        System.out.println(excelReader.getCommandsInfo());

        // 依次执行命令
        // 根据命令执行对应的操作
        var cmds = excelReader.getCommands();
        var it   = cmds.iterator();
        while(it.hasNext()) {
            var cmd = it.next();
            switch(cmd.get(0)) {
            case "_item_ani_store" -> item_ani_store(cmd);
            case "_item_epi_store" -> item_epi_store(cmd);
            case "_item_tor_store" -> item_tor_store(cmd);
            case "_fetch_task_ani" -> fetch_task_ani(cmd);
            case "_fetch_task_epi" -> fetch_task_epi(cmd);
            case "_fetch_task_tor" -> fetch_task_tor(cmd);
            case "_run_fetch_task" -> run_fetch_task(cmd);
            case "_to_db" -> to_db(cmd);
            default -> unknown_command(cmd);
            }
        }

        // 输出结果
        for(var entry : dbItemMap.entrySet()) {
            var varName    = entry.getKey();
            var dbItems    = entry.getValue();
            var outputPath = "ignore/" + varName + "_output.txt";
            System.out.println("Writing " + varName + " to " + outputPath);
            MainUtils.WriteItemListToFile(dbItems, outputPath);
        }

        // 输出 FetchTask 结果
        for(var entry : fetchTaskMap.entrySet()) {
            var varName       = entry.getKey();
            var fetchTaskItem = entry.getValue();
            var outputPath    = "ignore/" + varName + "_fetch_tasks_output.txt";
            System.out.println("Writing FetchTasks " + varName + " to " + outputPath);
            MainUtils.WriteItemListToFile(fetchTaskItem.fetchTasks, outputPath);
        }
    }


    private static void item_ani_store(List<String> cmd) {
        var varName = cmd.get(1);
        handleItemStore(
            varName,
            cmd.subList(2, cmd.size()),
            blockLookup,
            dbItemMap,
            ItemTranslation::convertInfoAniStore);
    }

    private static void item_epi_store(List<String> cmd) {
        var varName = cmd.get(1);
        handleItemStore(
            varName,
            cmd.subList(2, cmd.size()),
            blockLookup,
            dbItemMap,
            ItemTranslation::convertInfoEpiStore);
    }

    private static void item_tor_store(List<String> cmd) {
        var varName = cmd.get(1);
        handleItemStore(
            varName,
            cmd.subList(2, cmd.size()),
            blockLookup,
            dbItemMap,
            ItemTranslation::convertInfoTorStore);
    }

    private static void fetch_task_ani(List<String> cmd) {
        var varName = cmd.get(1);
        handleFetchTask(
            varName,
            cmd.subList(2, cmd.size()),
            blockLookup,
            fetchTaskMap,
            ItemTranslation::createFetchTaskAni);
    }

    private static void fetch_task_epi(List<String> cmd) {
        var varName = cmd.get(1);
        handleFetchTask(
            varName,
            cmd.subList(2, cmd.size()),
            blockLookup,
            fetchTaskMap,
            ItemTranslation::createFetchTaskEpi);
    }

    private static void fetch_task_tor(List<String> cmd) {
        var varName = cmd.get(1);
        handleFetchTask(
            varName,
            cmd.subList(2, cmd.size()),
            blockLookup,
            fetchTaskMap,
            ItemTranslation::createFetchTaskTor);
    }

    private static void run_fetch_task(List<String> cmd) {
        var varUpsertName = cmd.get(1);
        var varUpdateName = cmd.get(2);

        List<FetchTaskItem> runTaskItemList = new ArrayList<>();
        for(var i = 3; i < cmd.size(); i++) {
            var taskName      = cmd.get(i);
            var fetchTaskItem = fetchTaskMap.get(taskName);
            if(fetchTaskItem != null) {
                runTaskItemList.add(fetchTaskItem);
            }
        }

        // 合并任务
        List<FetchTask> runTaskList = new ArrayList<>();
        for(var fetchTaskItem : runTaskItemList) {
            runTaskList.addAll(fetchTaskItem.fetchTasks);
        }

        // 运行任务
        MainUtils.RunFetchTasks(runTaskList);

        // 合并结果
        List<UpsertItem> combinedUpsert = new ArrayList<>();
        List<UpdateItem> combinedUpdate = new ArrayList<>();
        for(var fetchTaskItem : runTaskItemList) {
            combinedUpsert.addAll(fetchTaskItem.bufferUpsert);
            combinedUpdate.addAll(fetchTaskItem.bufferUpdate);
        }

        // 保存结果
        if(!combinedUpsert.isEmpty())
            dbItemMap.put(varUpsertName, combinedUpsert);
        if(!combinedUpdate.isEmpty())
            dbItemMap.put(varUpdateName, combinedUpdate);
    }

    private static void to_db(List<String> cmd) {
        List<DatabaseItem> dbItems = new ArrayList<>();
        for(var i = 1; i < cmd.size(); i++) {
            var varName = cmd.get(i);
            var items   = dbItemMap.get(varName);
            if(items != null && !items.isEmpty()) {
                dbItems.addAll(items);
            }
        }
        MainUtils.ToDatabase(dbItems, TestMetaData.DATABASE_PATH);
    }

    private static void unknown_command(List<String> cmd) {
        System.out.println("Unknown Command: " + cmd.get(0));
    }
}
