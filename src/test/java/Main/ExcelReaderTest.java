package Main;

import Excel.ExcelReader;
import Utils.ColorCode;

import static Utils.UtilityFunctions.color;


class ExcelReaderTest {
    public static void main(String[] args) {
        System.out.println(color("Testing ExcelReader with file 'db/test_exc.xlsx'...", ColorCode.BOLD_BLUE));

        var excelFilePath = "db/test_exc.xlsx";

        // 测试读取 Excel 文件
        try {
            System.out.println(color("Reading excel file...", ColorCode.GREEN));
            var result = new ExcelReader().Read(excelFilePath);

            System.out.println("\n" + color("ExcelReader Result:", ColorCode.BOLD_BLUE));
            System.out.println(result.toPrintString("xxx"));
            System.out.println();

        } catch(Exception e) {
            System.out.println(color("Failed to read Excel file: " + e.getMessage(), ColorCode.RED));
        }

        System.out.println(color("ExcelReader test completed.", ColorCode.BOLD_BLUE));
    }
}
