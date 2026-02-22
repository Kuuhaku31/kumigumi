package TestDatabase;

import InfoItem.InfoAni.InfoAniFetch;

public class ARGS {
    public static final String DB_NAME = "test.db";
    public static final String DB_PATH = "ignore/db/" + DB_NAME;

    public static final String TOR_HASH_1 = "5e79512a40a38b3bd785cfaa9447fcefc5d595b6";
    public static final String TOR_PATH_1 = "ignore/a.torrent";

    public static byte[] readFile(String path) throws java.io.IOException {
        return java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path));
    }

    public static final InfoAniFetch INFO_ANI_FETCH_1;
    static {
        INFO_ANI_FETCH_1 = new InfoAniFetch(2);

        // 设置 INFO_ANI_FETCH_1 的字段值
        INFO_ANI_FETCH_1.air_date          = new java.util.Date(1234567890000L);
        INFO_ANI_FETCH_1.title             = "Test Title";
        INFO_ANI_FETCH_1.title_cn          = "测试标题";
        INFO_ANI_FETCH_1.aliases           = "Alias1, Alias2";
        INFO_ANI_FETCH_1.description       = "This is a test description.\nIt has multiple lines.\nAnd special characters: \r\n\t\"'";
        INFO_ANI_FETCH_1.episode_count     = 12;
        INFO_ANI_FETCH_1.url_official_site = "http://example.com/official";
        INFO_ANI_FETCH_1.url_cover         = "http://example.com/cover.jpg";
    }
}
