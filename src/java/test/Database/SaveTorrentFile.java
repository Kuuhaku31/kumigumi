package Database;

import java.util.List;

import MetaData.ARGS;

public class SaveTorrentFile {
    public static void main(String[] args) {
        var tor_hash = "ea5e686c111c47dcadbd16f335d7e7d79e48563f";

        try(var db = new SQLiteAccess(ARGS.DATABASE_PATH)) {
            db.exportTorrentFiles(List.of(tor_hash), "./");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
