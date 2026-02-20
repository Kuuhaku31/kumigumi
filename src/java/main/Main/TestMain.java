package Main;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Database.Item.DatabaseItem;
import Database.Item.UpdateItem;
import Database.Item.UpsertItem;
import Excel.BlockData;
import FetchTask.*;
import MetaData.TestMetaData;

public class TestMain {

    public void main(String[] args) throws IOException, SQLException {
        System.out.println("TestMain");
        // test0();
        // test1();
        // test2();
        // test3(); // 保存 kg-n 的 2510 epi_store 数据到数据库
        // test4(); // 获取 2510 数据
        // test5(); // store tor
        testMain();
    }

    static void testMain() throws IOException {
        System.out.println("testMain()");

        List<BlockData> blockDataList = MainUtils.ReadExcel(TestMetaData.EXCEL_FILE_KG_N_PATH);
        List<DatabaseItem> dbItems = new ArrayList<>();

        // 构建任务
        List<UpsertItem> bufferUpsert = new ArrayList<>();
        List<UpdateItem> bufferUpdate = new ArrayList<>();
        List<FetchTask> fetchTaskList = new ArrayList<>();

        // CommandParser
        for (var blockData : blockDataList) {
            if (blockData.block_name.equals("store2510ani")) {
                dbItems.addAll(ItemTranslation.convertInfoAniUpsert(blockData));
                dbItems.addAll(ItemTranslation.convertInfoAniStore(blockData));
            } else if (blockData.block_name.equals("store2510epi")) {
                dbItems.addAll(ItemTranslation.convertInfoEpiUpsert(blockData));
                dbItems.addAll(ItemTranslation.convertInfoEpiStore(blockData));
            } else if (blockData.block_name.equals("store_tor")) {
                dbItems.addAll(ItemTranslation.convertInfoTorUpsert(blockData));
                dbItems.addAll(ItemTranslation.convertInfoTorStore(blockData));
            } else if (blockData.block_name.equals("fetch2510")) {
                fetchTaskList.addAll(MainUtils.BuildFetchTasks(bufferUpsert, bufferUpdate, blockData));
            } else {
                System.out.println("Unknown block name: " + blockData.block_name);
            }
        }

        MainUtils.RunFetchTasks(fetchTaskList);

        dbItems.addAll(bufferUpsert);
        dbItems.addAll(bufferUpdate);

        MainUtils.WriteItemListToFile(dbItems, TestMetaData.OUTPUT_INFO_ITEM);

        MainUtils.ToDatabase(dbItems, TestMetaData.DATABASE_PATH);
    }

    // 获取 tor 数据
    static void test5() throws IOException, SQLException {
        System.out.println("test5");

        List<BlockData> 表格数据块列表;
        List<UpdateItem> 更新项目列表;

        表格数据块列表 = MainUtils.ReadExcel(TestMetaData.EXCEL_FILE_KG_N_PATH); // 读取表格
        更新项目列表 = new ArrayList<>();

        // 处理各个 blockData
        for (var blockData : 表格数据块列表) {
            if (blockData.block_name.equals("store_tor")) {
                MainFunc.storeTor(更新项目列表, blockData);
            } else {
                System.out.println("Unknown block name: " + blockData.block_name);
            }
        }
        // 保存到数据库
        MainUtils.ToDatabase(更新项目列表, TestMetaData.DATABASE_PATH);
    }

    // 获取 2510 数据
    static void test4() throws IOException, SQLException {
        System.out.println("test4");

        List<BlockData> 表格数据块列表 = MainUtils.ReadExcel(TestMetaData.EXCEL_FILE_KG_N_PATH);

        List<UpsertItem> 插更缓存 = new ArrayList<>();
        List<UpdateItem> 获取缓存 = new ArrayList<>();
        List<FetchTask> 任务列表 = new ArrayList<>();

        // 处理各个 blockData
        for (var blockData : 表格数据块列表) {
            if (blockData.block_name.equals("fetch2510ani")) {
                MainFunc.fetch2510ani(插更缓存, 获取缓存, 任务列表, blockData);
            } else {
                System.out.println("Unknown block name: " + blockData.block_name);
            }
        }

        // 批量运行获取任务
        MainUtils.RunFetchTasks(任务列表);

        // 输出任务获取的内容
        MainUtils.WriteItemListToFile(插更缓存, TestMetaData.OUTPUT_UPSERT_TASK_ITEM);

        // 保存到数据库
        List<DatabaseItem> allItems = new ArrayList<>();
        allItems.addAll(插更缓存);
        allItems.addAll(获取缓存);
        MainUtils.ToDatabase(allItems, TestMetaData.DATABASE_PATH);
    }

