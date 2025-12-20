package Excel;

import java.io.IOException;

import Main.TableToInfo;

public class TestExcel {

    static final String TEST_EXCEL_PATH = "D:/OneDrive/2025秋-p.xlsx";

    public static void main(String[] args) throws IOException {
        System.out.println("TestExcel");
        ExcelReader reader = new ExcelReader(TEST_EXCEL_PATH);
        reader.printVariables();
        reader.printCommands();
        reader.runCommands();
        reader.printBlocks();

        var res = TableToInfo.convertInfoTorFetch(reader.blockDataList.get(0));
        for (var info : res) {
            System.out.println(info);
        }
    }

}
