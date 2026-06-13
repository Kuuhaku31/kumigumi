package Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class Info {
    abstract void setParams(PreparedStatement ps) throws SQLException;
}
