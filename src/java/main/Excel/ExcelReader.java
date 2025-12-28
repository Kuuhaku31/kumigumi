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
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReader {
    private XSSFWorkbook workbook; // Excel 工作簿
    private FormulaEvaluator evaluator;// 公式计算器

    private CellPosition cursor = new CellPosition(); // 光标位置
    private Map<String, String> variables = new HashMap<>(); // 定义的变量
    private List<List<String>> commands = new ArrayList<>(); // 保存读取的数据
    private List<BlockData> blockDataList = new ArrayList<>(); // 保存块信息

    public List<BlockData> getBlockDataList() {
        return blockDataList;
    }

    public String getCommandsInfo() {
        var sb = new StringBuilder();
        sb.append("#Data List:\n");
        for (var row : commands) {
            sb.append("\tRow: [");
            for (var cell : row) {
                sb.append(cell).append(", ");
            }
            sb.append("]\n");
        }
        return sb.toString();
    }

    public String getVariables() {
        var sb = new StringBuilder();
        sb.append("#Defined Variables:\n");
        for (var entry : variables.entrySet())
            sb.append("\t").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        return sb.toString();
    }

    public String getBlocksInfo() {
        var sb = new StringBuilder();
        for (var blockData : blockDataList) {
            sb.append(blockData).append("\n");
        }
        return sb.toString();
    }

    public ExcelReader(String filePath) throws IOException {
        // 创建临时文件（系统自动放在临时目录）
        var temp_file = Files.createTempFile("Temp_", ".txt");
        System.out.println("ExcelReader: Copying file to temp file: " + temp_file.toAbsolutePath().toString());
        Files.copy(Files.newInputStream(Path.of(filePath)), temp_file, StandardCopyOption.REPLACE_EXISTING);
        workbook = new XSSFWorkbook(new FileInputStream(temp_file.toFile()));
        evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        // 遍历所有行，保存数据
        boolean isReading = true;
        while (isReading) {
            var dx = 0; // 游标列偏移量
            var row_data = new ArrayList<String>();
            while (true) {
                // 依次读取每一个单元格
                var cell = getCell(dx++);
                if (cell == null || cell.getCellType() == CellType.BLANK) {
                    cursor.gotoNextRow();
                    break; // 如果遇到空单元格，结束该行读取

                } else {
                    var cellData = GetCellValue(cell); // 读取单元格数据
                    if (cellData != null && cellData.startsWith("#"))
                        isReading = 特殊标记处理(cellData);
                    else
                        row_data.add(cellData); // 保存单元格数据
                }
            }
            if (row_data.size() != 0)
                commands.add(row_data); // 保存该行数据
        }

        createBlockData(); // 解析命令
    }

    /** 解析命令 */
    private void createBlockData() {
        var it = commands.iterator();
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
                        blockMeta.startRow = (int) Double.parseDouble(row.get(1)) - 1;
                    } else if (fist.equals("_to")) {
                        blockMeta.endRow = (int) Double.parseDouble(row.get(1)) - 1;
                    } else if (fist.equals("_sheet")) {
                        blockMeta.sheetName = row.get(1);
                    } else {
                        // 添加列
                        var header = row.get(0);
                        var colIdx = (int) Double.parseDouble(row.get(1)) - 1;
                        var type = row.size() >= 3 ? row.get(2) : null;
                        blockMeta.addColumn(header, type, colIdx);
                    }
                }
                createBlocks(blockMeta); // 创建块数据
                System.out.println("Parsed Block: " + blockMeta);
            }
        }
    }

    /** 创建表格数据对象 */
    private void createBlocks(BlockMetaData blockMeta) {

        // 获取工作表
        var sheet = workbook.getSheet(blockMeta.sheetName);
        if (sheet == null) {
            System.out.println("Sheet not found: " + blockMeta.sheetName);
            return;
        }

        // 获取表头
        var headerMetaList = new ArrayList<>(blockMeta.headerToColIndex.entrySet()); // 获取表头元数据列表
        var headers = new String[headerMetaList.size()];
        for (var i = 0; i < headerMetaList.size(); i++)
            headers[i] = headerMetaList.get(i).getKey();

        // 遍历表格每一行
        // 根据类型创建表格数据对象
        var data = new BlockData(blockMeta.blockName, headers);
        for (var sheet_row_idx = blockMeta.startRow; sheet_row_idx < blockMeta.endRow; sheet_row_idx++) {
            var recode = data.new Record();

            // 遍历每一列（仅考虑 column_list 中定义的列）
            var row = sheet.getRow(sheet_row_idx);
            for (var column_map : headerMetaList) {
                var cell = row.getCell(column_map.getValue().col());

                String cell_value;
                cell_value = GetCellValue(cell); // 提取单元格值
                cell_value = ParseString(cell_value, StringType.FromString(column_map.getValue().type())); // 解析出显示值

                recode.Set(column_map.getKey(), cell_value);
            }
        }
        blockDataList.add(data);
    }

    private boolean 特殊标记处理(String cellData) {
        if (cellData.equalsIgnoreCase("#end")) // 结束读取
        {
            System.out.println("#End of Data.");
            return false;

        } else if (cellData.equalsIgnoreCase("#goto")) // 跳转到指定位置
        {
            jump(0);

        } else if (cellData.equalsIgnoreCase("#define")) // 定义变量
        {
            // 读取变量名和值（但不处理）
            var var_name = GetCellValue(getCell(1));
            var var_value = GetCellValue(getCell(2));
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
            var var_name = GetCellValue(getCell(1));
            // 如果存在则跳转
            if (variables.containsKey(var_name))
                jump(1);
            else
                cursor.gotoNextRow();
        } else
            cursor.gotoNextRow();
        return true;
    }

    private String GetCellValue(Cell cell) {

        CellValue value = null;
        try {
            value = evaluator.evaluate(cell);
        } catch (Exception e) {
            System.out.println("GetCellValue Error: " + e.getMessage());
            System.out.println(cell);
            System.exit(1);
        }
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
        var target_row = GetCellValue(getCell(dx + 1));
        var target_col = GetCellValue(getCell(dx + 2));
        var r = (int) Double.parseDouble(target_row) - 1;
        var c = (int) Double.parseDouble(target_col) - 1;
        var newSheet = GetCellValue(getCell(dx + 3));

        cursor.gotoPosition(r, c, newSheet);
        System.out.println("#Goto Position: (" + r + ", " + c + ") in Sheet: " + newSheet);
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

    // 解析字符串
    private String ParseString(String value, StringType type) {
        // 如果为空白串，则返回 null
        if (value == null || value.isEmpty())
            return null;
        return switch (type) {
            case Bool -> value.equals("0") ? "FALSE" : "TRUE";
            case Text -> value;
            default -> {
                try {
                    var double_value = Double.parseDouble(value);
                    if (type == StringType.Int)
                        yield String.valueOf((int) double_value);

                    var date = DateUtil.getJavaDate(double_value);
                    var datetime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());

                    var pattern = switch (type) {
                        case Date -> "yyyy-MM-dd";
                        case Time -> "HH:mm:ss";
                        case Datetime -> "yyyy-MM-dd'T'HH:mm:ssXXX";
                        default -> null;
                    };
                    var fmt = DateTimeFormatter.ofPattern(pattern);
                    yield datetime.atZone(ZoneId.systemDefault()).format(fmt);
                } catch (Exception e) {
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
            if (str == null)
                return Text;
            return switch (str.toLowerCase()) {
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