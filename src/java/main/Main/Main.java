package Main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import Database.InfoItem.DatabaseItem;
import Database.InfoItem.UpdateItem;
import Database.InfoItem.UpsertItem;

import Excel.BlockData;
import Excel.ExcelReader;
import FetchTask.FetchTask;
import MetaData.TestMetaData;
import util.TableData;

public class Main {

    // 任务容器
    private static class FetchTaskItem {
        List<FetchTask> fetchTasks = new ArrayList<>();
        List<UpsertItem> bufferUpsert = new ArrayList<>();
        List<UpdateItem> bufferUpdate = new ArrayList<>();
    }

    @FunctionalInterface
    private interface FetchTaskFactory {
        List<? extends FetchTask> create(List<UpsertItem> upsertBuffer, List<UpdateItem> updateBuffer,
                BlockData blockData);
    }

    private static Map<String, BlockData> indexBlocks(List<BlockData> blockDataList) {
        var map = new HashMap<String, BlockData>();
        for (var block : blockDataList) {
            map.putIfAbsent(block.block_name, block);
        }
        return map;
    }

    private static <T extends DatabaseItem> void handleItemStore(
            String varName,
            List<String> blockNames,
            Map<String, BlockData> blockLookup,
            Map<String, List<? extends DatabaseItem>> dbItemMap,
            Function<TableData, List<T>> converter) {

        var items = new ArrayList<T>();
        for (var blockName : blockNames) {
            var blockData = blockLookup.get(blockName);
            if (blockData == null)
                continue;
            var converted = converter.apply(blockData);
            if (converted != null && !converted.isEmpty())
                items.addAll(converted);
        }
        if (!items.isEmpty())
            dbItemMap.put(varName, items);
    }

