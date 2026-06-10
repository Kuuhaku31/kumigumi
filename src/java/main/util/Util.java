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

    public static void WriteStringToFile(String str, String filePath) {

        try {
            // 保证目录存在
            Files.createDirectories(Path.of(filePath).getParent());

            // 写入文件
            try(var writer = Files.newBufferedWriter(Path.of(filePath))) {
                writer.write(str);
            }
        } catch(IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static Date parseDate(String dateStr) {

        Date air_date = null;

        if(dateStr != null) try {
            var sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            air_date = sdf.parse(dateStr);
        } catch (java.text.ParseException _) {}

        return air_date;
    }

    public static String standardString(String str) {
        if(str == null) return null;
        var res_str = str.trim();
        res_str = res_str.replace("\r", "\\r");
        res_str = res_str.replace("\n", "\\n");
        return res_str;
    }

    // Thu, 28 May 2026 22:06:07 -0000 -> OffsetDateTime
    public static OffsetDateTime parseOffsetDateTime(String dateStr) {
        if(dateStr == null) return null;
        try {
            var rssFormat  = new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.ENGLISH);
            var parsedDate = rssFormat.parse(dateStr);
            if(parsedDate == null) return null;
            return parsedDate.toInstant().atOffset(java.time.ZoneOffset.UTC);
        } catch(java.text.ParseException e) {
            return null;
        }
    }

    public static String getInfoString(Map<String, Object> info) {
        var sb = new StringBuilder();
        sb.append("{");
        info.forEach((key, value) -> sb.append(key).append(": ").append(value).append(", "));
        if(!info.isEmpty()) {
            sb.setLength(sb.length() - 2); // 去掉最后的逗号和空格
        }
        sb.append("}");
        return sb.toString();
    }

    public static String color(String str, ColorCode colorCode) {
        var res = str.replace("\033[0m", colorCode.getCode()); // 先替换掉字符串中已有的重置代码，避免颜色被重置
        res     = colorCode.getCode() + res + "\033[0m";
        return res;
    }
}
