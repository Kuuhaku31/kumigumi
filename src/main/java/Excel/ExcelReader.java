package Excel;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Utils.ColorCode;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static Utils.UtilityFunctions.color;


public class ExcelReader {

    private ExcelReader() {} // 私有构造函数，禁止实例化

    /**
     * 读取 Excel 文件并解析数据
     * @param filePath Excel 文件路径
     * @throws IOException 如果文件读取失败
     */
    public static ExcelResult Read(String filePath) throws IOException {

        // 创建临时文件（系统自动放在临时目录）
        var temp_file = Files.createTempFile("Temp_", ".xlsx");
        try {
            var source_path = Path.of(filePath);
            if(!Files.exists(source_path)) {
                throw new IOException("Excel file not found: " + filePath);
            }

            // 复制 Excel 文件到临时文件
            try(var is = Files.newInputStream(source_path)) {
                Files.copy(is, temp_file, StandardCopyOption.REPLACE_EXISTING);
            }
            var msg = "ExcelReader: 复制文件到: " + temp_file.toAbsolutePath().toString();
            System.out.println(color(msg, ColorCode.GREEN));

            // 创建工作簿，一次性读取全部 sheet 数据
            try(
                var fis = new FileInputStream(temp_file.toFile());
                var workbook = new XSSFWorkbook(fis)
            ) {
                var excel_data = read_excel_data(workbook);
                var context = new ExcelReadContext(excel_data);
                return context.parse();
            }
        }
        finally {
            // 删除临时文件
            try { Files.deleteIfExists(temp_file); }
            catch(Exception e) {
                var msg = "Failed to delete temporary file: " + e.getMessage();
                System.out.println(color(msg, ColorCode.RED));
            }
        }
    }

