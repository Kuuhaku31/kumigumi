// ExcelReader.java

package Excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utils.TableData;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


enum StringType
{
    Int,
    Date,
    Time,
    Datetime,
    Bool,
    Text;

    public static
    StringType FromString(String str)
    {
        return switch(str.toLowerCase())
        {
            case "int" -> Int;
            case "date" -> Date;
            case "time" -> Time;
            case "datetime" -> Datetime;
            case "bool" -> Bool;
            default -> Text;
        };
    }
}

public
class ExcelReader
{
    // 解析字符串
    private static
    String ParseString(String value, StringType type)
    {
        // 如果为空白串，则返回 null
        if(value.isEmpty()) return null;
        return switch(type)
        {
            case Bool -> value.equals("0") ? "FALSE" : "TRUE";
            case Text -> value;
            default ->
            {
                try
                {
                    double double_value = Double.parseDouble(value);
                    if(type == StringType.Int) yield String.valueOf((int) double_value);

                    Date date = DateUtil.getJavaDate(double_value);

                    String pattern = switch(type)
                    {
                        case Date -> "yyyy-MM-dd";
                        case Time -> "HH:mm:ss";
                        case Datetime -> "yyyy-MM-dd HH:mm:ss";
                        default -> null;
                    };

                    yield new SimpleDateFormat(pattern).format(date);
                }
                catch(Exception e)
                {
                    yield null; // 解析失败则返回空
                }
            }

        };
    }

    public static
    ArrayList<TableData> ReadData(String file_path) throws IOException //
    {
        // 创建临时文件（系统自动放在临时目录）
        var temp_file = Files.createTempFile("Temp_", ".txt");
        Files.copy(Path.of(file_path), temp_file, StandardCopyOption.REPLACE_EXISTING); // 将原文件复制到临时文件

        // 访问文件
        final var workbook   = new XSSFWorkbook(new FileInputStream(temp_file.toFile()));
        final var evaluator  = workbook.getCreationHelper().createFormulaEvaluator();
        final var main_sheet = workbook.getSheet("main");

        // 遍历所有行
        ArrayList<TableData> table_data_list = new ArrayList<>();

        Sheet      dst_sheet       = null;
        String     table_name      = "";
        int        start_row       = 0;
        int        end_row         = 0;
        ColumnList column_list_buf = null;

        boolean is_reading_table_info = false; // 是否处于读取表格信息模式
        for(var row : main_sheet)
        {
            // 忽略空行
            if(row == null) continue;
            var key_cell = row.getCell(0);
            if(key_cell == null || key_cell.getCellType() == CellType.BLANK) continue;

            // 对于每一个键
            String key = key_cell.toString().trim();
            if(is_reading_table_info) // 处于读取表格信息模式
            {
                switch(key)
                {
                case "_sheet" -> dst_sheet = workbook.getSheet(row.getCell(1).toString().trim());
                case "_from" -> start_row = (int) row.getCell(1).getNumericCellValue();
                case "_to" -> end_row = (int) row.getCell(1).getNumericCellValue();

                case "_table_end" ->
                {
                    TableData td = CreateTableData(
                        evaluator, dst_sheet,
                        table_name,
                        start_row, end_row,
                        column_list_buf
                    );
                    table_data_list.add(td);

                    is_reading_table_info = false;
                } // 结束表格信息读取

                default ->
                {
                    int    column_idx  = (int) row.getCell(1).getNumericCellValue();
                    Cell   cell        = row.getCell(2);
                    String string_type = cell == null ? "" : cell.toString().trim();
                    column_list_buf.Add(key, column_idx, string_type);
                } // 读取表列信息
                }
            }
            else if(key.equals("_table")) // 开始读取表格信息
            {
                table_name      = row.getCell(1).toString().trim();
                column_list_buf = new ColumnList();

                is_reading_table_info = true;
            }
        }

        // 转换成数组
        return table_data_list;
    }

    /**
     * 创建表格数据对象
     */
    private static
    TableData CreateTableData(
        FormulaEvaluator evaluator, Sheet sheet,
        String table_name,
        int start_row, int end_row,
        ColumnList column_list
    )
    {
        String[]   headers = new String[column_list.GetLength()];
        String[][] data    = new String[end_row - start_row][column_list.GetLength()];

        int header_idx = 0;
        for(ColumnMap column_map : column_list.GetList()) headers[header_idx++] = column_map.column_name(); // 获取表头
        for(int row_index = 0, sheet_row_idx = start_row; sheet_row_idx < end_row; row_index++, sheet_row_idx++)
        { // 遍历表格每一行
            Row row = sheet.getRow(sheet_row_idx);

            // 遍历每一列（仅考虑 column_list 中定义的列）
            int column_index = 0;
            for(ColumnMap column_map : column_list.GetList())
            {
                var cell = row.getCell(column_map.column_index());

                String cell_value;
                cell_value = GetCellValue(evaluator, cell);                                          // 提取单元格值
                cell_value = ParseString(cell_value, StringType.FromString(column_map.data_type())); // 解析出显示值

                data[row_index][column_index++] = cell_value; // 添加单元格数据到 行数据
            }
        }
        return new TableData(table_name, headers, data);// 创建表格数据对象
    }

    // 提取单元格值
    private static
    String GetCellValue(FormulaEvaluator evaluator, Cell cell)
    {
        // DataFormatter formatter = new DataFormatter();
        // return formatter.formatCellValue(cell, null);

        // 处理空单元格
        CellValue value = evaluator.evaluate(cell);
        if(value == null) return "";

        return switch(value.getCellType())
        {
            case BOOLEAN -> value.getBooleanValue() ? "1" : "0";
            case NUMERIC -> Double.toString(value.getNumberValue());
            case STRING -> value.getStringValue();
            default -> "";
        };
    }


}


class ColumnList
{
    private final ArrayList<ColumnMap> list = new ArrayList<>();

    public
    void Add(String column_name, int column_index, String data_type)
    {
        list.add(new ColumnMap(column_name, column_index, data_type));
    }

    public
    int GetLength()
    {
        return list.size();
    }

    // 迭代器
    public
    Iterable<ColumnMap> GetList()
    {
        return list;
    }
}

record ColumnMap(String column_name, int column_index, String data_type)
{
}
