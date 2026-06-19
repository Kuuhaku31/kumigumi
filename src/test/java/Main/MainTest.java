package Main;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import Utils.DataBlock;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {

    @TempDir
    Path tempDir;

    @Test
    void registersDatabaseViewCommands() {
        assertEquals(ExcelCommand.FLUSH_DB_VIEW, ExcelCommand.fromString("_flush_db_view"));
        assertEquals(ExcelCommand.FLUSH_DB_VIEW_REQUIRED, ExcelCommand.fromString("_flush_db_view_required"));
    }

    @Test
    void replacesViewFiltersFromDataBlockAndRefreshesViews() throws Exception {
        var dbPath = tempDir.resolve("view-command-test.db").toString();
        var mainApp = new MainApplication(new String[] {
            "--database_path", dbPath,
            "--excel_file_path", tempDir.resolve("unused.xlsx").toString(),
            "--log_path", tempDir.resolve("logs").toString() + "/",
            "--export_dir", tempDir.resolve("export").toString()
        });

        var dataBlock = new DataBlock(new String[] {
            "ANI_ID", "URL_RSS",
            "100", "https://example.com/feed-1.xml",
            "100", "https://example.com/feed-1.xml",
            "invalid", " ",
            "200", "https://example.com/feed-2.xml"
        }, 2);
        mainApp.variables.put("viewFilters", new DataBlockItem(dataBlock));

        Commands.flushDBViewRequired(mainApp, List.of("_flush_db_view_required", "viewFilters"));
        Commands.flushDBView(mainApp, List.of("_flush_db_view"));

        try(var conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            var stmt = conn.createStatement()) {
            var aniIDs = new HashSet<Integer>();
            try(var rs = stmt.executeQuery("SELECT ANI_ID FROM required_anime_id")) {
                while(rs.next()) aniIDs.add(rs.getInt("ANI_ID"));
            }
            assertEquals(Set.of(100, 200), aniIDs);

            var rssURLs = new HashSet<String>();
            try(var rs = stmt.executeQuery("SELECT URL_RSS FROM required_rss")) {
                while(rs.next()) rssURLs.add(rs.getString("URL_RSS"));
            }
            assertEquals(
                Set.of("https://example.com/feed-1.xml", "https://example.com/feed-2.xml"),
                rssURLs
            );

            try(var rs = stmt.executeQuery(
                "SELECT count(*) FROM sqlite_schema "
                + "WHERE type = 'view' AND name IN ('view_anime', 'view_episode', 'view_torrent_page')"
            )) {
                assertTrue(rs.next());
                assertEquals(3, rs.getInt(1));
            }
        }
    }

    @Test
    void clearsViewFiltersWhenNoDataBlockIsProvided() throws Exception {
        var dbPath = tempDir.resolve("clear-view-filter-test.db").toString();
        var mainApp = new MainApplication(new String[] {
            "--database_path", dbPath,
            "--excel_file_path", tempDir.resolve("unused.xlsx").toString(),
            "--log_path", tempDir.resolve("logs").toString() + "/",
            "--export_dir", tempDir.resolve("export").toString()
        });

        var dataBlock = new DataBlock(new String[] {
            "ANI_ID", "URL_RSS",
            "100", "https://example.com/feed.xml"
        }, 2);
        mainApp.variables.put("viewFilters", new DataBlockItem(dataBlock));
        Commands.flushDBViewRequired(mainApp, List.of("_flush_db_view_required", "viewFilters"));
        Commands.flushDBViewRequired(mainApp, List.of("_flush_db_view_required"));

        try(var conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            var stmt = conn.createStatement()) {
            try(var rs = stmt.executeQuery("SELECT count(*) FROM required_anime_id")) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1));
            }
            try(var rs = stmt.executeQuery("SELECT count(*) FROM required_rss")) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1));
            }
        }
    }
}
