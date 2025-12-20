package Excel;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TestExcel {

    static final String TEST_EXCEL_PATH = "D:/OneDrive/2025秋-p.xlsx";

    static List<List<String>> data = new ArrayList<>(); // 保存读取的数据

    static XSSFWorkbook workbook; // Excel 工作簿
    static FormulaEvaluator evaluator;// 公式计算器

    static CellPosition cursor = new CellPosition(); // 光标位置
    static Map<String, String> variables = new HashMap<>(); // 定义的变量

    public static void printDataList() {
        for (var row : data) {
            System.out.print("Row: [");
            for (var cell : row) {
                System.out.print(cell + ", ");
            }
            System.out.println("]");
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("TestExcel");

        // 创建临时文件（系统自动放在临时目录）
        var temp_file = Files.createTempFile("Temp_", ".txt");
        Files.copy(Files.newInputStream(Path.of(TEST_EXCEL_PATH)), temp_file, StandardCopyOption.REPLACE_EXISTING);
        workbook = new XSSFWorkbook(new FileInputStream(temp_file.toFile()));
        evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        // 遍历所有行，保存数据
        var flag = true;
        while (flag) {
            if (isCursorOut())
                break; // 超出行数

            var dx = 0; // 游标列偏移量
            var row_data = new ArrayList<String>();
            while (true) {
                // 依次读取每一个单元格
                var cell = getCell(dx++);
                if (cell == null || cell.getCellType() == CellType.BLANK) {
                    cursor.gotoNextRow();
                    break; // 如果遇到空单元格，结束该行读取

                } else {
                    // 读取单元格数据
                    var cellData = GetCellValue(evaluator, cell);

                    // 特殊标记处理
                    if (cellData != null && cellData.startsWith("#")) {
                        if (cellData.equalsIgnoreCase("#end")) // 结束读取
                        {
                            flag = false;
                            System.out.println("#End of Data.");

                        } else if (cellData.equalsIgnoreCase("#goto")) // 跳转到指定位置
                        {
                            jump(0);
                        } else if (cellData.equalsIgnoreCase("#define")) // 定义变量
                        {
                            // 读取变量名和值（但不处理）
                            var var_name = GetCellValue(evaluator, getCell(1));
                            var var_value = GetCellValue(evaluator, getCell(2));
                            if (var_name != null) {
                                var_name = var_name.trim();
                                if (var_value != null)
                                    var_value = var_value.trim();
                                variables.put(var_name, var_value);
                                System.out.println("#Define Variable: " + var_name + " = " + var_value);
                            }

                            cursor.gotoNextRow();

                        } else if (cellData.equalsIgnoreCase("#goto_if")) // 条件跳转
                        {
                            var var_name = GetCellValue(evaluator, getCell(1));
                            // 如果存在则跳转
                            if (variables.containsKey(var_name))
                                jump(1);
                            else
                                cursor.gotoNextRow();
                        } else
                            cursor.gotoNextRow();

                        break;
                    }

                    // 保存单元格数据
                    row_data.add(cellData);
                }
            }
            if (row_data.size() != 0)
                data.add(row_data); // 保存该行数据

        }

        printDataList();

        // 打印定义的变量
        System.out.println("#Defined Variables:");
        for (var entry : variables.entrySet())
            System.out.println(entry.getKey() + ": " + entry.getValue());

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

    static void jump(int dx) {
        // 读取目标位置
        var target_row = GetCellValue(evaluator, getCell(dx + 1));
        var target_col = GetCellValue(evaluator, getCell(dx + 2));
        var r = (int) Double.parseDouble(target_row) - 1;
        var c = (int) Double.parseDouble(target_col) - 1;
        var newSheet = GetCellValue(evaluator, getCell(dx + 3));

        cursor.gotoPosition(r, c, newSheet);
        System.out.println("#Goto Position: (" + r + ", " + c + ") in Sheet: " + newSheet);
    }

    /** 判断是否越界 */
    static boolean isCursorOut() {

        // 保证工作表存在
        var sheet = workbook.getSheet(cursor.sheetName());
        if (sheet == null)
            return true;

        // 判断行是否越界
        if (cursor.row() > sheet.getLastRowNum())
            return true;

        // 保证行存在
        var row = sheet.getRow(cursor.row());
        if (row == null)
            return true;

        // 判断列是否越界
        if (cursor.col() > row.getLastCellNum())
            return true;

        return false;
    }

    static Cell getCell(int dx) {
        // 保证工作表存在
        var sheet = workbook.getSheet(cursor.sheetName());
        if (sheet == null)
            return null;

        // 判断行是否越界
        if (cursor.row() > sheet.getLastRowNum())
            return null;

        // 保证行存在
        var row = sheet.getRow(cursor.row());
        if (row == null)
            return null;

        // 判断列是否越界
        if (cursor.col() + dx > row.getLastCellNum())
            return null;

        return row.getCell(cursor.col() + dx);
    }
}
