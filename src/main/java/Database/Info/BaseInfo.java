package Database.Info;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class BaseInfo {
    public abstract void setParams(PreparedStatement ps) throws SQLException;
}
