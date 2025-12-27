package TestMain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Database.InfoItem.UpdateItem;
import Database.InfoItem.UpsertItem;
import FetchTask.*;
import Main.ItemTranslation;
import MetaData.TestMetaData;

public class TestMain {

    public void main(String[] args) throws IOException, SQLException {
        System.out.println("TestMain");
        // test0();
        // test1();
        // test2();
        // test3(); // 保存 kg-n 的 2510 epi_store 数据到数据库
        // test4(); // 获取 2510 数据
        test5(); // store tor
    }

    // 获取 tor 数据
    static void test5() throws IOException, SQLException {
        System.out.println("test5");

        // 读取表格
        var blockDataList = TestMainUtils.ReadExcel(TestMetaData.EXCEL_FILE_KG_N_PATH);

        // 处理各个 blockData
        List<UpdateItem> updateItemList = new ArrayList<>();
        for (var blockData : blockDataList) {
            if (blockData.block_name.equals("store_tor")) {
                TestMainFunc.storeTor(updateItemList, blockData);
            } else {
                System.out.println("Unknown block name: " + blockData.block_name);
            }
        }

        // 保存到数据库
        TestMainUtils.ToDatabase(null, updateItemList, TestMetaData.DATABASE_PATH);
    }

    // 获取 2510 数据
    static void test4() throws IOException, SQLException {
        System.out.println("test4");

        // 读取表格
        var blockDataList = TestMainUtils.ReadExcel(TestMetaData.EXCEL_FILE_KG_N_PATH);

        // 处理各个 blockData
        List<UpsertItem> upsertBuffer = new ArrayList<>();
        List<UpdateItem> fetchBuffer = new ArrayList<>();
        List<FetchTask> fetchTaskList = new ArrayList<>();
        for (var blockData : blockDataList) {
            if (blockData.block_name.equals("fetch2510ani")) {
                TestMainFunc.fetch2510ani(upsertBuffer, fetchBuffer, fetchTaskList, blockData);
            } else {
                System.out.println("Unknown block name: " + blockData.block_name);
            }
        }

        // 批量运行获取任务
        TestMainUtils.RunFetchTasks(fetchTaskList);

        // 输出获取的 map 内容
        try (var writer = Files.newBufferedWriter(Path.of(TestMetaData.OUTPUT_FETCH_MAP))) {
            writer.write(util.Logger.log);
        }

        // 输出任务获取的内容
        TestMainUtils.WriteItemListToFile(upsertBuffer, TestMetaData.OUTPUT_UPSERT_TASK_ITEM);

        // 保存到数据库
        TestMainUtils.ToDatabase(upsertBuffer, fetchBuffer, TestMetaData.DATABASE_PATH);
    }

    // 保存 kg-n 的 2510 epi_store 数据到数据库
    static void test3() throws IOException, SQLException {
        System.out.println("test3");

        // 读取表格
        var blockDataList = TestMainUtils.ReadExcel(TestMetaData.EXCEL_FILE_KG_N_PATH);

        // 处理各个 blockData
        List<UpdateItem> updateList = new ArrayList<>();
        for (var blockData : blockDataList) {
            if (blockData.block_name.equals("store2510epi"))
                TestMainFunc.store2510epi(updateList, blockData);
            else {
                System.out.println("Unknown block name: " + blockData.block_name);
            }
        }

        // 打印 updateList 内容
        TestMainUtils.WriteItemListToFile(updateList, TestMetaData.OUTPUT_INFO_ITEMS);

        // 保存到数据库
        TestMainUtils.ToDatabase(null, updateList, TestMetaData.DATABASE_PATH);
    }

    // 处理 2026-01 表格
    static void test2() throws IOException, SQLException {
        System.out.println("test2");

        // 读取表格
        var blockDataList = TestMainUtils.ReadExcel(TestMetaData.EXCEL_FILE_KG_N_PATH);

        // 处理各个 blockData
        List<UpsertItem> upsertBuffer = new ArrayList<>(); // 获取的需要插入或者更新的信息
        List<UpdateItem> fetchBuffer = new ArrayList<>(); // 获取的需要更新的信息
        List<FetchTask> taskList = new ArrayList<>(); // 任务列表
        for (var blockData : blockDataList) {
            if (blockData.block_name.equals("ani_2601"))
                taskList.addAll(TestMainUtils.buildFetchTasks(upsertBuffer, fetchBuffer, blockData));
            else
                System.out.println("Unknown block name: " + blockData.block_name);
        }

        // 批量运行获取任务
        TestMainUtils.RunFetchTasks(taskList);

        // 输出任务获取的内容
        List<Object> allInfoItems = new ArrayList<>();
        allInfoItems.addAll(upsertBuffer);
        allInfoItems.addAll(fetchBuffer);
        TestMainUtils.WriteItemListToFile(allInfoItems, TestMetaData.OUTPUT_INFO_ITEM);

        // 插入数据库
        TestMainUtils.ToDatabase(upsertBuffer, fetchBuffer, TestMetaData.DATABASE_PATH);
    }

    static void test1() throws IOException, SQLException {
        System.out.println("test1");

        // 读取表格
        var blockDataList = TestMainUtils.ReadExcel(TestMetaData.EXCEL_FILE_KG_PATH);

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
        TestMainUtils.WriteItemListToFile(updateList, TestMetaData.OUTPUT_INFO_ITEMS);

        // 保存到数据库
        TestMainUtils.ToDatabase(null, updateList, TestMetaData.DATABASE_PATH);
    }

    static void test0() throws IOException, SQLException {
        System.out.println("test0");

        var blockDataList = TestMainUtils.ReadExcel(TestMetaData.EXCEL_FILE_KG_N_PATH);

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
                fetchTaskList.addAll(TestMainUtils.buildFetchTasks(bufferUpsertFetch, bufferUpdateFetch, blockData));
            } else {
                System.out.println("Unknown block name: " + blockData.block_name);
            }
        }

        if (fetchTaskList.isEmpty())
            System.out.println("No fetch tasks to run.");
        else
            TestMainUtils.RunFetchTasks(fetchTaskList);

        // 输出 infoItemList 内容
        List<Object> allInfoItems = new ArrayList<>();
        allInfoItems.addAll(upsertListStore);
        allInfoItems.addAll(updateListStore);
        allInfoItems.addAll(bufferUpsertFetch);
        allInfoItems.addAll(bufferUpdateFetch);
        TestMainUtils.WriteItemListToFile(allInfoItems, TestMetaData.OUTPUT_INFO_ITEM);

        // 保存到数据库
        List<UpdateItem> combinedUpdateList = new ArrayList<>(updateListStore);
        combinedUpdateList.addAll(bufferUpdateFetch);
        TestMainUtils.ToDatabase(upsertListStore, combinedUpdateList, TestMetaData.DATABASE_PATH);
    }

}
