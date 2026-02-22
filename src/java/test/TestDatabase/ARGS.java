package TestDatabase;

public class ARGS {
    public static final String DB_NAME = "test.db";
    public static final String DB_PATH = "ignore/db/" + DB_NAME;

    public static final String TOR_HASH_1 = "5e79512a40a38b3bd785cfaa9447fcefc5d595b6";
    public static final String TOR_PATH_1 = "ignore/a.torrent";

    public static byte[] readFile(String path) throws java.io.IOException {
        return java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path));
    }
}
