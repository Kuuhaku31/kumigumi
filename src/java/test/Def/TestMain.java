package Def;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Database.SQLiteAccess;
import Database.InfoItem.UpdateItem;
import Database.InfoItem.UpsertItem;
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

        List<UpsertItem> upsertList = new ArrayList<>();
        List<UpdateItem> updateList = new ArrayList<>();

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

        // 保存到数据库
        try (var db = new SQLiteAccess(TestMetaData.DATABASE_PATH)) {
            db.Upsert(upsertList);
            db.Update(updateList);
        }
    }
}
