package InfoItem.InfoTor;

import util.TorrentMetaUtil;

public class InfoTorUpsert {

    public final String  file_name; // 文件名
    public final Integer file_size; // 文件大小
    public final byte[] file;       // 种子文件二进制数据

    public InfoTorUpsert(byte[] file) {
        this.file      = file;
        var meta       = TorrentMetaUtil.extractMeta(file);
        this.file_name = meta.fileName;
        this.file_size = meta.fileSize;
    }

    @Override
    public String toString() {
        return "InfoTorUpsert{"
            + "file_name='" + file_name + '\'' + ", file_size=" + file_size + ", file=" + (file != null ? "[binary data]" : "null") + '}';
    }
}
