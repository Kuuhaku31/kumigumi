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

public class ExcelReader1 {
    private XSSFWorkbook workbook; // Excel 工作簿
    private FormulaEvaluator evaluator;// 公式计算器

    private CellPosition cursor = new CellPosition(); // 光标位置
    private Map<String, String> variables = new HashMap<>(); // 定义的变量
    private List<List<String>> data = new ArrayList<>(); // 保存读取的数据

    private boolean isReading = false; // 是否读取中

    ExcelReader1(String filePath) throws IOException {
        // 创建临时文件（系统自动放在临时目录）
        var temp_file = Files.createTempFile("Temp_", ".txt");
        System.out.println("ExcelReader: Copying file to temp file: " + temp_file.toAbsolutePath().toString());
        Files.copy(Files.newInputStream(Path.of(filePath)), temp_file, StandardCopyOption.REPLACE_EXISTING);
        workbook = new XSSFWorkbook(new FileInputStream(temp_file.toFile()));
        evaluator = workbook.getCreationHelper().createFormulaEvaluator();
    }

    void read() {
        // 遍历所有行，保存数据
        isReading = true;
        while (isReading) {
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
                    var cellData = GetCellValue(evaluator, cell); // 读取单元格数据
                    特殊标记处理(cellData);
                    row_data.add(cellData); // 保存单元格数据
                }
            }
            if (row_data.size() != 0)
                data.add(row_data); // 保存该行数据
        }
    }

    void printDataList() {
        System.out.println("#Data List:");
        for (var row : data) {
            System.out.print("\tRow: [");
            for (var cell : row) {
                System.out.print(cell + ", ");
            }
            System.out.println("]");
        }
    }

    void printVariables() {
        // 打印定义的变量
        System.out.println("#Defined Variables:");
        for (var entry : variables.entrySet())
            System.out.println("\t" + entry.getKey() + ": " + entry.getValue());
    }

    private void 特殊标记处理(String cellData) {
        if (cellData != null && cellData.startsWith("#")) {
            if (cellData.equalsIgnoreCase("#end")) // 结束读取
            {
                isReading = false;
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
        }
    }

    private String GetCellValue(FormulaEvaluator evaluator, Cell cell) {

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

    private void jump(int dx) {
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
    private boolean isCursorOut() {

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

    private Cell getCell(int dx) {
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

    void parseCommands() {

        List<BlockMetaData> blockMetaList = new ArrayList<>();

        var it = data.iterator();
        while (it.hasNext()) {
            var row = it.next();
            if (row.get(0).equals("_block")) {
                var blockMeta = new BlockMetaData(row.get(1));
                // 读取列信息
                while (it.hasNext()) {
                    row = it.next();
                    var fist = row.get(0);
                    if (fist.equals("_block_end"))
                        break;
                    else if (fist.equals("_from")) {
                        blockMeta.startRow = (int) Double.parseDouble(row.get(1));
                    } else if (fist.equals("_to")) {
                        blockMeta.endRow = (int) Double.parseDouble(row.get(1));
                    } else if (fist.equals("_sheet")) {
                        blockMeta.sheetName = row.get(1);
                    } else {
                        // 添加列
                        var header = row.get(0);
                        var colIdx = (int) Double.parseDouble(row.get(1));
                        var type = row.size() >= 3 ? row.get(2) : null;
                        blockMeta.addColumn(header, type, colIdx);
                    }
                }
                blockMetaList.add(blockMeta);
                System.out.println("Parsed Block: " + blockMeta);
            }
        }
    }

}