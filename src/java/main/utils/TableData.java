// TableData.java

package utils;

import org.jetbrains.annotations.NotNull;

public
record TableData(String table_name, String[] headers, String[][] data)
{
    public
    void PrintInfo()
    {
        String header     = "================== [" + table_name + "] ==================";
        int    header_len = header.length();
        IO.println(header);

        // 打印表头
        for(var col_name : headers) IO.print(col_name + "\t");
        IO.println();

        // 打印分隔线
        for(int i = 0; i < header_len; i++) IO.print("-");
        IO.println();

        // 打印数据行
        for(var row_data : data)
        {
            for(var cell_value : row_data) IO.print(cell_value + "\t");
            IO.println();
        }

        // 打印结束线
        for(int i = 0; i < header_len; i++) IO.print("=");
        IO.println();
    }

    public @NotNull
    String toString()
    {
        StringBuilder res = new StringBuilder();
        res.append("Table Name: ").append(table_name).append("\n");
        res.append("Headers: ");
        for(String header : headers)
        {
            res.append(header).append("\t");
        }
        res.append("\nData:\n");
        for(String[] row : data)
        {
            for(String cell : row)
            {
                res.append(cell).append("\t");
            }
            res.append("\n");
        }


        return res.toString();
    }

}
