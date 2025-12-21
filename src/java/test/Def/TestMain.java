package Def;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import Database.SQLiteAccess;
import Database.InfoItem.UpdateItem;
import Database.InfoItem.UpsertItem;
import Excel.ExcelReader;
import FetchTask.*;
import Main.TableToInfo;
import MetaData.TestMetaData;

public class TestMain {

    static List<UpsertItem> upsertList = new ArrayList<>();
    static List<UpdateItem> updateList = new ArrayList<>();

    static List<UpdateItem> taskBuffer = new ArrayList<>();
    static List<FetchTask> fetchTaskList = new ArrayList<>();

    public static void main(String[] args) throws IOException, SQLException {
        System.out.println("TestMain");

        var excelReader = new ExcelReader(TestMetaData.EXCEL_FILE_PATH);
        excelReader.runCommands();

        // 将 blockDataList 保存到文件
        try (var writer = Files.newBufferedWriter(Path.of(TestMetaData.OUTPUT_FILE_0))) {
            for (var blockData : excelReader.blockDataList) {
                writer.write(blockData.toString());
                writer.write("\n\n");
            }
        }

        // 处理各个 blockData
        for (var blockData : excelReader.blockDataList) {
            if (blockData.block_name.equals("ani_store")) {
                upsertList.addAll(TableToInfo.convertInfoAniUpsert(blockData));
                updateList.addAll(TableToInfo.convertInfoAniStore(blockData));
            } else if (blockData.block_name.equals("epi_store")) {
                upsertList.addAll(TableToInfo.convertInfoEpiUpsert(blockData));
                updateList.addAll(TableToInfo.convertInfoEpiStore(blockData));
            } else if (blockData.block_name.equals("tor_store")) {
                upsertList.addAll(TableToInfo.convertInfoTorUpsert(blockData));
                updateList.addAll(TableToInfo.convertInfoTorStore(blockData));
            } else if (blockData.block_name.equals("ani_epi_fetch")) {
                // 创建任务
                var ani_id_Index = blockData.GetHeaderIndex("ANI_ID");
                var url_rss_Index = blockData.GetHeaderIndex("url_rss");
                if (ani_id_Index != -1 && url_rss_Index != -1) {
                    for (var row : blockData.GetData()) {
                        Integer ani_id = Integer.parseInt(row[ani_id_Index]);
                        String url_rss = row[url_rss_Index];
                        fetchTaskList.add(new FetchTaskAni(taskBuffer, ani_id));
                        fetchTaskList.add(new FetchTaskEpi(taskBuffer, ani_id));
                        if (url_rss != null && !url_rss.isBlank())
                            fetchTaskList.add(new FetchTaskTor(taskBuffer, url_rss, ani_id));
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

        runFetchTasks();

        try (var writer = Files.newBufferedWriter(Path.of(TestMetaData.OUTPUT_FILE_2))) {
            writer.write("\nFetched Info Items:\n");
            for (var infoItem : taskBuffer) {
                writer.write(infoItem.toString());
                writer.write("\n");
            }
        }

        // 保存到数据库
        try (var db = new SQLiteAccess(TestMetaData.DATABASE_PATH)) {
            db.Upsert(upsertList);
            db.Update(updateList);
            db.Update(taskBuffer);
        }
    }

    // 带进度条的多线程运行 FetchTask
    static void runFetchTasks() {
        System.out.println("Starting concurrent task execution...");

        var MAX_THREADS = 10;

        AtomicInteger doneCount = new AtomicInteger(0);
        int totalTasks = fetchTaskList.size();
        List<Thread> threads = new ArrayList<>();
        for (var fetchTask : fetchTaskList) {
            Thread thread = new Thread(() -> {
                fetchTask.run();
                int done = doneCount.incrementAndGet();
                ShowProgress(done, totalTasks);
            });
            threads.add(thread);
            thread.start();

            // 控制最大并发线程数
            while (threads.size() >= MAX_THREADS) {
                threads.removeIf(t -> !t.isAlive());
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // 等待所有线程完成
        for (var thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted: " + e.getMessage());
            }
        }

        // 等待所有剩余线程完成
        for (var t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

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
