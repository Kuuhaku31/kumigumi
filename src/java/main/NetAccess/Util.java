// Util.java


package NetAccess;

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
}
