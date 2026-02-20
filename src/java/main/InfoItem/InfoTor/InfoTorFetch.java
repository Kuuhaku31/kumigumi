package InfoItem.InfoTor;

import Database.Item.UpdateItem;
import util.TorrentMetaUtil;

public class InfoTorFetch extends InfoTor implements UpdateItem {

    public final String  file_name; // 文件名
    public final Integer file_size; // 文件大小
    public final byte[] file;       // 种子文件二进制数据

    /**
     * 构造 InfoTorFetch 实例
     * @param file 种子文件的二进制数据
     */
    public InfoTorFetch(String torHash, byte[] file) {
        super(torHash);

        var meta       = TorrentMetaUtil.extractMeta(file);
        this.file_name = meta.fileName;
        this.file_size = meta.fileSize;
        this.file      = file;
    }

    @Override
    public String toString() {
        return "InfoTorFetch{"
            + "TOR_HASH='" + TOR_HASH + '\''
            + ", file_name='" + file_name + '\''
            + ", file_size=" + file_size + ", file=" + (file != null ? "[binary data]" : "null") + '}';
    }
}
