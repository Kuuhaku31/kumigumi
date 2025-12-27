package net;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.List;

public class BatchDownloader {

    public static void main(String[] args) {

        // 使用主机代理
        System.setProperty("java.net.useSystemProxies", "true");

        // 1. URL 文件路径（自行修改）
        var urlFile = Paths.get("resources/urls.txt");

        // 2. 用户默认下载目录
        var downloadDir = Paths.get("D:/Downloads/dt");

        try {
            if (!Files.exists(downloadDir)) {
                Files.createDirectories(downloadDir);
            }

            List<String> urls = Files.readAllLines(urlFile);

            for (String line : urls) {
                String urlStr = line.trim();
                if (urlStr.isEmpty()) {
                    continue;
                }

                downloadFile(urlStr, downloadDir);
            }

            System.out.println("全部下载完成");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void downloadFile(String urlStr, Path downloadDir) {
        try {
            var url = URI.create(urlStr).toURL();

            // 从 URL 中提取文件名
            String fileName = Paths.get(url.getPath()).getFileName().toString();
            if (fileName.isEmpty()) {
                fileName = "unknown.file";
            }

            var target = downloadDir.resolve(fileName);

            try (InputStream in = url.openStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println("已下载: " + fileName);

        } catch (Exception e) {
            System.err.println("下载失败: " + urlStr);
            e.printStackTrace();
        }
    }
}
