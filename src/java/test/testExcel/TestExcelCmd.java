package testExcel;

import java.io.IOException;

import Excel.ExcelReader;
import MetaData.TestMetaData;

public class TestExcelCmd {
    public static void main(String[] args) throws IOException {
        System.out.println("TestExcelCmd");

        var excelReader = new ExcelReader(TestMetaData.EXCEL_FILE_KG_N_PATH);

        var cmds = excelReader.getCommandsInfo();
        System.out.print(cmds);

    }

}
