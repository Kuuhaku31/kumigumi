package Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import Database.SQLiteAccess;


public final class CreateDatabaseViews {

    private CreateDatabaseViews() {}

    public static void main(String[] args) throws IOException, SQLException {
        if(args.length != 3 || args[0].isBlank() || args[1].isBlank() || args[2].isBlank()) {
            throw new IllegalArgumentException(
                "Usage: Utils.CreateDatabaseViews <database-path> <ani-id-file> <rss-url-file>"
            );
        }

        var aniIds = ReadAnimeIds(args[1]);
        var rssURLs = ReadRssURLs(args[2]);
        Create(args[0], aniIds, rssURLs);
        System.out.println(
            "视图筛选条件更新完成: " + args[0]
            + "，ANI_ID 数量: " + aniIds.size()
            + "，RSS URL 数量: " + rssURLs.size()
        );
    }

    public static void Create(
        String databasePath,
        String aniIdFilePath,
        String rssURLFilePath
    ) throws IOException, SQLException {

        Create(databasePath, ReadAnimeIds(aniIdFilePath), ReadRssURLs(rssURLFilePath));
    }

    public static void Create(
        String       databasePath,
        Set<Integer> aniIds,
        Set<String>  rssURLs
    ) throws SQLException {

        if(databasePath == null || databasePath.isBlank()) {
            throw new IllegalArgumentException("Database path cannot be blank");
        }
        if(aniIds == null) throw new IllegalArgumentException("ANI_ID set cannot be null");
        if(rssURLs == null) throw new IllegalArgumentException("RSS URL set cannot be null");

        try(var db = new SQLiteAccess(databasePath)) {
            db.ReplaceRequiredViewFilters(aniIds, rssURLs);
        }
    }

    public static Set<Integer> ReadAnimeIds(String filePath) throws IOException {
        if(filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("ANI_ID file path cannot be blank");
        }

        var aniIds = new LinkedHashSet<Integer>();
        try(var reader = Files.newBufferedReader(Path.of(filePath))) {
            String line;
            var lineNumber = 0;
            while((line = reader.readLine()) != null) {
                lineNumber++;
                var trimmedLine = line.trim();
                if(trimmedLine.isEmpty()) continue;

                for(var value : trimmedLine.split("[,;\\s]+")) {
                    try {
                        aniIds.add(Integer.parseInt(value));
                    } catch(NumberFormatException e) {
                        throw new IllegalArgumentException(
                            "Invalid ANI_ID at line " + lineNumber + ": " + value,
                            e
                        );
                    }
                }
            }
        }
        return Collections.unmodifiableSet(aniIds);
    }

    public static Set<String> ReadRssURLs(String filePath) throws IOException {
        if(filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("RSS URL file path cannot be blank");
        }

        var rssURLs = new LinkedHashSet<String>();
        try(var reader = Files.newBufferedReader(Path.of(filePath))) {
            String line;
            while((line = reader.readLine()) != null) {
                var url = line.trim();
                if(!url.isEmpty()) rssURLs.add(url);
            }
        }
        return Collections.unmodifiableSet(rssURLs);
    }
}