    // 保存 kg-n 的 2510 epi_store 数据到数据库
    static void test3() throws IOException, SQLException {
        System.out.println("test3");

        List<BlockData> 表格数据块列表 = MainUtils.ReadExcel(TestMetaData.EXCEL_FILE_KG_N_PATH);

        // 处理各个 blockData
        List<UpdateItem> 更新项目列表 = new ArrayList<>();
        for (var blockData : 表格数据块列表) {
            if (blockData.block_name.equals("store2510epi"))
                MainFunc.store2510epi(更新项目列表, blockData);
            else {
                System.out.println("Unknown block name: " + blockData.block_name);
            }
        }

        // 打印 更新项目列表 内容
        MainUtils.WriteItemListToFile(更新项目列表, TestMetaData.OUTPUT_INFO_ITEMS);

        // 保存到数据库
        MainUtils.ToDatabase(更新项目列表, TestMetaData.DATABASE_PATH);
    }

    // 处理 2026-01 表格
    static void test2() throws IOException, SQLException {
        System.out.println("test2");

        List<BlockData> 表格数据块列表 = MainUtils.ReadExcel(TestMetaData.EXCEL_FILE_KG_N_PATH);

        // 处理各个 blockData
        List<UpsertItem> upsertBuffer = new ArrayList<>(); // 获取的需要插入或者更新的信息
        List<UpdateItem> fetchBuffer = new ArrayList<>(); // 获取的需要更新的信息
        List<FetchTask> taskList = new ArrayList<>(); // 任务列表
        for (var blockData : 表格数据块列表) {
            if (blockData.block_name.equals("ani_2601"))
                taskList.addAll(MainUtils.BuildFetchTasks(upsertBuffer, fetchBuffer, blockData));
            else
                System.out.println("Unknown block name: " + blockData.block_name);
        }

        // 批量运行获取任务
        MainUtils.RunFetchTasks(taskList);

        // 输出任务获取的内容
        List<DatabaseItem> dbItems = new ArrayList<>();
        dbItems.addAll(upsertBuffer);
        dbItems.addAll(fetchBuffer);
        MainUtils.WriteItemListToFile(dbItems, TestMetaData.OUTPUT_INFO_ITEM);

        // 插入数据库
        MainUtils.ToDatabase(dbItems, TestMetaData.DATABASE_PATH);
    }

    static void test1() throws IOException, SQLException {
        System.out.println("test1");

        // 读取表格
        var blockDataList = MainUtils.ReadExcel(TestMetaData.EXCEL_FILE_KG_PATH);

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
        MainUtils.WriteItemListToFile(updateList, TestMetaData.OUTPUT_INFO_ITEMS);

        // 保存到数据库
        MainUtils.ToDatabase(updateList, TestMetaData.DATABASE_PATH);
    }

    static void test0() throws IOException, SQLException {
        System.out.println("test0");

        List<BlockData> 表格数据块列表 = MainUtils.ReadExcel(TestMetaData.EXCEL_FILE_KG_N_PATH);

        // 处理各个 blockData
        List<UpsertItem> upsertListStore = new ArrayList<>();
        List<UpdateItem> updateListStore = new ArrayList<>();

        List<UpsertItem> bufferUpsertFetch = new ArrayList<>();
        List<UpdateItem> bufferUpdateFetch = new ArrayList<>();
        List<FetchTask> fetchTaskList = new ArrayList<>();

        for (var blockData : 表格数据块列表) {
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
                fetchTaskList.addAll(MainUtils.BuildFetchTasks(bufferUpsertFetch, bufferUpdateFetch, blockData));
            } else {
                System.out.println("Unknown block name: " + blockData.block_name);
            }
        }

        if (fetchTaskList.isEmpty())
            System.out.println("No fetch tasks to run.");
        else
            MainUtils.RunFetchTasks(fetchTaskList);

        // 输出 infoItemList 内容
        List<DatabaseItem> dbItems = new ArrayList<>();
        dbItems.addAll(upsertListStore);
        dbItems.addAll(updateListStore);
        dbItems.addAll(bufferUpsertFetch);
        dbItems.addAll(bufferUpdateFetch);
        MainUtils.WriteItemListToFile(dbItems, TestMetaData.OUTPUT_INFO_ITEM);

        // 保存到数据库
        MainUtils.ToDatabase(dbItems, TestMetaData.DATABASE_PATH);
    }

}
