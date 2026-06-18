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
        if(args.length != 2 || args[0].isBlank() || args[1].isBlank()) {
            throw new IllegalArgumentException("Usage: Utils.CreateDatabaseViews <database-path> <ani-id-file>");
        }

        var aniIds = ReadAnimeIds(args[1]);
        Create(args[0], aniIds);
        System.out.println("required_anime_id 更新完成: " + args[0] + "，ANI_ID 数量: " + aniIds.size());
    }

    public static void Create(String databasePath, String aniIdFilePath) throws IOException, SQLException {
        Create(databasePath, ReadAnimeIds(aniIdFilePath));
    }

    public static void Create(String databasePath, Set<Integer> aniIds) throws SQLException {
        if(databasePath == null || databasePath.isBlank()) {
            throw new IllegalArgumentException("Database path cannot be blank");
        }
        if(aniIds == null) throw new IllegalArgumentException("ANI_ID set cannot be null");

        try(var db = new SQLiteAccess(databasePath)) {
            db.ReplaceRequiredAnimeIds(aniIds);
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
}
