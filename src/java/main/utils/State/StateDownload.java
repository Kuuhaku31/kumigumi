// StateDownload.java

package utils.State;

public
enum StateDownload
{
    NotDownloaded,
    Downloaded,
    DoNotDownload;

    public static
    StateDownload fromString(String str)
    {
        return switch(str)
        {
            case "未下载" -> NotDownloaded;
            case "已下载" -> Downloaded;
            case "不下载" -> DoNotDownload;
            default -> throw new IllegalArgumentException();
        };
    }

    // 重载 toString 方法，返回中文状态
    @Override
    public
    String toString()
    {
        return switch(this)
        {
            case NotDownloaded -> "未下载";
            case Downloaded -> "已下载";
            case DoNotDownload -> "不下载";
        };
    }
}

