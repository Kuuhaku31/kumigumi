package TestMain;

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
import Database.InfoItem.DatabaseItem;
import Database.InfoItem.UpdateItem;
import Database.InfoItem.UpsertItem;
import Excel.ExcelReader;
import FetchTask.FetchTask;
import FetchTask.FetchTaskAni;
import FetchTask.FetchTaskEpi;
import FetchTask.FetchTaskTor;
import MetaData.TestMetaData;
import util.TableData.BlockData;

public class TestMainUtils {
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
            writer.write(excelReader.getCommandsInfo());
        }

        // 将 blockDataList 保存到文件
        System.out.println("Saving block data...");
        try (var writer = Files.newBufferedWriter(Path.of(TestMetaData.OUTPUT_EXCEL_BLOCKS))) {
            writer.write(excelReader.getBlocksInfo());
        }

        return excelReader.getBlockDataList();
    }

    // 保存到数据库
    static void ToDatabase(List<? extends DatabaseItem> items, String databasePath) {

        List<UpsertItem> upsertList = new ArrayList<>();
        List<UpdateItem> updateList = new ArrayList<>();
        for (var item : items) {
            if (item instanceof UpsertItem)
                upsertList.add((UpsertItem) item);
            else if (item instanceof UpdateItem)
                updateList.add((UpdateItem) item);
            else {
                System.err.print("Unknown DatabaseItem type: ");
                System.err.print(item.getClass().getName());
                System.err.print(", item: ");
                System.err.println(item.toString());
            }
        }

        try (var db = new SQLiteAccess(databasePath)) {
            db.Upsert(upsertList);
            db.Update(updateList);
        } catch (SQLException e) {
            System.err.println("Database operation error: " + e.getMessage());
        }
    }

    // 构建 FetchTask 列表
    static List<FetchTask> BuildFetchTasks(
            List<UpsertItem> upsertBuffer,
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
            res.add(new FetchTaskAni(upsertBuffer, fetchBuffer, ani_id));
            res.add(new FetchTaskEpi(upsertBuffer, fetchBuffer, ani_id));
            if (url_rss != null && !url_rss.isBlank())
                res.add(new FetchTaskTor(upsertBuffer, fetchBuffer, url_rss, ani_id));

        }
        return res;
    }

    // 带进度条的多线程运行 FetchTask
    static void RunFetchTasks(List<FetchTask> taskList) throws IOException {
        if (taskList == null || taskList.size() == 0) {
            System.out.println("No tasks to run.");
            return;
        }
        System.out.println("Starting concurrent task execution...");

        var MAX_THREADS = 32;

        var task_count = taskList.size(); // 总数
        var finished = new AtomicInteger(0); // 完成数
        showProgress(0, task_count); // 更新进度条

        // 并发执行任务
        var pool = Executors.newFixedThreadPool(MAX_THREADS);
        for (var task : taskList) {
            pool.submit(() -> {
                task.run();
                var done = finished.incrementAndGet(); // 完成数加一
                showProgress(done, task_count); // 更新进度条
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

        showProgress(task_count, task_count); // 最终更新进度条

        // 输出结果
        if (ok)
            System.out.println("并发任务完成");
        else
            System.err.println("并发任务出现异常");

        System.out.println("All tasks completed.");
    }

    // 控制台进度条显示函数
    private static synchronized void showProgress(int done, int total) {
        int percent = (int) ((done * 100.0f) / total);
        int barLen = 30;
        int filled = percent * barLen / 100;
        String bar = "=".repeat(filled) + " ".repeat(barLen - filled);
        System.out.printf("\r开始并发执行任务: [%s] %3d%% (%d/%d)", bar, percent, done, total);
        if (done == total)
            System.out.println();
    }
}
