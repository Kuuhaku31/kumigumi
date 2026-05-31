package Database;

import java.sql.SQLException;

public class TestInitDB {
     public static void main(String[] args) throws SQLException {
        var cwd = System.getProperty("user.dir").replace("\\", "/");
        System.out.println("当前工作目录: " + cwd);
        SQLiteInit.initDatabase("jdbc:sqlite:" + cwd + "/db/kumigumi_test2.db");
    }
}
