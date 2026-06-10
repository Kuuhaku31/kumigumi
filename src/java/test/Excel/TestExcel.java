package Excel;

import java.io.IOException;
import java.sql.SQLException;

import Database.RSSInfo;
import Database.SQLiteAccess;

public class TestExcel {
    /**
     * 从Excel文件导入数据到数据库
     * @param args
     * @throws IOException
     * @throws SQLException
     */
    public static void main(String[] args) throws IOException, SQLException {
        System.out.println("TestExcel...");

        var e_reader = new Excel.ExcelReader();
        var res      = e_reader.Read("./db/test_exc.xlsx");

        System.out.println("从Excel读取的数据:");
        var blockDataList = res.blockDataList();
        for(var entry : blockDataList.entrySet()) {
            System.out.println("Block Name: " + entry.getKey());
            System.out.println("Block Data:\n" + entry.getValue().toString());
        }

        System.out.println("从Excel创建的BlockData实例:");

        var epi_recode_info_set = Database.EpisodeRecordInfo.ParseEpisodeRecordInfoByTableData(blockDataList.get("BlockEpisode"));
        System.out.println("从Excel创建的EpisodeRecordInfo实例:");
        for(var epi_record_info : epi_recode_info_set) {
            System.out.println(epi_record_info.toString());
        }

        var rss_info_set = RSSInfo.ParseRSSInfoByTableData(blockDataList.get("BlockRSS"));
        System.out.println("从Excel创建的RSSInfo实例:");
        for(var rss_info : rss_info_set) {
            System.out.println(rss_info.toString());
        }

        // 测试导入数据库
        try(var db = new SQLiteAccess("db/test.db")) {
            for(var epi_record_info : epi_recode_info_set) {
                db.UpsertEpisodeRecordInfo(epi_record_info);
            }
            for(var rss_info : rss_info_set) {
                db.UpsertRSSInfo(rss_info);
            }
        }
    }
}
