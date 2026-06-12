package Database;

import java.sql.*;

import Utils.TorrentMetaUtil;


public class TorrentInfo {

    public final String TOR_HASH;     // 种子哈希值，作为唯一标识

    public final String file_name;    // 文件名
    public final Long   file_size;    // 文件大小
    public final byte[] torrent_file; // 种子文件二进制数据


    static PreparedStatement GetUpsertStatement(Connection conn) throws SQLException {
        String upsertSqlFetch =
        """
        INSERT INTO torrent (
            TOR_HASH,
            file_name,
            file_size,
            torrent_file
        )
        VALUES (?, ?, ?, ?)
        ON CONFLICT(TOR_HASH) DO UPDATE SET
            file_name    = excluded.file_name,
            file_size    = excluded.file_size,
            torrent_file = excluded.torrent_file;
        """;
        return conn.prepareStatement(upsertSqlFetch);
    }

    void SetParams(PreparedStatement ps) throws SQLException {
        Utils.safeSetString (ps, 1, TOR_HASH  );
        Utils.safeSetString (ps, 2, file_name );
        Utils.safeSetLong   (ps, 3, file_size );
        Utils.safeSetBytes  (ps, 4, torrent_file      );
    }


    /**
     * 构造 TorrentInfo 实例
     * @param torrent_file 种子文件的二进制数据
     */
    public TorrentInfo(byte[] data) {

        var meta = TorrentMetaUtil.extractMeta(data);

        TOR_HASH  = meta.torHash;
        file_name = meta.fileName;
        file_size = meta.fileSize;
        torrent_file      = data;
    }

    @Override
    public String toString() {
        return "TorrentInfo{"
            + "TOR_HASH='" + TOR_HASH + '\''
            + ", file_name='" + file_name + '\''
            + ", file_size=" + file_size + ", torrent_file=" + (torrent_file != null ? "[binary data]" : "null") + '}';
    }
}
