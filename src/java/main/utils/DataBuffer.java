// DataBuffer.java


package utils;

import utils.TableData.TableData;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public
class DataBuffer
{
    public static
    void SaveDataList(List<TableData> data_list)
    {
        var path = Paths.get("D:/repositories/kumigumi/ignore/DataBuffer.txt");

        try
        {
            var parent = path.getParent();
            if(parent != null) { Files.createDirectories(parent); }

            // 覆盖写入文件，使用 UTF-8 编码
            try(var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8))
            {
                if(data_list == null) return;
                for(var data : data_list)
                {
                    var line = (data == null) ? "null" : data.toString();
                    writer.write(line);
                    writer.newLine();
                }
            }
        }
        catch(IOException e) { System.err.println(e.getMessage()); }
        System.out.println("Log save to " + path);
    }
}
