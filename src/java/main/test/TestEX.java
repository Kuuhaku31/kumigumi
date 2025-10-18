package test;

import java.io.IOException;

public
class TestEX
{
    void main(String[] args) throws IOException
    {
        IO.println("TestEX:");
        String file_path = args[0];

        Excel.ExcelReader reader = new Excel.ExcelReader(file_path);
        reader.ReadData();
        // reader.TestTableData();

        // var res = reader.ReadShell("main");
        //
        // // 打印输出
        // for(var entry : res)
        // {
        //     IO.println("\"" + entry.getKey() + "\" : \"" + entry.getValue() + "\"");
        // }
    }
}
