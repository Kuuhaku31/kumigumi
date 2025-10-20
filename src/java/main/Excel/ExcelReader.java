// ExcelReader.java

package Excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utils.TableData;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


enum StringType
{
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
    private final Workbook         workbook;
    private final FormulaEvaluator evaluator;

    public
    ExcelReader(String file_path) throws IOException
    {
        workbook  = new XSSFWorkbook(new FileInputStream(file_path));
        evaluator = workbook.getCreationHelper().createFormulaEvaluator();
    }

    // 解析字符串
    private static
    String ParseString(String value, StringType type)
    {
        if(value == null || value.isEmpty()) return "";
        if(type == StringType.Bool) return "0".equals(value) ? "FALSE" : "TRUE";

        return switch(type)
        {
            case Date, Time, Datetime ->
            {
                double excelSerial = Double.parseDouble(value);
                Date   date        = DateUtil.getJavaDate(excelSerial);

                String pattern = switch(type)
                {
                    case Date -> "yyyy-MM-dd";
                    case Time -> "HH:mm:ss";
                    default -> "yyyy-MM-dd HH:mm:ss";
                };
                yield new SimpleDateFormat(pattern).format(date);
            }
            default -> value;

        };
    }

    // 提取单元格值
    private
    String GetCellValue(Cell cell)
    {
        if(cell == null) return "";

        CellValue value = evaluator.evaluate(cell);
        return switch(value.getCellType())
        {
            case BOOLEAN -> value.getBooleanValue() ? "1" : "0";
            case NUMERIC -> Double.toString(value.getNumberValue());
            case STRING -> value.getStringValue();
            default -> "";
        };
    }


    /**
     * 创建表格数据对象
     */
    private
    TableData CreateTableData(Sheet sheet, String table_name, int start_row, int end_row, ColumnList column_list)
    {
        ArrayList<String>            headers = new ArrayList<>();
        ArrayList<ArrayList<String>> data    = new ArrayList<>();


        for(ColumnMap column_map : column_list.GetList()) headers.add(column_map.column_name()); // 获取表头
        for(int row_idx = start_row; row_idx < end_row; row_idx++) // 遍历表格每一行
        {
            Row row = sheet.getRow(row_idx);
            if(row == null) continue; // 跳过空行

            // 遍历每一列（仅考虑 column_list 中定义的列）
            ArrayList<String> row_data = new ArrayList<>();
            for(ColumnMap column_map : column_list.GetList())
            {
                // 跳过空单元格
                String cell_value = "";
                Cell   cell       = row.getCell(column_map.column_index());
                if(cell != null)
                {
                    cell_value = GetCellValue(cell); // 提取单元格值
                    cell_value = ParseString(cell_value, StringType.FromString(column_map.data_type())); // 解析出显示值
                }
                row_data.add(cell_value); // 添加单元格数据到 行数据
            }
            data.add(row_data); // 添加行数据到 表格数据
        }
        return new TableData(table_name, headers, data);// 创建表格数据对象
    }

    public
    ArrayList<TableData> ReadData() //
    {
        String sheet_main = "main";
        Sheet  sheet      = workbook.getSheet(sheet_main);

        ArrayList<TableData> table_data_list = new ArrayList<>();

        Sheet      dst_sheet       = null;
        String     table_name      = "";
        int        start_row       = 0;
        int        end_row         = 0;
        ColumnList column_list_buf = null;

        // 遍历所有行
        boolean is_table = false;
        for(Row row : sheet)
        {
            // 忽略空行
            if(row == null) continue;
            Cell key_cell = row.getCell(0);
            if(key_cell == null || key_cell.getCellType() == CellType.BLANK) continue;

            // 对于每一个键
            String key = key_cell.toString().trim();
            if(is_table) // 处于读取表格信息模式
            {
                switch(key)
                {
                case "_table_end": // 结束表格信息读取
                    TableData td = CreateTableData(dst_sheet, table_name, start_row, end_row, column_list_buf);
                    table_data_list.add(td);

                    is_table = false;
                    break;

                case "_sheet":
                    dst_sheet = workbook.getSheet(row.getCell(1).toString().trim());
                    break;

                case "_from":
                    start_row = (int) row.getCell(1).getNumericCellValue();
                    break;

                case "_to":
                    end_row = (int) row.getCell(1).getNumericCellValue();
                    break;

                default:
                    int column_idx = (int) row.getCell(1).getNumericCellValue();
                    Cell cell = row.getCell(2);
                    String string_type = cell == null ? "" : cell.toString().trim();
                    column_list_buf.Add(key, column_idx, string_type);
                    break;
                }
            }
            else if(key.equals("_table")) // 开始读取表格信息
            {
                table_name      = row.getCell(1).toString().trim();
                column_list_buf = new ColumnList();

                is_table = true;
            }
        }

        return table_data_list;
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