    private static Map<String, List<List<String>>> read_excel_data(XSSFWorkbook workbook) {

        var evaluator = workbook.getCreationHelper().createFormulaEvaluator(); // 创建公式求值器

        // 读取所有 sheet 数据到 excel_data 中
        var excel_data = new LinkedHashMap<String, List<List<String>>>();
        for(var sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {

            // 读取该 sheet 的所有数据到 sheet_data 中
            var sheet = workbook.getSheetAt(sheetIndex);
            var sheet_data = new ArrayList<List<String>>();
            for(var rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {

                // 读取该行数据，如果该行不存在，则添加一个空行
                var row = sheet.getRow(rowIndex);
                if(row == null) {
                    sheet_data.add(new ArrayList<>());
                    continue; // 跳过空行
                }

                // 读取该行的所有单元格数据到 row_data 中
                var row_data = new ArrayList<String>();
                var last_cell_num = row.getLastCellNum();
                for(var col_index = 0; col_index < last_cell_num; col_index++)
                    row_data.add(get_cell_value(evaluator, row.getCell(col_index)));
                sheet_data.add(row_data);
            }
            excel_data.put(sheet.getSheetName(), sheet_data);
        }
        return excel_data;
    }

    private static String get_cell_value(FormulaEvaluator evaluator, Cell cell) {

        // 如果单元格不存在或为空白，则返回 null
        if(cell == null || cell.getCellType() == CellType.BLANK) return null;

        CellValue value = null;

        // 如果是公式单元格，则求值；否则直接获取值
        if(cell.getCellType() == CellType.FORMULA) {

            // 公式求值失败时，捕获异常并打印错误信息
            try { value = evaluator.evaluate(cell); }
            catch(Exception e) {
                var title = color("GetCellValue Error:", ColorCode.BOLD_RED);
                var msg = title + "\n  msg:  " + e.getMessage() + "\n  cell: " + cell;
                System.out.println(color(msg, ColorCode.RED));
            }

            // 如果求值结果为 null，则返回 null；否则根据求值结果的类型返回对应的字符串
            if(value == null) return null;
            else return switch(value.getCellType()) {
                case BOOLEAN -> value.getBooleanValue() ? "1" : "0";
                case NUMERIC -> Double.toString(value.getNumberValue());
                case STRING  -> value.getStringValue();
                default      -> null;
            };
        }

        // 如果不是公式单元格，则根据单元格类型返回对应的字符串
        else return switch(cell.getCellType()) {
            case BOOLEAN -> cell.getBooleanCellValue() ? "1" : "0";
            case NUMERIC -> Double.toString(cell.getNumericCellValue());
            case STRING  -> cell.getStringCellValue();
            default      -> null;
        };
    }

    private static final class ExcelReadContext {

        private Map<String, List<List<String>>> excelData; // sheet名 -> 二维字符串数据

        private ExcelCursor cursor; // 光标位置

        private Map<String, String>    variables;     // 定义的变量
        private List<List<String>>     commands;      // 保存命令列表
        private Map<String, TableData> blockDataList; // 保存块信息

        private boolean isReading = true;

        ExcelReadContext(Map<String, List<List<String>>> excelData) {

            this.excelData     = excelData;
            this.cursor        = new ExcelCursor(0, 0, "main");
            this.variables     = new HashMap<>();
            this.commands      = new ArrayList<>();
            this.blockDataList = new HashMap<>();
        }

        ExcelResult parse() throws IOException {

            // 遍历所有行，读取命令到 commands 列表
            while(isReading) {

                // 依次读取每一个单元格
                cursor.carriageReturn(); // 回车，重置列偏移量
                List<String> row_data = new ArrayList<>(); // 保存该行数据
                while(true) {

                    // 如果遇到空单元格，结束该行读取
                    var cellData = getCell(cursor.dx++);
                    if(cellData == null) {
                        cursor.gotoNextRow();
                        break;
                    }
                    else {
                        if(cellData != null && cellData.startsWith("#")) preprocessing(cellData);
                        else row_data.add(cellData); // 保存单元格数据
                    }
                }
                if(row_data.size() != 0) commands.add(row_data); // 保存该行数据
            }

            // 返回结果
            return new ExcelResult(variables, commands, blockDataList);
        }

        /**
         * 创建表格数据对象
         */
        private void createBlocks(BlockMetaData blockMeta) {

            // 获取工作表
            var sheet = excelData.get(blockMeta.sheetName);
            if(sheet == null) {
                var msg = "Sheet not found: " + blockMeta.sheetName;
                System.out.println(color(msg, ColorCode.RED));
                return;
            }

            // 获取表头
            var headerMetaList = new ArrayList<>(blockMeta.headerToColIndex.entrySet()); // 获取表头元数据列表
            var headers        = new String[headerMetaList.size()];
            for(var i = 0; i < headerMetaList.size(); i++) headers[i] = headerMetaList.get(i).getKey();

            var tableValues = new ArrayList<String>();
            for(var header : headers) tableValues.add(header);

            // 遍历表格每一行
            for(var sheet_row_idx = blockMeta.startRow; sheet_row_idx < blockMeta.endRow; sheet_row_idx++) {
                // 遍历每一列（仅考虑 column_list 中定义的列）
                for(var columnIndex = 0; columnIndex < headerMetaList.size(); columnIndex++) {
                    var column_map = headerMetaList.get(columnIndex);

                    String cell_value;
                    cell_value = getCell(blockMeta.sheetName, sheet_row_idx, column_map.getValue().col()); // 提取单元格值
                    cell_value = parse_string(cell_value, StringType.FromString(column_map.getValue().type())); // 解析出显示值

                    tableValues.add(cell_value);
                }
            }
            blockDataList.put(blockMeta.blockName, new TableData(tableValues.toArray(String[]::new), headers.length));
        }

        private void preprocessing(String cellData) {

            // 结束读取
            if(cellData.equalsIgnoreCase("#end")) {
                System.out.println(color("#End of Data.", ColorCode.GREEN));
                isReading = false;
                return;
            }

            // 跳转到指定位置
            else if(cellData.equalsIgnoreCase("#goto")) {
                jump(0);
            }

            // 定义变量
            else if(cellData.equalsIgnoreCase("#define")) {
                // 读取变量名和值（但不处理）
                var var_name  = getCell(1);
                var var_value = getCell(2);
                if(var_name != null) {
                    var_name = var_name.trim();
                    if(var_value != null) var_value = var_value.trim();
                    variables.put(var_name, var_value);
                    cursor.dx = 0;
                    System.out.println(color("#Define Variable: " + var_name + " = " + var_value, ColorCode.YELLOW));
                }
                cursor.gotoNextRow();
            }

            // 创建 TableData
            else if(cellData.equalsIgnoreCase("#block")) {
                var blockMeta = new BlockMetaData(getCell(1)); // 读取块名称

                    // 读取列信息
                    while(true) {
                        cursor.gotoNextRow();

                        if(getCell(0) == null) continue; // 跳过空行

                        var fist = getCell(0);
                        if     (fist.equals("#block_end")) break;
                        else if(fist.equals("#from"     )) blockMeta.startRow  = (int)Double.parseDouble(getCell(1)) - 1;
                        else if(fist.equals("#to"       )) blockMeta.endRow    = (int)Double.parseDouble(getCell(1)) - 1;
                        else if(fist.equals("#sheet"    )) blockMeta.sheetName = getCell(1);
                        else {
                            // 添加列
                            var header = getCell(0);
                            var colIdx = (int)Double.parseDouble(getCell(1)) - 1;
                            var type   = getCell(2);
                            blockMeta.addColumn(header, type, colIdx);
                        }
                    }
                    createBlocks(blockMeta); // 创建块数据
                    System.out.println(color("Parsed Block: " + blockMeta, ColorCode.BLUE));
            }

            // 条件跳转
            else if(cellData.equalsIgnoreCase("#goto_if")) {
                var var_name = getCell(1);
                if(variables.containsKey(var_name)) jump(1);
                else cursor.gotoNextRow();
            }

            // 其他情况继续读取下一行
            else cursor.gotoNextRow();
        }

        private void jump(int dx) {
            // 读取目标位置
            var target_row = getCell(dx + 1);
            var target_col = getCell(dx + 2);
            var r          = (int)Double.parseDouble(target_row) - 1;
            var c          = (int)Double.parseDouble(target_col) - 1;
            var newSheet   = getCell(dx + 3);

            cursor.gotoPosition(r, c, newSheet);
            cursor.dx = 0; // 重置列偏移量
            System.out.println(color("#Goto Position: (" + r + ", " + c + ") in Sheet: " + newSheet, ColorCode.GREEN));
        }

        private String getCell(int dx) {
            return getCell(cursor.sheetName, cursor.row, cursor.col + dx);
        }

        private String getCell(String sheetName, int rowIndex, int colIndex) {

            var sheet = excelData.get(sheetName);
            if(sheet == null) return null;

            if(rowIndex < 0 || rowIndex >= sheet.size()) return null;

            var row = sheet.get(rowIndex);
            if(row == null) return null;

            if(colIndex < 0 || colIndex >= row.size()) return null;

            return row.get(colIndex);
        }

        // 解析字符串
        private static String parse_string(String value, StringType type) {
            // 如果为空白串，则返回 null
            if(value == null || value.isEmpty()) return null;
            return switch(type) {
                case Bool -> value.equals("0") ? "FALSE" : "TRUE";
                case Text -> value;
                default -> {
                    try {
                        var double_value = Double.parseDouble(value);
                        if(type == StringType.Int)
                            yield String.valueOf((int)double_value);

                        var date     = DateUtil.getJavaDate(double_value);
                        var datetime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());

                        var pattern = switch(type) {
                            case Date -> "yyyy-MM-dd";
                            case Time -> "HH:mm:ss";
                            case Datetime -> "yyyy-MM-dd'T'HH:mm:ss";
                            default -> null;
                        };
                        var   fmt = DateTimeFormatter.ofPattern(pattern);
                        yield datetime.format(fmt);
                    } catch(Exception e) {
                        yield null; // 解析失败则返回空
                    }
                }
            };
        }

        private enum StringType {
            Int,
            Date,
            Time,
            Datetime,
            Bool,
            Text;

            static StringType FromString(String str) {
                if(str == null)
                    return Text;
                return switch(str.toLowerCase()) {
                    case "int" -> Int;
                    case "date" -> Date;
                    case "time" -> Time;
                    case "datetime" -> Datetime;
                    case "bool" -> Bool;
                    default -> Text;
                };
            }
        }
    }

    private final static class ExcelCursor {
        String sheetName = "main";
        int    row       = 0;
        int    col       = 0;

        int dx = 0; // 游标列偏移量

        ExcelCursor(int r, int c, String name) {
            row       = r;
            col       = c;
            sheetName = name;
        }

        void carriageReturn() {
            dx = 0;
        }

        void gotoPosition(int r, int c, String name) {
            row       = r;
            col       = c;
            sheetName = name == null ? sheetName : name;
        }

        void gotoNextRow() {
            row++;
        }
    }
}
