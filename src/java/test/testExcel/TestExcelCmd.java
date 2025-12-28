package testExcel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Database.InfoItem.DatabaseItem;
import Database.InfoItem.UpdateItem;
import Database.InfoItem.UpsertItem;
import Database.InfoItem.InfoAni.InfoAniStore;
import Database.InfoItem.InfoEpi.InfoEpiStore;
import Database.InfoItem.InfoTor.InfoTorStore;

import Excel.ExcelReader;
import FetchTask.FetchTask;
import Main.ItemTranslation;
import MetaData.TestMetaData;

public class TestExcelCmd {
    public static void main(String[] args) throws IOException {
        System.out.println("TestExcelCmd");

        var excelReader = new ExcelReader(TestMetaData.EXCEL_FILE_KG_N_PATH);
        var blockDataList = excelReader.getBlockDataList();

        System.out.println(excelReader.getCommandsInfo());

        // 工具类
        class FetchTaskItem {
            List<FetchTask> fetchTasks = new ArrayList<>();
            List<UpsertItem> bufferUpsert = new ArrayList<>();
            List<UpdateItem> bufferUpdate = new ArrayList<>();
        }

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
                    List<InfoAniStore> infoAniStore = new ArrayList<>();
                    for (var i = 2; i < cmd.size(); i++) {
                        var block_name = cmd.get(i);
                        var blockData = blockDataList.stream()
                                .filter(b -> b.block_name.equals(block_name))
                                .findFirst()
                                .orElse(null);
                        if (blockData != null)
                            infoAniStore.addAll(ItemTranslation.convertInfoAniStore(blockData));
                    }

                    // 添加到结果中
                    if (infoAniStore.size() > 0)
                        dbItemMap.put(varName, infoAniStore);
                }

                // 处理 _item_epi_store 命令
                case "_item_epi_store" -> {
                    var varName = cmd.get(1);
                    List<InfoEpiStore> infoEpiStore = new ArrayList<>();
                    for (var i = 2; i < cmd.size(); i++) {
                        var block_name = cmd.get(i);
                        var blockData = blockDataList.stream()
                                .filter(b -> b.block_name.equals(block_name))
                                .findFirst()
                                .orElse(null);
                        if (blockData != null)
                            infoEpiStore.addAll(ItemTranslation.convertInfoEpiStore(blockData));
                    }

                    // 添加到结果中
                    if (infoEpiStore.size() > 0)
                        dbItemMap.put(varName, infoEpiStore);
                }

                // 处理 _item_tor_store 命令
                case "_item_tor_store" -> {
                    var varName = cmd.get(1);
                    List<InfoTorStore> infoTorStore = new ArrayList<>();
                    for (var i = 2; i < cmd.size(); i++) {
                        var block_name = cmd.get(i);
                        var blockData = blockDataList.stream()
                                .filter(b -> b.block_name.equals(block_name))
                                .findFirst()
                                .orElse(null);
                        if (blockData != null)
                            infoTorStore.addAll(ItemTranslation.convertInfoTorStore(blockData));
                    }
                    // 添加到结果中
                    if (infoTorStore.size() > 0)
                        dbItemMap.put(varName, infoTorStore);
                }

                // 处理 _fetch_task_ani 命令
                case "_fetch_task_ani" -> {
                    var varName = cmd.get(1);
                    var fetchTaskItem = new FetchTaskItem();
                    for (var i = 2; i < cmd.size(); i++) {
                        var block_name = cmd.get(i);
                        var blockData = blockDataList.stream()
                                .filter(b -> b.block_name.equals(block_name))
                                .findFirst()
                                .orElse(null);
                        if (blockData != null) {
                            var tasks = ItemTranslation.createFetchTaskAni(
                                    fetchTaskItem.bufferUpsert,
                                    fetchTaskItem.bufferUpdate,
                                    blockData);
                            if (tasks != null && tasks.size() > 0) {
                                fetchTaskItem.fetchTasks.addAll(tasks);
                            }
                        }
                    }
                    if (fetchTaskItem.fetchTasks.size() > 0)
                        fetchTaskMap.put(varName, fetchTaskItem);
                }

                // 处理 _fetch_task_epi 命令
                case "_fetch_task_epi" -> {
                    var varName = cmd.get(1);
                    var fetchTaskItem = new FetchTaskItem();
                    for (var i = 2; i < cmd.size(); i++) {
                        var block_name = cmd.get(i);
                        var blockData = blockDataList.stream()
                                .filter(b -> b.block_name.equals(block_name))
                                .findFirst()
                                .orElse(null);
                        if (blockData != null) {
                            var tasks = ItemTranslation.createFetchTaskEpi(
                                    fetchTaskItem.bufferUpsert,
                                    fetchTaskItem.bufferUpdate,
                                    blockData);
                            if (tasks != null && tasks.size() > 0) {
                                fetchTaskItem.fetchTasks.addAll(tasks);
                            }
                        }
                    }
                    if (fetchTaskItem.fetchTasks.size() > 0)
                        fetchTaskMap.put(varName, fetchTaskItem);
                }

                // 处理 _fetch_task_tor 命令
                case "_fetch_task_tor" -> {
                    var varName = cmd.get(1);
                    var fetchTaskItem = new FetchTaskItem();
                    for (var i = 2; i < cmd.size(); i++) {
                        var block_name = cmd.get(i);
                        var blockData = blockDataList.stream()
                                .filter(b -> b.block_name.equals(block_name))
                                .findFirst()
                                .orElse(null);
                        if (blockData != null) {
                            var tasks = ItemTranslation.createFetchTaskTor(
                                    fetchTaskItem.bufferUpsert,
                                    fetchTaskItem.bufferUpdate,
                                    blockData);
                            if (tasks != null && tasks.size() > 0) {
                                fetchTaskItem.fetchTasks.addAll(tasks);
                            }
                        }
                    }
                    if (fetchTaskItem.fetchTasks.size() > 0)
                        fetchTaskMap.put(varName, fetchTaskItem);
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
                    Main.MainUtils.RunFetchTasks(runTaskList);

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
                    Main.MainUtils.ToDatabase(dbItems, TestMetaData.DATABASE_PATH);
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
            Main.MainUtils.WriteItemListToFile(dbItems, outputPath);
        }

        // 输出 FetchTask 结果
        for (var entry : fetchTaskMap.entrySet()) {
            var varName = entry.getKey();
            var fetchTaskItem = entry.getValue();
            var outputPath = "ignore/" + varName + "_fetch_tasks_output.txt";
            System.out.println("Writing FetchTasks " + varName + " to " + outputPath);
            Main.MainUtils.WriteItemListToFile(fetchTaskItem.fetchTasks, outputPath);
        }
    }

}
