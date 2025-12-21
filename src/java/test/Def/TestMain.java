package Def;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Database.SQLiteAccess;
import Database.InfoItem.InfoItem;
import Excel.ExcelReader;
import Main.TableToInfo;
import MetaData.TestMetaData;

public class TestMain {
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

        List<InfoItem> infoItemList = new ArrayList<>();
        for (var blockData : excelReader.blockDataList) {
            if (blockData.block_name.equals("ani_store")) {
                infoItemList.addAll(TableToInfo.convertInfoAniStore(blockData));
            } else if (blockData.block_name.equals("epi_store")) {
                infoItemList.addAll(TableToInfo.convertInfoEpiStore(blockData));
            } else if (blockData.block_name.equals("tor_store")) {
                infoItemList.addAll(TableToInfo.convertInfoTorStore(blockData));
            }
        }

        // 输出 infoItemList 内容
        try (var writer = Files.newBufferedWriter(Path.of(TestMetaData.OUTPUT_FILE_1))) {
            for (var infoItem : infoItemList) {
                writer.write(infoItem.toString());
                writer.write("\n");
            }
        }

        // 保存到数据库
        try (var db = new SQLiteAccess(TestMetaData.DATABASE_PATH)) {
            db.Upsert(infoItemList);
        }
    }
}