    private static void handleFetchTask(
            String varName,
            List<String> blockNames,
            Map<String, BlockData> blockLookup,
            Map<String, FetchTaskItem> fetchTaskMap,
            FetchTaskFactory factory) {

        var fetchTaskItem = new FetchTaskItem();
        for (var blockName : blockNames) {
            var blockData = blockLookup.get(blockName);
            if (blockData == null)
                continue;
            var tasks = factory.create(fetchTaskItem.bufferUpsert, fetchTaskItem.bufferUpdate, blockData);
            if (tasks != null && !tasks.isEmpty())
                fetchTaskItem.fetchTasks.addAll(tasks);
        }
        if (!fetchTaskItem.fetchTasks.isEmpty())
            fetchTaskMap.put(varName, fetchTaskItem);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Main");

        var excelReader = new ExcelReader(TestMetaData.EXCEL_FILE_KG_N_PATH);
        var blockDataList = excelReader.getBlockDataList();
        var blockLookup = indexBlocks(blockDataList);

        System.out.println(excelReader.getCommandsInfo());

        // 保存结果
        Map<String, List<? extends DatabaseItem>> dbItemMap = new HashMap<>();
        Map<String, FetchTaskItem> fetchTaskMap = new HashMap<>();

        // 解析命令
        var cmds = excelReader.getCommands();
        var it = cmds.iterator();
        while (it.hasNext()) {
            var cmd = it.next();

            switch (cmd.get(0)) {

                // 处理 _item_ani_store 命令
                case "_item_ani_store" -> {
                    var varName = cmd.get(1);
                    handleItemStore(varName,
                            cmd.subList(2, cmd.size()),
                            blockLookup, dbItemMap,
                            ItemTranslation::convertInfoAniStore);
                }

                // 处理 _item_epi_store 命令
                case "_item_epi_store" -> {
                    var varName = cmd.get(1);
                    handleItemStore(varName,
                            cmd.subList(2, cmd.size()),
                            blockLookup, dbItemMap,
                            ItemTranslation::convertInfoEpiStore);
                }

                // 处理 _item_tor_store 命令
                case "_item_tor_store" -> {
                    var varName = cmd.get(1);
                    handleItemStore(varName,
                            cmd.subList(2, cmd.size()),
                            blockLookup, dbItemMap,
                            ItemTranslation::convertInfoTorStore);
                }

                // 处理 _fetch_task_ani 命令
                case "_fetch_task_ani" -> {
                    var varName = cmd.get(1);
                    handleFetchTask(varName,
                            cmd.subList(2, cmd.size()),
                            blockLookup, fetchTaskMap,
                            ItemTranslation::createFetchTaskAni);
                }

                // 处理 _fetch_task_epi 命令
                case "_fetch_task_epi" -> {
                    var varName = cmd.get(1);
                    handleFetchTask(varName,
                            cmd.subList(2, cmd.size()),
                            blockLookup, fetchTaskMap,
                            ItemTranslation::createFetchTaskEpi);
                }

                // 处理 _fetch_task_tor 命令
                case "_fetch_task_tor" -> {
                    var varName = cmd.get(1);
                    handleFetchTask(varName,
                            cmd.subList(2, cmd.size()),
                            blockLookup, fetchTaskMap,
                            ItemTranslation::createFetchTaskTor);
                }

                // _run_fetch_task
                case "_run_fetch_task" -> {
                    var varUpsertName = cmd.get(1);
                    var varUpdateName = cmd.get(2);

                    List<FetchTaskItem> runTaskItemList = new ArrayList<>();
                    for (var i = 3; i < cmd.size(); i++) {
                        var taskName = cmd.get(i);
                        var fetchTaskItem = fetchTaskMap.get(taskName);
                        if (fetchTaskItem != null) {
                            runTaskItemList.add(fetchTaskItem);
                        }
                    }

                    // 合并任务
                    List<FetchTask> runTaskList = new ArrayList<>();
                    for (var fetchTaskItem : runTaskItemList) {
                        runTaskList.addAll(fetchTaskItem.fetchTasks);
                    }

                    // 运行任务
                    MainUtils.RunFetchTasks(runTaskList);

                    // 合并结果
                    List<UpsertItem> combinedUpsert = new ArrayList<>();
                    List<UpdateItem> combinedUpdate = new ArrayList<>();
                    for (var fetchTaskItem : runTaskItemList) {
                        combinedUpsert.addAll(fetchTaskItem.bufferUpsert);
                        combinedUpdate.addAll(fetchTaskItem.bufferUpdate);
                    }

                    // 保存结果
                    if (combinedUpsert.size() > 0)
                        dbItemMap.put(varUpsertName, combinedUpsert);
                    if (combinedUpdate.size() > 0)
                        dbItemMap.put(varUpdateName, combinedUpdate);
                }

                // _to_db
                case "_to_db" -> {
                    List<DatabaseItem> dbItems = new ArrayList<>();
                    for (var i = 1; i < cmd.size(); i++) {
                        var varName = cmd.get(i);
                        var items = dbItemMap.get(varName);
                        if (items != null && items.size() > 0) {
                            dbItems.addAll(items);
                        }
                    }
                    MainUtils.ToDatabase(dbItems, TestMetaData.DATABASE_PATH);
                }

                default -> {
                    // System.out.println("Unknown Command: " + cmd.get(0));
                }
            }
        }

        // 输出结果
        for (var entry : dbItemMap.entrySet()) {
            var varName = entry.getKey();
            var dbItems = entry.getValue();
            var outputPath = "ignore/" + varName + "_output.txt";
            System.out.println("Writing " + varName + " to " + outputPath);
            MainUtils.WriteItemListToFile(dbItems, outputPath);
        }

        // 输出 FetchTask 结果
        for (var entry : fetchTaskMap.entrySet()) {
            var varName = entry.getKey();
            var fetchTaskItem = entry.getValue();
            var outputPath = "ignore/" + varName + "_fetch_tasks_output.txt";
            System.out.println("Writing FetchTasks " + varName + " to " + outputPath);
            MainUtils.WriteItemListToFile(fetchTaskItem.fetchTasks, outputPath);
        }
    }

}
