package net;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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
        HttpURLConnection conn = null;
        try {
            var url = URI.create(urlStr).toURL();
            conn = (HttpURLConnection) url.openConnection();

            // 必须设置的请求头
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);

            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Connection", "keep-alive");

            var code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP 状态码异常: " + code);
            }

            var fileName = Paths.get(url.getPath()).getFileName().toString();
            var target = downloadDir.resolve(fileName);

            try (InputStream in = conn.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println("下载成功: " + fileName);

        } catch (Exception e) {
            System.err.println("下载失败: " + urlStr);
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

}
