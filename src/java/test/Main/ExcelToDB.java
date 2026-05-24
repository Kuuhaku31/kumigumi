package Main;

import java.io.IOException;

import Database.RSSInfo;

public class ExcelToDB {
    /**
     * 从Excel文件导入数据到数据库
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        System.out.println("正在从Excel导入数据到数据库...");

        var e_reader = new Excel.ExcelReader();
        var res =  e_reader.Read("./db/test_exc.xlsx");

        var blockDataList = res.blockDataList();
        for(var entry : blockDataList.entrySet()) {
            System.out.println("Block Name: " + entry.getKey());
            System.out.println("Block Data:\n" + entry.getValue().toString());
        }

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
    }
}
