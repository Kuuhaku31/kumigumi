package Task;


public
class TestFetch
{
    static
    void main()
    {
        var rss = "https://mikanani.me/RSS/Bangumi?bangumiId=3806";
        var t   = new TaskFetchTor(1, rss);

        t.run();

        System.out.println(t);
    }
}
