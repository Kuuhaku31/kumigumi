package util;


import util.TableData.TableData;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public
class Util
{
    // 解析字幕组
    public static
    String ParseSubtitleGroup(String title)
    {
        String[] left_brackets  = {"[", "【", "(", "（"};
        String[] right_brackets = {"]", "】", ")", "）"};

        for(int i = 0; i < left_brackets.length; i++)
        {
            // 如果匹配到左括号
            if(title.startsWith(left_brackets[i]))
            {
                int end_index = title.indexOf(right_brackets[i]);         // 找到对应的右括号
                if(end_index != -1) return title.substring(1, end_index); // 如果找到了右括号，就提取中间的字幕组名称
            }
        }
        return null; // 如果没有匹配到任何括号，就返回 null
    }

    public static
    Long ParseSizeToBytes(String size_str)
    {
        if(size_str == null || size_str.isEmpty()) return null;

        size_str = size_str.trim().replace(",", ".");
        String[] parts = size_str.split(" ");
        if(parts.length != 2) return null;

        double value = Double.parseDouble(parts[0]);
        String unit  = parts[1].toUpperCase();
        return switch(unit)
        {
            case "B" -> (long) value;
            case "KIB" -> (long) (value * 1024);
            case "MIB" -> (long) (value * 1024 * 1024);
            case "GIB" -> (long) (value * 1024 * 1024 * 1024);
            case "TIB" -> (long) (value * 1024L * 1024 * 1024 * 1024);

            // 万一 站点返回 MB / GB 等十进制单位，也兼容：
            case "KB" -> (long) (value * 1000);
            case "MB" -> (long) (value * 1000 * 1000);
            case "GB" -> (long) (value * 1000 * 1000 * 1000);
            case "TB" -> (long) (value * 1000L * 1000 * 1000 * 1000);

            default -> null;
        };
    }

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
