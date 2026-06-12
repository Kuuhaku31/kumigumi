package Excel;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.poi.ss.usermodel.DateUtil;


enum CellStringType {

    Int,
    Date,
    Time,
    Datetime,
    Bool,
    Text;

    // 从字符串创建 CellStringType，默认为 Text
    static CellStringType fromString(String str) {

        if(str == null) return Text;
        else return switch(str.toLowerCase()) {
            case "int"      -> Int;
            case "date"     -> Date;
            case "time"     -> Time;
            case "datetime" -> Datetime;
            case "bool"     -> Bool;
            default         -> Text;
        };
    }

    // 解析字符串
    static String parseCellString(String value, CellStringType type) {

        // 如果为空白串，则返回 null
        if(value == null || value.isBlank()) return null;

        return switch(type) {
            case Bool -> value.equals("0") ? "FALSE" : "TRUE";
            case Text -> value;
            default   -> {
                try {
                    var double_value = Double.parseDouble(value);
                    if(type == CellStringType.Int)
                        yield String.valueOf((int)double_value);

                    var date     = DateUtil.getJavaDate(double_value);
                    var datetime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());

                    var pattern = switch(type) {
                        case Date -> "yyyy-MM-dd";
                        case Time -> "HH:mm:ss";
                        case Datetime -> "yyyy-MM-dd'T'HH:mm:ss";
                        default -> null;
                    };
                    var   fmt = DateTimeFormatter.ofPattern(pattern);
                    yield datetime.format(fmt);
                } catch(Exception e) {
                    yield null; // 解析失败则返回空
                }
            }
        };
    }
}
