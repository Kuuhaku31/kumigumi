// TableData.java

package Excel;

import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;

public
class TableData
{
    public final String                       table_name;
    public final ArrayList<String>            headers;
    public final ArrayList<ArrayList<String>> data;

    public
    TableData(
        Sheet sheet,
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
                        cell_value = cell.toString().trim();
                    }
                }

                // 添加单元格数据到行数据
                row_data.add(cell_value);
            }

            data.add(row_data);
        }
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
}
