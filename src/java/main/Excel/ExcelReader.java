// ExcelReader.java

package Excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

    private
    TableData CreateTableData(Sheet sheet, String table_name, int start_row, int end_row, ColumnList column_list)
    {
        ArrayList<String>            headers = new ArrayList<>();
        ArrayList<ArrayList<String>> data    = new ArrayList<>();

        // 获取表头
        for(ColumnMap column_map : column_list.GetList())
        {
            headers.add(column_map.column_name());
        }

        // 遍历表格每一行
        for(int row_idx = start_row; row_idx < end_row; row_idx++)
        {
            // 跳过空行
            Row row = sheet.getRow(row_idx);
            if(row == null) continue;

            // 遍历每一列（仅考虑 column_list 中定义的列）
            ArrayList<String> row_data = new ArrayList<>();
            for(ColumnMap column_map : column_list.GetList())
            {
                String cell_value = "";

                // 跳过空单元格
                Cell cell = row.getCell(column_map.column_index());
                if(cell != null)
                {

                    // 提取显示值
                    CellValue value = evaluator.evaluate(cell);
                    cell_value = switch(value.getCellType())
                    {
                        case BOOLEAN -> value.getBooleanValue() ? "1" : "0";
                        case NUMERIC -> Double.toString(value.getNumberValue());
                        case STRING -> value.getStringValue();
                        default -> "";
                    };

                    // 解析
                    StringType type = switch(column_map.data_type().toLowerCase())
                    {
                        case "date" -> StringType.Date;
                        case "time" -> StringType.Time;
                        case "datetime" -> StringType.Datetime;
                        case "bool" -> StringType.Bool;
                        default -> StringType.Text;
                    };
                    cell_value = ParseString(cell_value, type);
                }

                // 添加单元格数据到 行数据
                row_data.add(cell_value);
            }

            // 添加行数据到 表格数据
            data.add(row_data);
        }

        // 创建表格数据对象
        return new TableData(table_name, headers, data);
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

    private
    enum StringType
    {
        Date,
        Time,
        Datetime,
        Bool,
        Text
    }
}
