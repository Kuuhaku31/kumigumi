package Def;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import Database.SQLiteAccess;
import Database.InfoItem.UpdateItem;
import Database.InfoItem.UpsertItem;
import Excel.ExcelReader;
import FetchTask.*;
import Main.ItemTranslation;
import MetaData.TestMetaData;
import util.TableData.BlockData;

public class TestMain {

    public void main(String[] args) throws IOException, SQLException {
        System.out.println("TestMain");
        // test0();
        // test1();
        // test2();
        // test3();
        // test4();
        test5(); // store tor
    }

    static void WriteItemListToFile(List<?> itemList, String filePath) throws IOException {
        try (var writer = Files.newBufferedWriter(Path.of(filePath))) {
            for (var item : itemList) {
                writer.write(item.toString());
                writer.write("\n");
            }
        }
    }

    // 读取表格
    static List<BlockData> ReadExcel(String excelFilePath) throws IOException {
        System.out.println("Reading excel file...");
        var excelReader = new ExcelReader(excelFilePath);

        // 将 excelReader.commands 保存到文件
        System.out.println("Saving commands to file...");
        try (var writer = Files.newBufferedWriter(Path.of(TestMetaData.OUTPUT_EXCEL_CMDS))) {
            writer.write(excelReader.getCommands());
        }

        // 运行命令
        System.out.println("Running commands...");
        excelReader.runCommands();

        // 将 blockDataList 保存到文件
        System.out.println("Saving block data...");
        try (var writer = Files.newBufferedWriter(Path.of(TestMetaData.OUTPUT_EXCEL_BLOCKS))) {
            writer.write(excelReader.getBlocks());
        }

        return excelReader.blockDataList;
    }

    static void ToDatabase(List<UpsertItem> upsertList, List<UpdateItem> updateList, String databasePath) {
        try (var db = new SQLiteAccess(databasePath)) {
            db.Upsert(upsertList);
            db.Update(updateList);
        } catch (SQLException e) {
            System.err.println("Database operation error: " + e.getMessage());
        }
    }

    static void fetch2510ani(
            List<UpsertItem> upsertBuffer,
            List<UpdateItem> fetchBuffer,
            List<FetchTask> fetchTaskList,
            BlockData blockData) {
        System.out.println("fetch2510ani");
        // 创建任务
        System.out.println("Creating fetch tasks...");
        var ani_id_Index = blockData.GetHeaderIndex("ANI_ID");
        var url_rss_Index = blockData.GetHeaderIndex("url_rss");
        if (ani_id_Index != -1 && url_rss_Index != -1) {
            for (var row : blockData.GetData()) {
                Integer ani_id = Integer.parseInt(row[ani_id_Index]);
                String url_rss = row[url_rss_Index];
                fetchTaskList.add(new FetchTaskAni(upsertBuffer, fetchBuffer, ani_id));
                fetchTaskList.add(new FetchTaskEpi(upsertBuffer, fetchBuffer, ani_id));
                if (url_rss != null && !url_rss.isBlank())
                    fetchTaskList.add(new FetchTaskTor(upsertBuffer, fetchBuffer, url_rss, ani_id));
            }
        }
    }

    static void store2510epi(
            List<UpdateItem> updateList,
            BlockData blockData) {
        System.out.println("store2510epi");
        updateList.addAll(ItemTranslation.convertInfoEpiStore(blockData));
    }

    static void storeTor(
            List<UpdateItem> updateList,
            BlockData blockData) {
        System.out.println("storeTor");
        updateList.addAll(ItemTranslation.convertInfoTorStore(blockData));
    }

    // 获取 tor 数据
    static void test5() throws IOException, SQLException {
        System.out.println("test5");

        // 读取表格
        var blockDataList = ReadExcel(TestMetaData.EXCEL_FILE_KG_N_PATH);

        // 处理各个 blockData
        List<UpdateItem> updateItemList = new ArrayList<>();
        for (var blockData : blockDataList) {
            if (blockData.block_name.equals("store_tor")) {
                storeTor(updateItemList, blockData);
            } else {
                System.out.println("Unknown block name: " + blockData.block_name);
            }
        }

        // 保存到数据库
        ToDatabase(null, updateItemList, TestMetaData.DATABASE_PATH);
    }

