package Database;

import utils.TableData.TableData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public
class KG_SQLiteAccess
{
    /* -----------------------------------------------------------------------
       表结构注册：不同表包含的列和主键
     ----------------------------------------------------------------------- */
    private static final Map<TableName, TableMeta> META = Map.of(
        TableName.anime,
        new TableMeta(
            new DBStructure.Headers[] {DBStructure.Headers.ANI_ID},
            DBStructure.ANIME_HEADERS
        ),

        TableName.episode,
        new TableMeta(
            new DBStructure.Headers[] {DBStructure.Headers.EPI_ID},
            DBStructure.EPISODE_HEADERS
        ),

        TableName.torrent,
        new TableMeta(
            new DBStructure.Headers[] {DBStructure.Headers.TOR_URL},
            DBStructure.TORRENT_HEADERS
        )
    );
    private static       Connection                conn = null;

    public static
    void Open()
    {
        String url = "jdbc:sqlite:D:/db/st-sqlite/kumigumi.db";
        try
        {
            conn = DriverManager.getConnection(url);
        }
        catch(SQLException e)
        {
            System.err.println("Open failed: " + e.getMessage());
        }
    }

    public static
    boolean isOpen()
    {
        return conn != null;
    }

    public static
    void Close()
    {
        try
        {
            if(conn != null) conn.close();
        }
        catch(SQLException e)
        {
            System.err.println("Close failed: " + e.getMessage());
        }
        conn = null;
    }

    /* -----------------------------------------------------------------------
       单条 Upsert（Map 数据）
     ----------------------------------------------------------------------- */
    public static
    void Upsert(TableName table, Map<String, String> record) throws SQLException
    {
        checkOpen();

        TableMeta meta = META.get(table);
        if(meta == null)
        {
            System.err.println("Unknown table: " + table);
            return;
        }

        // 主键检查
        for(var pk : meta.primaryKeys)
        {
            if(!record.containsKey(pk.toString()))
            {
                System.err.println("Primary key missing: " + pk);
                return;
            }
        }

        // 提取合法字段
        List<DBStructure.Headers> cols   = new ArrayList<>();
        List<String>              values = new ArrayList<>();

        for(var header : meta.headers)
        {
            String key = header.toString();
            if(record.containsKey(key))
            {
                cols.add(header);
                values.add(record.get(key));
            }
        }

        if(cols.isEmpty())
        {
            System.err.println("No valid columns to upsert.");
            return;
        }

        // 构建 SQL
        SQLParts sql = buildSQL(table, meta.primaryKeys, cols);

        try(var stmt = conn.prepareStatement(sql.sql))
        {
            for(int i = 0; i < values.size(); i++)
            {
                stmt.setString(i + 1, values.get(i));
            }
            stmt.executeUpdate();
        }
    }

    /* -----------------------------------------------------------------------
       批量 Upsert（TableData）
     ----------------------------------------------------------------------- */
    public static
    void Upsert(TableName table, TableData data) throws SQLException, UpsertException
    {
        checkOpen();

        var meta = META.get(table);
        if(meta == null)
        {
            throw new IllegalArgumentException("Unknown table: " + table);
        }

        // 检查主键是否存在
        for(var pk : meta.primaryKeys)
        {
            if(data.GetHeaderIndex(pk.toString()) == -1)
            {
                throw new IllegalArgumentException("Primary key missing: " + pk);
            }
        }

        // 提取合法列
        List<DBStructure.Headers> cols     = new ArrayList<>();
        Set<String>               incoming = new HashSet<>(List.of(data.GetHeaders()));

        for(var h : meta.headers) if(incoming.contains(h.toString())) cols.add(h);

        SQLParts sql = buildSQL(table, meta.primaryKeys, cols);

        try(var stmt = conn.prepareStatement(sql.sql))
        {
            conn.setAutoCommit(false);

            var rows     = data.GetData();
            int rowIndex = 0;

            for(; rowIndex < rows.size(); rowIndex++)
            {
                var row = rows.get(rowIndex);

                try
                {
                    for(int c = 0; c < cols.size(); c++)
                    {
                        stmt.setString(c + 1, row[c]);
                    }

                    stmt.executeUpdate();  // 不用 batch 以便精准定位
                }
                catch(SQLException ex)
                {
                    conn.rollback();
                    throw new UpsertException("Upsert failed at row " + rowIndex, rowIndex, row, ex);
                }
            }
            conn.commit();
        }
        catch(UpsertException ue)
        {
            throw ue;    // 继续向上抛给上层
        }
        catch(Exception e)
        {
            conn.rollback();
            throw e;
        }
        finally
        {
            conn.setAutoCommit(true);
        }
    }

    private static
    SQLParts buildSQL(
        TableName table,
        DBStructure.Headers[] primaryKeys,
        List<DBStructure.Headers> cols
    )
    {

        String colNames     = joinCols(cols);
        String placeholders = String.join(", ", Collections.nCopies(cols.size(), "?"));
        String pkStr        = joinCols(Arrays.asList(primaryKeys));

        String updatePart = String.join(", ",
            cols.stream()
                .map(h -> "`" + h + "` = excluded.`" + h + "`")
                .toArray(String[]::new)
        );

        String sql =
            "INSERT INTO `" + table + "` (" + colNames + ")\n" +
                "VALUES (" + placeholders + ")\n" +
                "ON CONFLICT (" + pkStr + ") DO UPDATE SET " + updatePart;

        return new SQLParts(sql);
    }

    private static
    String joinCols(List<DBStructure.Headers> cols)
    {
        return String.join(", ",
            cols.stream().map(c -> "`" + c + "`").toArray(String[]::new)
        );
    }

    private static
    void checkOpen()
    {
        if(conn == null)
            throw new IllegalStateException("Database not opened.");
    }

    /* -----------------------------------------------------------------------
       表名枚举
     ----------------------------------------------------------------------- */
    public
    enum TableName
    {
        anime, episode, torrent;

        public static
        TableName Get(String value)
        {
            try { return TableName.valueOf(value); }
            catch(Exception e) { return null; }
        }
    }

    private
    record TableMeta(DBStructure.Headers[] primaryKeys, DBStructure.Headers[] headers) { }

    /* -----------------------------------------------------------------------
       SQL 构建辅助结构
     ----------------------------------------------------------------------- */
    private
    record SQLParts(String sql) { }
}
