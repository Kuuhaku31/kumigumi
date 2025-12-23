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
        test4();
    }

    // 获取 2510 数据
    static void test4() throws IOException, SQLException {
        System.out.println("test4");

        List<UpdateItem> updateList = new ArrayList<>();

        // 读取表格
        System.out.println("Reading excel file...");
        var excelReader = new ExcelReader(TestMetaData.EXCEL_FILE_KG_N_PATH);

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

        System.out.println("Creating fetch tasks...");

        // 创建任务
        List<UpsertItem> upsertBuffer = new ArrayList<>();
        List<UpdateItem> fetchBuffer = new ArrayList<>();
        List<FetchTask> fetchTaskList = new ArrayList<>();

        // 处理各个 blockData
        for (var blockData : excelReader.blockDataList) {
            if (blockData.block_name.equals("fetch2510ani")) {
                // 创建任务
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
            } else {
                System.out.println("Unknown block name: " + blockData.block_name);
            }
        }

        // 输出 infoItemList 内容
        try (var writer = Files.newBufferedWriter(Path.of(TestMetaData.OUTPUT_FILE_1))) {

            writer.write("Upsert Items:\n");
            for (var infoItem : upsertBuffer) {
                writer.write(infoItem.toString());
                writer.write("\n");
            }

            writer.write("\nUpdate Items:\n");
            for (var infoItem : updateList) {
                writer.write(infoItem.toString());
                writer.write("\n");
            }
        }

        // 批量运行获取任务
        runFetchTasks(fetchTaskList);

        // 输出任务获取的内容
        try (var writer = Files.newBufferedWriter(Path.of(TestMetaData.OUTPUT_FILE_2))) {
            writer.write("\nFetched Info Items:\n");
            for (var infoItem : fetchBuffer) {
                writer.write(infoItem.toString());
                writer.write("\n");
            }
        }

        // 保存到数据库
        try (var db = new SQLiteAccess(TestMetaData.DATABASE_PATH)) {
            db.Upsert(upsertBuffer);
            db.Update(updateList);
            db.Update(fetchBuffer);
        }
    }

    // 保存 kg-n 的 2510 epi_store 数据到数据库
    static void test3() throws IOException, SQLException {
        System.out.println("test3");

        List<UpdateItem> updateList = new ArrayList<>();

        // 读取表格
        var excelReader = new ExcelReader(TestMetaData.EXCEL_FILE_KG_N_PATH);

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

        // 处理各个 blockData
        for (var blockData : excelReader.blockDataList) {
            if (blockData.block_name.equals("store2510epi")) {
                updateList.addAll(ItemTranslation.convertInfoEpiStore(blockData));
            } else {
                System.out.println("Unknown block name: " + blockData.block_name);
            }
        }

        // 打印 updateList 内容
        try (var writer = Files.newBufferedWriter(Path.of(TestMetaData.OUTPUT_UPDATE_ITEM))) {
            for (var infoItem : updateList) {
                writer.write(infoItem.toString());
                writer.write("\n");
            }
        }

        // 保存到数据库
        try (var db = new SQLiteAccess(TestMetaData.DATABASE_PATH)) {
            db.Update(updateList);
        }
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
        runFetchTasks(taskList);

        // 输出任务获取的内容
        try (var writer = Files.newBufferedWriter(Path.of(TestMetaData.OUTPUT_UPSERT_TASK_ITEM))) {
            writer.write("Upserted Info Items:\n");
            for (var infoItem : upsertBuffer) {
                writer.write(infoItem.toString());
                writer.write("\n");
            }

            writer.write("\nFetched Info Items:\n");
            for (var infoItem : fetchBuffer) {
                writer.write(infoItem.toString());
                writer.write("\n");
            }
        }

        // 插入数据库
        try (var db = new SQLiteAccess(TestMetaData.DATABASE_PATH)) {
            db.Upsert(upsertBuffer);
            db.Update(fetchBuffer);
        }
    }

    static void test1() throws IOException, SQLException {
        System.out.println("test1");

        List<UpdateItem> updateList = new ArrayList<>();

        // 读取表格
        var excelReader = new ExcelReader(TestMetaData.EXCEL_FILE_KG_PATH);

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

        // 处理各个 blockData
        for (var blockData : excelReader.blockDataList) {
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
        try (var writer = Files.newBufferedWriter(Path.of(TestMetaData.OUTPUT_UPDATE_ITEM))) {
            for (var infoItem : updateList) {
                writer.write(infoItem.toString());
                writer.write("\n");
            }
        }

        // 保存到数据库
        try (var db = new SQLiteAccess(TestMetaData.DATABASE_PATH)) {
            db.Update(updateList);
        }
    }

    static void test0() throws IOException, SQLException {
        System.out.println("test0");

        List<UpsertItem> upsertList = new ArrayList<>();
        List<UpdateItem> updateList = new ArrayList<>();

        List<UpsertItem> upsertBuffer = new ArrayList<>();
        List<UpdateItem> fetchBuffer = new ArrayList<>();
        List<FetchTask> fetchTaskList = new ArrayList<>();

        var excelReader = new ExcelReader(TestMetaData.EXCEL_FILE_PATH);
        excelReader.runCommands();

        // 将 blockDataList 保存到文件
        try (var writer = Files.newBufferedWriter(Path.of(TestMetaData.OUTPUT_EXCEL_BLOCKS))) {
            for (var blockData : excelReader.blockDataList) {
                writer.write(blockData.toString());
                writer.write("\n\n");
            }
        }

        // 处理各个 blockData
        for (var blockData : excelReader.blockDataList) {
            if (blockData.block_name.equals("ani_store")) {
                upsertList.addAll(ItemTranslation.convertInfoAniUpsert(blockData));
                updateList.addAll(ItemTranslation.convertInfoAniStore(blockData));
            } else if (blockData.block_name.equals("epi_store")) {
                upsertList.addAll(ItemTranslation.convertInfoEpiUpsert(blockData));
                updateList.addAll(ItemTranslation.convertInfoEpiStore(blockData));
            } else if (blockData.block_name.equals("tor_store")) {
                upsertList.addAll(ItemTranslation.convertInfoTorUpsert(blockData));
                updateList.addAll(ItemTranslation.convertInfoTorStore(blockData));
            } else if (blockData.block_name.equals("ani_epi_fetch")) {
                // 创建任务
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
            } else {
                System.out.println("Unknown block name: " + blockData.block_name);
            }
        }

        // 输出 infoItemList 内容
        try (var writer = Files.newBufferedWriter(Path.of(TestMetaData.OUTPUT_FILE_1))) {

            writer.write("Upsert Items:\n");
            for (var infoItem : upsertList) {
                writer.write(infoItem.toString());
                writer.write("\n");
            }

            writer.write("\nUpdate Items:\n");
            for (var infoItem : updateList) {
                writer.write(infoItem.toString());
                writer.write("\n");
            }
        }

        runFetchTasks(fetchTaskList);

        try (var writer = Files.newBufferedWriter(Path.of(TestMetaData.OUTPUT_FILE_2))) {
            writer.write("\nFetched Info Items:\n");
            for (var infoItem : fetchBuffer) {
                writer.write(infoItem.toString());
                writer.write("\n");
            }
        }

        // 保存到数据库
        try (var db = new SQLiteAccess(TestMetaData.DATABASE_PATH)) {
            db.Upsert(upsertList);
            db.Update(updateList);
            db.Update(fetchBuffer);
        }
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
    static void runFetchTasks(List<FetchTask> taskList) throws IOException {
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
    private static synchronized void ShowProgress(int done, int total) {
        int percent = (int) ((done * 100.0f) / total);
        int barLen = 30;
        int filled = percent * barLen / 100;
        String bar = "=".repeat(filled) + " ".repeat(barLen - filled);
        System.out.printf("\r开始并发执行任务: [%s] %3d%% (%d/%d)", bar, percent, done, total);
        if (done == total)
            System.out.println();
    }
}