    // 获取 2510 数据
    static void test4() throws IOException, SQLException {
        System.out.println("test4");

        // 读取表格
        var blockDataList = ReadExcel(TestMetaData.EXCEL_FILE_KG_N_PATH);

        // 处理各个 blockData
        List<UpsertItem> upsertBuffer = new ArrayList<>();
        List<UpdateItem> fetchBuffer = new ArrayList<>();
        List<FetchTask> fetchTaskList = new ArrayList<>();
        for (var blockData : blockDataList) {
            if (blockData.block_name.equals("fetch2510ani")) {
                fetch2510ani(upsertBuffer, fetchBuffer, fetchTaskList, blockData);
            } else {
                System.out.println("Unknown block name: " + blockData.block_name);
            }
        }

        // 批量运行获取任务
        RunFetchTasks(fetchTaskList);

        // 输出获取的 map 内容
        try (var writer = Files.newBufferedWriter(Path.of(TestMetaData.OUTPUT_FETCH_MAP))) {
            writer.write(util.Loger.log);
        }

        // 输出任务获取的内容
        WriteItemListToFile(upsertBuffer, TestMetaData.OUTPUT_UPSERT_TASK_ITEM);

        // 保存到数据库
        ToDatabase(upsertBuffer, fetchBuffer, TestMetaData.DATABASE_PATH);
    }

    // 保存 kg-n 的 2510 epi_store 数据到数据库
    static void test3() throws IOException, SQLException {
        System.out.println("test3");

        // 读取表格
        var blockDataList = ReadExcel(TestMetaData.EXCEL_FILE_KG_N_PATH);

        // 处理各个 blockData
        List<UpdateItem> updateList = new ArrayList<>();
        for (var blockData : blockDataList) {
            if (blockData.block_name.equals("store2510epi"))
                store2510epi(updateList, blockData);
            else {
                System.out.println("Unknown block name: " + blockData.block_name);
            }
        }

        // 打印 updateList 内容
        WriteItemListToFile(updateList, TestMetaData.OUTPUT_INFO_ITEMS);

        // 保存到数据库
        ToDatabase(null, updateList, TestMetaData.DATABASE_PATH);
    }

    // 处理 2026-01 表格
    static void test2() throws IOException, SQLException {
        System.out.println("test2");

        // 读取表格
        var excelReader = readExcel(TestMetaData.EXCEL_FILE_KG_N_PATH);

        // 处理各个 blockData
        List<UpsertItem> upsertBuffer = new ArrayList<>(); // 获取的需要插入或者更新的信息
        List<UpdateItem> fetchBuffer = new ArrayList<>(); // 获取的需要更新的信息
        List<FetchTask> taskList = new ArrayList<>(); // 任务列表
        for (var blockData : excelReader.blockDataList) {
            if (blockData.block_name.equals("ani_2601"))
                taskList.addAll(buildFetchTasks(upsertBuffer, fetchBuffer, blockData));
            else
                System.out.println("Unknown block name: " + blockData.block_name);
        }

        // 批量运行获取任务
        RunFetchTasks(taskList);

        // 输出任务获取的内容
        List<Object> allInfoItems = new ArrayList<>();
        allInfoItems.addAll(upsertBuffer);
        allInfoItems.addAll(fetchBuffer);
        WriteItemListToFile(allInfoItems, TestMetaData.OUTPUT_INFO_ITEM);

        // 插入数据库
        ToDatabase(upsertBuffer, fetchBuffer, TestMetaData.DATABASE_PATH);
    }

    static void test1() throws IOException, SQLException {
        System.out.println("test1");

        // 读取表格
        var blockDataList = ReadExcel(TestMetaData.EXCEL_FILE_KG_PATH);

        // 处理各个 blockData
        List<UpdateItem> updateList = new ArrayList<>();
        for (var blockData : blockDataList) {
            if (blockData.block_name.equals("ani_store_2510")) {
                updateList.addAll(ItemTranslation.convertInfoAniStore(blockData));
            } else if (blockData.block_name.equals("epi_store_2510")) {
                updateList.addAll(ItemTranslation.convertInfoEpiStore(blockData));
            } else if (blockData.block_name.equals("tor_store_2510")) {
                updateList.addAll(ItemTranslation.convertInfoTorStore(blockData));
            } else {
                System.out.println("Unknown block name: " + blockData.block_name);
            }
        }

        // 打印 updateList 内容
        WriteItemListToFile(updateList, TestMetaData.OUTPUT_INFO_ITEMS);

        // 保存到数据库
        ToDatabase(null, updateList, TestMetaData.DATABASE_PATH);
    }

