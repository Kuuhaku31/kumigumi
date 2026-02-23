package Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import Database.Item.DatabaseItem;
import Database.Item.UpdateItem;
import Database.Item.UpsertItem;
import Database.SQLiteAccess;
import Excel.BlockData;
import Excel.ExcelReader;
import FetchTask.FetchTask;
import FetchTask.FetchTaskAni;
import FetchTask.FetchTaskEpi;
import FetchTask.FetchTaskTor;
import MetaData.TestMetaData;


public class MainUtils {
    public static void WriteItemListToFile(List<?> itemList, String filePath) throws IOException {
        // 保证目录存在
        Files.createDirectories(Path.of(filePath).getParent());

        // 写入文件
        try(var writer = Files.newBufferedWriter(Path.of(filePath))) {
            for(var item : itemList) {
                writer.write(item.toString());
                writer.write("\n");
            }
        }
    }

    // 读取表格
    public static List<BlockData> ReadExcel(String excelFilePath) throws IOException {
        System.out.println("Reading excel file...");
        var excelReader = new ExcelReader(excelFilePath);

        // 将 excelReader.commands 保存到文件
        System.out.println("Saving commands to file...");
        try(var writer = Files.newBufferedWriter(Path.of(TestMetaData.OUTPUT_EXCEL_CMDS))) {
            writer.write(excelReader.getCommandsInfo());
        }

        // 将 blockDataList 保存到文件
        System.out.println("Saving block data...");
        try(var writer = Files.newBufferedWriter(Path.of(TestMetaData.OUTPUT_EXCEL_BLOCKS))) {
            writer.write(excelReader.getBlocksInfo());
        }

        return excelReader.getBlockDataList();
    }

    // 保存到数据库
    public static void ToDatabase(List<? extends DatabaseItem> items, String databasePath) {

        List<UpsertItem> upsertList = new ArrayList<>();
        List<UpdateItem> updateList = new ArrayList<>();
        for(var item : items) {
            if(item instanceof UpsertItem)
                upsertList.add((UpsertItem)item);
            else if(item instanceof UpdateItem)
                updateList.add((UpdateItem)item);
            else {
                System.err.print("Unknown DatabaseItem type: ");
                System.err.print(item.getClass().getName());
                System.err.print(", item: ");
                System.err.println(item.toString());
            }
        }

        try(var db = new SQLiteAccess(databasePath)) {
            db.Upsert(upsertList);
            db.Update(updateList);
        } catch(SQLException e) {
            System.err.println("Database operation error: " + e.getMessage());
        }
    }

    // 构建 FetchTask 列表
    public static List<FetchTask> BuildFetchTasks(
        List<UpsertItem> upsertBuffer,
        List<UpdateItem> fetchBuffer,
        BlockData        blockData) {
        if(blockData == null)
            return null;

        // 确保关键字段存在
        var ani_id_index  = blockData.GetHeaderIndex("ANI_ID");
        var url_rss_index = blockData.GetHeaderIndex("url_rss");
        if(ani_id_index == -1 || url_rss_index == -1)
            return null;

        // 创建任务
        List<FetchTask> res = new ArrayList<>();
        for(var row : blockData.GetData()) {
            var ani_id  = Integer.parseInt(row[ani_id_index]);
            var url_rss = row[url_rss_index];

            // 创建任务
            res.add(new FetchTaskAni(upsertBuffer, fetchBuffer, ani_id));
            res.add(new FetchTaskEpi(upsertBuffer, fetchBuffer, ani_id));
            if(url_rss != null && !url_rss.isBlank())
                res.add(new FetchTaskTor(upsertBuffer, fetchBuffer, new ArrayList<>(), url_rss, ani_id));
        }
        return res;
    }

    // 执行 FetchTask 列表
    public static void RunFetchTasks(List<FetchTask> taskList) {
        if(taskList == null || taskList.isEmpty()) {
            return;
        }

        var           executor  = Executors.newFixedThreadPool(4);
        AtomicInteger completed = new AtomicInteger(0);
        int           total     = taskList.size();

        for(var task : taskList) {
            executor.submit(() -> {
                try {
                    task.run();
                } finally {
                    int count = completed.incrementAndGet();
                    if(count % 10 == 0) {
                        System.out.println("Progress: " + count + "/" + total + " tasks completed");
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch(InterruptedException e) {
            System.err.println("Task execution interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
