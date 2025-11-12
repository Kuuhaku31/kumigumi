// DataBuffer.java


package utils;

import utils.TableData.TableData;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public
class DataBuffer
{
    public static
    void SaveDataList(ArrayList<TableData> data_list)
    {
        Path path = Paths.get("D:/repositories/kumigumi/ignore/DataBuffer.txt");

        try
        {
            Path parent = path.getParent();
            if(parent != null)
            {
                Files.createDirectories(parent);
            }

            // 覆盖写入文件，使用 UTF-8 编码
            try(BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8))
            {
                if(data_list == null)
                {
                    return;
                }
                for(TableData data : data_list)
                {
                    String line = (data == null) ? "null" : data.toString();
                    writer.write(line);
                    writer.newLine();
                }
            }
        }
        catch(IOException e)
        {
            // 简单输出错误，按需替换为日志
            e.printStackTrace();
        }
    }
}