    static void test0() throws IOException, SQLException {
        System.out.println("test0");

        var blockDataList = ReadExcel(TestMetaData.EXCEL_FILE_KG_N_PATH);

        // 处理各个 blockData
        List<UpsertItem> upsertListStore = new ArrayList<>();
        List<UpdateItem> updateListStore = new ArrayList<>();

        List<UpsertItem> bufferUpsertFetch = new ArrayList<>();
        List<UpdateItem> bufferUpdateFetch = new ArrayList<>();
        List<FetchTask> fetchTaskList = new ArrayList<>();

        for (var blockData : blockDataList) {
            if (blockData.block_name.equals("ani_store")) {
                upsertListStore.addAll(ItemTranslation.convertInfoAniUpsert(blockData));
                updateListStore.addAll(ItemTranslation.convertInfoAniStore(blockData));
            } else if (blockData.block_name.equals("epi_store")) {
                upsertListStore.addAll(ItemTranslation.convertInfoEpiUpsert(blockData));
                updateListStore.addAll(ItemTranslation.convertInfoEpiStore(blockData));
            } else if (blockData.block_name.equals("tor_store")) {
                upsertListStore.addAll(ItemTranslation.convertInfoTorUpsert(blockData));
                updateListStore.addAll(ItemTranslation.convertInfoTorStore(blockData));
            } else if (blockData.block_name.equals("ani_epi_fetch")) {
                fetchTaskList.addAll(buildFetchTasks(bufferUpsertFetch, bufferUpdateFetch, blockData));
            } else {
                System.out.println("Unknown block name: " + blockData.block_name);
            }
        }

        if (fetchTaskList.isEmpty())
            System.out.println("No fetch tasks to run.");
        else
            RunFetchTasks(fetchTaskList);

        // 输出 infoItemList 内容
        List<Object> allInfoItems = new ArrayList<>();
        allInfoItems.addAll(upsertListStore);
        allInfoItems.addAll(updateListStore);
        allInfoItems.addAll(bufferUpsertFetch);
        allInfoItems.addAll(bufferUpdateFetch);
        WriteItemListToFile(allInfoItems, TestMetaData.OUTPUT_INFO_ITEM);

        // 保存到数据库
        List<UpdateItem> combinedUpdateList = new ArrayList<>(updateListStore);
        combinedUpdateList.addAll(bufferUpdateFetch);
        ToDatabase(upsertListStore, combinedUpdateList, TestMetaData.DATABASE_PATH);
    }

    static ExcelReader readExcel(String path) throws IOException {
        // 读取表格
        var excelReader = new ExcelReader(path);

        // 将 excelReader.commands 保存到文件
        try (var writer = Files.newBufferedWriter(Path.of(TestMetaData.OUTPUT_EXCEL_CMDS))) {
            writer.write(excelReader.getCommands());
        }

        // 运行命令
        excelReader.runCommands();

        // 将 blockDataList 保存到文件
        try (var writer = Files.newBufferedWriter(Path.of(TestMetaData.OUTPUT_EXCEL_BLOCKS))) {
            writer.write(excelReader.getBlocks());
        }

        return excelReader;
    }

    static List<FetchTask> buildFetchTasks(
            List<UpsertItem> UpsertBuffer,
            List<UpdateItem> fetchBuffer,
            BlockData blockData) {
        if (blockData == null)
            return null;

        // 确保关键字段存在
        var ani_id_index = blockData.GetHeaderIndex("ANI_ID");
        var url_rss_index = blockData.GetHeaderIndex("url_rss");
        if (ani_id_index == -1 || url_rss_index == -1)
            return null;

        // 创建任务
        List<FetchTask> res = new ArrayList<>();
        for (var row : blockData.GetData()) {
            var ani_id = Integer.parseInt(row[ani_id_index]);
            var url_rss = row[url_rss_index];

            // 创建任务
            res.add(new FetchTaskAni(UpsertBuffer, fetchBuffer, ani_id));
            res.add(new FetchTaskEpi(UpsertBuffer, fetchBuffer, ani_id));
            if (url_rss != null && !url_rss.isBlank())
                res.add(new FetchTaskTor(UpsertBuffer, fetchBuffer, url_rss, ani_id));

        }
        return res;
    }

    // 带进度条的多线程运行 FetchTask
    static void RunFetchTasks(List<FetchTask> taskList) throws IOException {
        System.out.println("Starting concurrent task execution...");

        var MAX_THREADS = 32;

        var task_count = taskList.size(); // 总数
        var finished = new AtomicInteger(0); // 完成数
        ShowProgress(0, task_count); // 更新进度条

        // 并发执行任务
        var pool = Executors.newFixedThreadPool(MAX_THREADS);
        for (var task : taskList) {
            pool.submit(() -> {
                task.run();
                var done = finished.incrementAndGet(); // 完成数加一
                ShowProgress(done, task_count); // 更新进度条
            });
        }
        pool.shutdown();

        // 等待全部完成
        var ok = false;
        try {
            ok = pool.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }

        // 输出结果
        if (ok)
            System.out.println("并发任务完成");
        else
            System.err.println("并发任务出现异常");

        System.out.println("All tasks completed.");
    }

    // 控制台进度条显示函数
    static synchronized void ShowProgress(int done, int total) {
        int percent = (int) ((done * 100.0f) / total);
        int barLen = 30;
        int filled = percent * barLen / 100;
        String bar = "=".repeat(filled) + " ".repeat(barLen - filled);
        System.out.printf("\r开始并发执行任务: [%s] %3d%% (%d/%d)", bar, percent, done, total);
        if (done == total)
            System.out.println();
    }
}
