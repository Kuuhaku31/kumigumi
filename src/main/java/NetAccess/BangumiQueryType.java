package NetAccess;

// Bangumi API 查询类型及其 URL 模板。
enum BangumiQueryType {

    BANGUMI_API_BASE("https://api.bgm.tv"),       // Bangumi API 基础 URL
    ANIME_INFO("%s/v0/subjects/%d"),              // 获取番剧信息的 URL 模板
    EPISODE_LIST("%s/v0/episodes?subject_id=%d"); // 获取分集信息列表的 URL 模板


    private final String format_str;

    private BangumiQueryType(String format_str) {
        this.format_str = format_str;
    }

    String formatUrl(int anime_id) {
        return String.format(format_str, BANGUMI_API_BASE, anime_id);
    }
}
