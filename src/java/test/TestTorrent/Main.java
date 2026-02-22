package TestTorrent;

import TestDatabase.ARGS;
import util.TorrentMetaUtil;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, TestTorrent!");

        var file = ARGS.TOR_PATH_1;
        System.out.println("File path: " + file);

        var meta = TorrentMetaUtil.extractMeta(ARGS.TOR_FILE_BIN_1);

        System.out.println("Extracted metadata:");
        System.out.println("File Name: " + meta.fileName);
        System.out.println("File Size: " + meta.fileSize);
    }
}
