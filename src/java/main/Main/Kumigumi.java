package Main;// Main.Kumigumi.java

import Database.DBStructure.Headers;
import Database.KG_SQLiteAccess;
import Database.KG_SQLiteAccess.TableName;
import Database.UpsertException;
import Excel.ExcelReader;
import Task.*;
import util.DataBuffer;
import util.TableData.TableData;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static util.Util.SaveDataList;

public class Kumigumi {
    private final DataBuffer data_buffer = new DataBuffer();

    // 读取 Excel
    public void ReadExcel(Path excel_path) {
        // 先一次性读取 Excel 全部数据块，并分类
        var info = ExcelReader.Read(excel_path);
        if (info == null)
            return;

        List<String> ani_store_tables = new ArrayList<>();
        List<String> epi_store_tables = new ArrayList<>();
        List<String> tor_store_tables = new ArrayList<>();

        List<String> fetch_tables = new ArrayList<>();
        List<String> dt_tables = new ArrayList<>();

        // 解析命令
        for (var op : info.operations()) {
            var command = op.command();
            var params = op.params();

            switch (command) {
                case "-store-ani" -> Collections.addAll(ani_store_tables, params);
                case "-store-epi" -> Collections.addAll(epi_store_tables, params);
                case "-store-tor" -> Collections.addAll(tor_store_tables, params);

                case "-fetch" -> Collections.addAll(fetch_tables, params);
                case "-dt" -> Collections.addAll(dt_tables, params);
            }
        }

        // 分类数据块
        for (var block : info.data()) {
            if (ani_store_tables.contains(block.block_name))
                data_buffer.table_update_ani.add(block);
            if (epi_store_tables.contains(block.block_name))
                data_buffer.table_update_epi.add(block);
            if (tor_store_tables.contains(block.block_name))
                data_buffer.table_update_tor.add(block);

            if (fetch_tables.contains(block.block_name))
                data_buffer.table_fetch.add(block);
            if (dt_tables.contains(block.block_name))
                data_buffer.table_dt.add(block);
        }

        data_buffer.printExcelInfo();
    }

    // 将 Excel 的数据块解析成 fetch 任务列表
    public List<TaskManager.Task> getFetchTask() {
        List<TaskManager.Task> res = new ArrayList<>();
        for (var table : data_buffer.table_fetch) {
            // 获取对应列号
            var i_ani_id = table.GetHeaderIndex("ANI_ID");
            var i_rss_url = table.GetHeaderIndex("url_rss");

            // 生成不同任务
            if (i_ani_id >= 0) {
                for (var data_row : table.GetData()) {
                    res.add(new TaskFetchAni((int) Double.parseDouble(data_row[i_ani_id])));
                    res.add(new TaskFetchEpi((int) Double.parseDouble(data_row[i_ani_id])));
                }
            }
            if (i_rss_url >= 0) {
                for (var data_row : table.GetData()) {
                    if (data_row[i_rss_url] != null) {
                        res.add(new TaskFetchTor((int) Double.parseDouble(data_row[i_ani_id]), data_row[i_rss_url]));
                    }
                }
            }
        }
        return res;
    }

    // 将 Excel 的数据块解析成种子下载任务列表
    public List<TaskManager.Task> getDTTask(Path dt_path, String state) {
        List<TaskManager.Task> res = new ArrayList<>();
        for (var table : data_buffer.table_dt) {
            // 获取列号
            var i_dt_url = table.GetHeaderIndex("TOR_URL");
            var i_t_state = table.GetHeaderIndex("status_download");
            if (i_dt_url < 0 || i_t_state < 0)
                continue;

            // 每个块的各个 TOR_URL : status_download
            for (var data_row : table.GetData()) {
                if (data_row[i_t_state] != null && data_row[i_t_state].equals(state))
                    res.add(new TaskDT(
                            dt_path,
                            data_row[i_dt_url]));
            }
        }
        return res;
    }

    // 获取任务运行结果
    public void addTaskRes(List<TaskManager.Task> tasks) {
        for (var task : tasks) {
            switch (task) {
                case TaskFetchAni t -> data_buffer.anime_fetch.addAll(t.getBuffer());
                case TaskFetchEpi t -> data_buffer.episode_fetch.addAll(t.getBuffer());
                case TaskFetchTor t -> data_buffer.torrent_fetch.addAll(t.getBuffer());
                case TaskDT _ -> {
                }
                case null, default -> System.err.println("出现了非法任务: " + task);
            }
        }
    }

