package test;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;

public
class TestEX
{
    void main(String[] args) throws IOException
    {
        IO.println("TestEX:");
        String file_path = args[0];

        Excel.ExcelReader reader = new Excel.ExcelReader(file_path);

        ArrayList<AbstractMap.SimpleEntry<String, String>> res = reader.ReadShell("main");

        // 打印输出
        for(AbstractMap.SimpleEntry<String, String> entry : res)
        {
            IO.println("Key: \"" + entry.getKey() + "\", Value: \"" + entry.getValue() + "\"");
        }
    }
}
