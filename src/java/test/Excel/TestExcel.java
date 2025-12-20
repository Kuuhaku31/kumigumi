package Excel;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TestExcel {

    static final String TEST_EXCEL_PATH = "D:/OneDrive/2025秋-p.xlsx";

    static List<List<String>> data = new ArrayList<>();

    public static void printDataList() {
        for (var row : data) {
            System.out.print("Row: [");
            for (var cell : row) {
                System.out.print(cell + ", ");
            }
            System.out.println("]");
        }
    }

    @SuppressWarnings("resource")
    public static void main(String[] args) throws IOException {
        System.out.println("TestExcel");

        // 创建临时文件（系统自动放在临时目录）
        var temp_file = Files.createTempFile("Temp_", ".txt");
        Files.copy(Files.newInputStream(Path.of(TEST_EXCEL_PATH)), temp_file, StandardCopyOption.REPLACE_EXISTING);
        var workbook = new XSSFWorkbook(new FileInputStream(temp_file.toFile()));
        var evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        var targetSheet = workbook.getSheet("main");

        // 遍历所有行，保存数据

        // var sheetSize = new CellPosition(main_sheet.getLastRowNum(),
        // main_sheet.getRow(0).getLastCellNum());
        var cursor = new CellPosition();

        var flag = true;
        while (flag) {
            if (cursor.row() > targetSheet.getLastRowNum())
                break; // 超出行数

            var row = targetSheet.getRow(cursor.row());
            if (row == null) {
                cursor.gotoNextRow();
                continue; // 空行
            }

            var row_data = new ArrayList<String>();
            var isJumped = false;
            for (var col = cursor.col(); col < row.getLastCellNum(); col++) {
                // 保证不越界
                // 依次读取每一个单元格
                var cell = row.getCell(col);
                if (cell == null || cell.getCellType() == CellType.BLANK)
                    break; // 如果遇到空单元格，结束该行读取
                else {
                    // 读取单元格数据
                    var cellData = GetCellValue(evaluator, cell);

                    // 特殊标记处理
                    if (cellData != null && cellData.startsWith("#")) {
                        if (cellData.equalsIgnoreCase("#end")) // 结束读取
                        {
                            flag = false;

                        } else if (cellData.equalsIgnoreCase("#goto")) // 跳转到指定位置
                        {
                            // 读取目标位置
                            var target_row = GetCellValue(evaluator, row.getCell(col + 1));
                            var target_col = GetCellValue(evaluator, row.getCell(col + 2));
                            var r = (int) Double.parseDouble(target_row) - 1;
                            var c = (int) Double.parseDouble(target_col) - 1;
                            cursor.gotoPosition(r, c);

                            // 可选：跳转到指定工作表
                            var newSheet = GetCellValue(evaluator, row.getCell(col + 3));
                            var sheet = workbook.getSheet(newSheet);
                            if (sheet != null)
                                targetSheet = sheet;

                            System.out.println("Goto Position: (" + r + ", " + c + ") in Sheet: " + newSheet);
                            isJumped = true;
                        }

                        break;
                    }

                    // 保存单元格数据
                    row_data.add(cellData);
                }
            }
            if (row_data.size() != 0)
                data.add(row_data); // 保存该行数据

            if (isJumped == false)
                cursor.gotoNextRow();
        }

        printDataList();

    }

    static String GetCellValue(FormulaEvaluator evaluator, Cell cell) {

        // 处理空单元格
        var value = evaluator.evaluate(cell);
        if (value == null)
            return null;

        return switch (value.getCellType()) {
            case BOOLEAN -> value.getBooleanValue() ? "1" : "0";
            case NUMERIC -> Double.toString(value.getNumberValue());
            case STRING -> value.getStringValue();
            default -> null;
        };
    }
}
