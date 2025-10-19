// TableData.java

package Excel;

import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public
class TableData
{
    public final String                       table_name;
    public final ArrayList<String>            headers;
    public final ArrayList<ArrayList<String>> data;

    public
    TableData(
        Sheet sheet,
        FormulaEvaluator evaluator,
        String table_name,
        int start_row, int end_row,
        ColumnList column_list
    )
    {
        var list = column_list.GetList();

        this.table_name = table_name;
        data            = new ArrayList<>();
        headers         = new ArrayList<>();


        // 获取表头
        for(var column_map : list)
        {
            headers.add(column_map.column_name());
        }

        // 遍历表格每一行
        for(int row_idx = start_row; row_idx < end_row; row_idx++)
        {
            var row      = sheet.getRow(row_idx);
            var row_data = new ArrayList<String>();

            // 遍历每一列
            for(var column_map : list)
            {
                String cell_value = "";

                // 获取单元格
                if(row != null)
                {
                    var cell = row.getCell(column_map.column_index());


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
                            case "datetime" -> StringType.Datetime;
                            case "bool" -> StringType.Bool;
                            default -> StringType.Text;
                        };

                        cell_value = ParseString(cell_value, type);
                    }
                }

                // 添加单元格数据到 行数据
                row_data.add(cell_value);
            }

            data.add(row_data);
        }
    }

    // 解析字符串
    private static
    String ParseString(String value, StringType type)
    {
        return switch(type)
        {
            case Date ->
            {
                // 1. 转为 double
                double excelSerial = Double.parseDouble(value);

                // 2. 转换为 Java Date
                Date date = DateUtil.getJavaDate(excelSerial);

                // 3. 格式化输出
                yield new SimpleDateFormat("yyyy-MM-dd").format(date);
            }
            case Datetime ->
            {
                // 1. 转为 double
                double excelSerial = Double.parseDouble(value);

                // 2. 转换为 Java Date
                Date date = DateUtil.getJavaDate(excelSerial);

                // 3. 格式化输出
                yield new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
            }
            case Bool -> value.equals("0") ? "FALSE" : "TRUE";
            default -> value;
        };
    }

    public
    void PrintInfo()
    {
        String header     = "================== [" + table_name + "] ==================";
        int    header_len = header.length();
        System.out.println(header);

        // 打印表头
        for(var col_name : headers)
        {
            System.out.print(col_name + "\t");
        }
        System.out.println();

        // 打印分隔线
        for(int i = 0; i < header_len; i++) System.out.print("-");
        System.out.println();

        // 打印数据行
        for(var row_data : data)
        {
            for(var cell_value : row_data)
            {
                System.out.print(cell_value + "\t");
            }
            System.out.println();
        }

        // 打印结束线
        for(int i = 0; i < header_len; i++) System.out.print("=");
        System.out.println();
    }

    private
    enum StringType
    {
        Date,
        Datetime,
        Bool,
        Text
    }
}
