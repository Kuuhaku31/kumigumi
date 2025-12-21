// ExcelReader.java

package Excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import util.Operation;
import util.TableData.BlockData;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExcelReader_o {
    /**
     *
     * 读取 Excel 数据
     * <p>
     * 返回 TableData 列表
     *
     */
    @SuppressWarnings("resource")
    public static ExcelInfo Read(Path file_path) {
        XSSFWorkbook workbook;
        try {
            var temp_file = Files.createTempFile("Temp_", ".txt"); // 创建临时文件（系统自动放在临时目录）
            Files.copy(file_path, temp_file, StandardCopyOption.REPLACE_EXISTING); // 将原文件复制到临时文件
            workbook = new XSSFWorkbook(new FileInputStream(temp_file.toFile()));
        } catch (Exception e) {
            System.err.println("Error reading file: " + e.getMessage());
            return null;
        }
        var evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        var main_sheet = workbook.getSheet("main");
        if (main_sheet == null)
            return null;

        // 遍历所有行
        String table_name = null;
        Sheet dst_sheet = null;
        int start_row = 0;
        int end_row = 0;
        List<BlockData> data = new ArrayList<>();
        List<ColumnMap> column_list_buf = null;
        List<Operation> operations = new ArrayList<>();
        for (var row : main_sheet) {
            // 忽略空行
            if (row == null)
                continue;
            var key_cell = row.getCell(0);
            if (key_cell == null || key_cell.getCellType() == CellType.BLANK)
                continue;

            // 对于每一个键
            var key = key_cell.toString().trim();
            if (key.equals("_end"))
                break;
            else if (table_name != null) // 处于读取表格信息模式
            {
                switch (key) {
                    case "_sheet" -> dst_sheet = workbook.getSheet(row.getCell(1).toString().trim());
                    case "_from" -> start_row = (int) row.getCell(1).getNumericCellValue();
                    case "_to" -> end_row = (int) row.getCell(1).getNumericCellValue();

                    // 结束表格信息读取
                    case "_block_end" -> {
                        var td = CreateTableData(
                                table_name,
                                evaluator, dst_sheet,
                                start_row, end_row,
                                column_list_buf);
                        data.add(td);

                        table_name = null; // 退出读取表格信息模式
                    }

                    // 读取表列信息
                    default -> {
                        var column_idx = (int) row.getCell(1).getNumericCellValue();
                        if (column_idx < 0)
                            break;
                        var cell = row.getCell(2);
                        var string_type = cell == null ? "" : cell.toString().trim();
                        column_list_buf.add(new ColumnMap(key, column_idx, string_type));
                    }
                }
            } else if (key.equals("_block")) // 开始读取块信息
            {
                table_name = row.getCell(1).toString().trim();
                column_list_buf = new ArrayList<>(); // 重新开始获取列元数据
            } else // 读取命令
            {
                List<String> args_list = new ArrayList<>();
                for (int i = 1; i < row.getLastCellNum(); i++) {
                    var cell = row.getCell(i);
                    if (cell == null || cell.getCellType() == CellType.BLANK)
                        break;
                    args_list.add(cell.toString().trim());
                }
                operations.add(new Operation(key, args_list.toArray(new String[0])));
            }
        }
        return new ExcelInfo(operations, data);
    }

    /**
     * 创建表格数据对象
     */
    private static BlockData CreateTableData(
            String block_name,
            FormulaEvaluator e, Sheet s,
            int start_row, int end_row,
            List<ColumnMap> column_list) {
        // 获取表头
        var headers = new String[column_list.size()];
        for (var i = 0; i < column_list.size(); i++)
            headers[i] = column_list.get(i).column_name;

        // 遍历表格每一行
        // 根据类型创建表格数据对象
        BlockData data = new BlockData(block_name, headers);
        for (var sheet_row_idx = start_row; sheet_row_idx < end_row; sheet_row_idx++) {
            var recode = data.new Record();

            // 遍历每一列（仅考虑 column_list 中定义的列）
            var row = s.getRow(sheet_row_idx);
            for (var column_map : column_list) {
                var cell = row.getCell(column_map.column_index);

                String cell_value;
                cell_value = GetCellValue(e, cell); // 提取单元格值
                cell_value = ParseString(cell_value, StringType.FromString(column_map.data_type)); // 解析出显示值

                recode.Set(column_map.column_name, cell_value);
            }
        }

        return data;
    }

    // 解析字符串
    private static String ParseString(String value, StringType type) {
        // 如果为空白串，则返回 null
        if (value == null || value.isEmpty())
            return null;
        return switch (type) {
            case Bool -> value.equals("0") ? "FALSE" : "TRUE";
            case Text -> value;
            default -> {
                try {
                    double double_value = Double.parseDouble(value);
                    if (type == StringType.Int)
                        yield String.valueOf((int) double_value);

                    Date date = DateUtil.getJavaDate(double_value);

                    String pattern = switch (type) {
                        case Date -> "yyyy-MM-dd";
                        case Time -> "HH:mm:ss";
                        case Datetime -> "yyyy-MM-dd HH:mm:ss";
                        default -> null;
                    };

                    yield new SimpleDateFormat(pattern).format(date);
                } catch (Exception e) {
                    yield null; // 解析失败则返回空
                }
            }

        };
    }

    private static String GetCellValue(FormulaEvaluator evaluator, Cell cell) {
        // DataFormatter formatter = new DataFormatter();
        // return formatter.formatCellValue(cell, null);

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

    private enum StringType {
        Int,
        Date,
        Time,
        Datetime,
        Bool,
        Text;

        public static StringType FromString(String str) {
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

    private record ColumnMap(String column_name, int column_index, String data_type) {
    }

    public record ExcelInfo(List<Operation> operations, List<BlockData> data) {
    }
}
