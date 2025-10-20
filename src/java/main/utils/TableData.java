// TableData.java

package utils;

import java.util.ArrayList;

public
record TableData(String table_name, ArrayList<String> headers, ArrayList<ArrayList<String>> data)
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


}
