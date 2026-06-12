package Utils;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import org.junit.jupiter.api.Test;


class TorrentMetaUtilTest {

    @Test
    void extractsSingleFileTorrentMetadata() throws Exception {
        var info = "d6:lengthi123e4:name8:test.mkv12:piece lengthi16384e6:pieces20:aaaaaaaaaaaaaaaaaaaae";
        var torrent = ("d4:info" + info + "e").getBytes(StandardCharsets.UTF_8);

        var meta = TorrentMetaUtil.extractMeta(torrent);

        assertEquals(sha1(info), meta.torHash);
        assertEquals("test.mkv", meta.fileName);
        assertEquals(123L, meta.fileSize);
    }

    @Test
    void rejectsInvalidTorrentData() {
        assertThrows(IllegalArgumentException.class, () -> TorrentMetaUtil.extractMeta(new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> TorrentMetaUtil.extractMeta("not-bencode".getBytes(StandardCharsets.UTF_8)));
    }

    private static String sha1(String value) throws Exception {
        var digest = MessageDigest.getInstance("SHA-1");
        return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
