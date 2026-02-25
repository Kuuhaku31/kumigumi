package TestDatabase;

import java.io.IOException;

import InfoItem.InfoAni.InfoAniFetch;
import InfoItem.InfoTor.InfoTorFetch;

public class ARGS {
    public static final String DB_NAME = "test.db";
    public static final String DB_PATH = "ignore/db/" + DB_NAME;

    public static final String TOR_HASH_1 = "5e79512a40a38b3bd785cfaa9447fcefc5d595b6";
    public static final String TOR_HASH_2 = "HASH222";
    public static final String TOR_HASH_3 = "HASH333";
    public static final String TOR_PATH_1 = "ignore/a.torrent";
    public static final String TOR_PATH_B = "ignore/b.torrent";
    public static final byte[] TOR_FILE_BIN_1;
    public static final byte[] TOR_FILE_BIN_B;
    static {
        try {
            TOR_FILE_BIN_1 = readFile(TOR_PATH_1); 
            TOR_FILE_BIN_B = readFile(TOR_PATH_B);
        }
        catch(IOException _) { throw new RuntimeException("Failed to read file"); }
    }

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

    public static final InfoTorFetch INFO_TOR_FETCH_1;
    static {
        try { INFO_TOR_FETCH_1 = new InfoTorFetch(TOR_HASH_1, readFile(TOR_PATH_1)); }
        catch(IOException _) { throw new RuntimeException("Failed to read file for INFO_TOR_FETCH_1"); }
    }
}