    public void toDatabase() {
        String[] ANIME_HEADERS_FETCH = {
                Headers.ANI_ID.toString(),
                Headers.air_date.toString(),
                Headers.title.toString(),
                Headers.title_cn.toString(),
                Headers.aliases.toString(),
                Headers.description.toString(),
                Headers.episode_count.toString(),
                Headers.url_official_site.toString(),
                Headers.url_cover.toString(),
        };

        String[] EPISODE_HEADERS_FETCH = {
                Headers.EPI_ID.toString(),
                Headers.ANI_ID.toString(),
                Headers.sort.toString(),
                Headers.air_date.toString(),
                Headers.duration.toString(),
                Headers.ep.toString(),
                Headers.title.toString(),
                Headers.title_cn.toString(),
                Headers.description.toString(),
        };

        String[] TORRENT_HEADERS_FETCH = {
                Headers.TOR_URL.toString(),
                Headers.ANI_ID.toString(),
                Headers.air_datetime.toString(),
                Headers.size.toString(),
                Headers.url_page.toString(),
                Headers.title.toString(),
                Headers.subtitle_group.toString(),
                Headers.description.toString(),
        };

        var ani_upsert = new TableData(ANIME_HEADERS_FETCH);
        var epi_upsert = new TableData(EPISODE_HEADERS_FETCH);
        var tor_upsert = new TableData(TORRENT_HEADERS_FETCH);

        // 遍历每个 map，生成记录
        for (var map : data_buffer.anime_fetch) {
            var recode = ani_upsert.new Record();
            for (var header : ANIME_HEADERS_FETCH) {
                if (map.containsKey(header)) {
                    recode.Set(header, map.get(header));
                }
            }
        }

        for (var map : data_buffer.episode_fetch) {
            var recode = epi_upsert.new Record();
            for (var header : EPISODE_HEADERS_FETCH) {
                if (map.containsKey(header)) {
                    recode.Set(header, map.get(header));
                }
            }
        }

        // todo: 检测合法性
        for (var map : data_buffer.torrent_fetch) {
            var recode = tor_upsert.new Record();
            for (var header : TORRENT_HEADERS_FETCH) {
                if (map.containsKey(header)) {
                    recode.Set(header, map.get(header));
                }
            }
        }

        KG_SQLiteAccess.Open();

        // todo: 优化
        try {
            // 提交任务
            KG_SQLiteAccess.Upsert(TableName.anime, ani_upsert);
            KG_SQLiteAccess.Upsert(TableName.episode, epi_upsert);
            KG_SQLiteAccess.Upsert(TableName.torrent, tor_upsert);

            for (var table : data_buffer.table_update_ani)
                KG_SQLiteAccess.Upsert(TableName.anime, table);
            for (var table : data_buffer.table_update_epi)
                KG_SQLiteAccess.Upsert(TableName.episode, table);
            for (var table : data_buffer.table_update_tor)
                KG_SQLiteAccess.Upsert(TableName.torrent, table);
        } catch (SQLException | UpsertException e) {
            System.err.println(e.getMessage());
        }

        KG_SQLiteAccess.Close();
    }

    // 将所有表数据保存到日志文件
    public void SaveLog() {
        List<TableData> list = new ArrayList<>();
        // list.addAll(block_list_fetch);
        // list.addAll(block_list_dt);
        //
        // list.addAll(anime_block_list_store);
        // list.addAll(episode_block_list_store);
        // list.addAll(torrent_block_list_store);

        SaveDataList(list);
    }

    // 将参数解析成任务列表
    public void addTaskFromArgs(String[] args) {
        // for(int i = 0; i < args.length; i++)
        // {
        // if(args[i].startsWith("-a")) // 如果开头两个字符是 "-a"
        // {
        // // 获取 anime_id
        // var ani_id = Integer.parseInt(args[i].substring(2));
        //
        // // 继续判断下一个参数开头是不是 "-r"
        // String rss_link_str = null;
        // if(i + 1 < args.length && args[i + 1].startsWith("-r")) { rss_link_str =
        // args[i + 1].substring(2); }
        //
        // // 添加不同类型任务
        // new TaskFetchAni(anime_fetch, ani_id);
        // new TaskFetchEpi(episode_fetch, ani_id);
        // if(rss_link_str != null) new TaskFetchTor(torrent_fetch, ani_id,
        // rss_link_str);
        // }
        // }
    }
}