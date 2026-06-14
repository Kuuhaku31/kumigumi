package Database.Info;

import java.sql.*;

import Utils.DatabaseUtils;
import Utils.TorrentMetaUtil;


public class TorrentInfo extends BaseInfo {

    public final String TOR_HASH;     // 种子哈希值，作为唯一标识

    public final String file_name;    // 文件名
    public final Long   file_size;    // 文件大小
    public final byte[] torrent_file; // 种子文件二进制数据

    @Override
    public void setParams(PreparedStatement ps) throws SQLException {
        DatabaseUtils.safeSetString (ps, 1, TOR_HASH    );
        DatabaseUtils.safeSetString (ps, 2, file_name   );
        DatabaseUtils.safeSetLong   (ps, 3, file_size   );
        DatabaseUtils.safeSetBytes  (ps, 4, torrent_file);
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
    public String toPrintString(String indent, boolean enable_color) {
        return formatInfo("TorrentInfo", indent, enable_color, new Object[][] {
            { "TOR_HASH", TOR_HASH },
            { "file_name", file_name },
            { "file_size", file_size },
            { "torrent_file", torrent_file }
        });
    }

    @Override
    public String toString() {
        return toPrintString("", false);
    }
}
