// StateView.java

package utils.State;

public
enum StateView
{
    NotWatched,
    Watched,
    DoNotWatch;

    public static
    StateView fromString(String str)
    {
        return switch(str)
        {
            case "未观看" -> NotWatched;
            case "已观看" -> Watched;
            case "不观看" -> DoNotWatch;
            default -> throw new IllegalArgumentException();
        };
    }

    @Override
    public
    String toString()
    {
        return switch(this)
        {
            case NotWatched -> "未观看";
            case Watched -> "已观看";
            case DoNotWatch -> "不观看";
        };
    }
}