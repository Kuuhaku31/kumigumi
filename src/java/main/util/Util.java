package Util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;

public class Util {

    /**
     * 以 yyyy-MM-dd 格式显示 date
     */
    public static String getDateString(Date date) {
        return (date == null) ? null : String.format("%tF", date);
    }

    /**
     * 以 yyyy-MM-ddTHH:mm:ssXXX 格式显示 datetime
     */
    public static String getDateString(OffsetDateTime datetime) {
        if(datetime == null)
            return null;
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        return datetime.format(formatter);
    }

    public static void printMapList(List<Map<String, String>> list) {
        for(var map : list)
            printMap(map);
    }

    public static void printMap(Map<String, String> map) {
        System.out.println("--- Map ------------------------------------------");
        for(var entry : map.entrySet()) {
            var key   = entry.getKey();
            var value = entry.getValue();
            System.out.println("[" + key + "]: " + value);
        }
        System.out.println();
    }

    public static void SaveDataList(List<TableData> data_list) {
        var path = Paths.get("D:/repositories/kumigumi/ignore/DataBuffer.txt");

        try {
            var parent = path.getParent();
            if(parent != null) {
                Files.createDirectories(parent);
            }

            // 覆盖写入文件，使用 UTF-8 编码
            try(var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                if(data_list == null)
                    return;
                for(var data : data_list) {
                    var line = (data == null) ? "null" : data.toString();
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch(IOException e) {
            System.err.println(e.getMessage());
        }
        System.out.println("Log save to " + path);
    }

    public static void WriteItemListToFile(List<?> itemList, String filePath) {
        try {
            // 保证目录存在
            Files.createDirectories(Path.of(filePath).getParent());

            // 写入文件
            try(var writer = Files.newBufferedWriter(Path.of(filePath))) {
                for(var item : itemList) {
                    writer.write(item.toString());
                    writer.write("\n");
                }
            }
        } catch(IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void WriteStringToFile(String str, String filePath) throws IOException {
        // 保证目录存在
        Files.createDirectories(Path.of(filePath).getParent());

        // 写入文件
        try(var writer = Files.newBufferedWriter(Path.of(filePath))) {
            writer.write(str);
        }
    }
}
