package Excel;

import java.io.IOException;

public class TestExcel {

    static final String TEST_EXCEL_PATH = "D:/OneDrive/2025秋-p.xlsx";

    public static void main(String[] args) throws IOException {
        System.out.println("TestExcel");
        ExcelReader1 reader = new ExcelReader1(TEST_EXCEL_PATH);
        reader.read();
        reader.printVariables();
        reader.printDataList();
        reader.parseCommands();
        reader.printBlocks();
    }

}
