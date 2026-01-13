package net;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public class BatchDownloader {

    // 最大重试次数
    private static final int MAX_RETRY = 3;

    // 缓冲区大小
    private static final int BUFFER_SIZE = 8192;

    // 失败 URL 记录文件
    private static String failed_url_file_path = null;

    public static void main(String[] args) {

        String url_file_path = null;
        if (args.length > 1) {
            url_file_path = args[0];
            failed_url_file_path = args[1];
        } else {
            System.out.println("请提供包含 URL 列表的文件路径和失败 URL 记录文件路径作为参数");
            return;
        }

        Path urlFile = Paths.get(url_file_path); // 存放 URL 的文件
        Path downloadDir = Paths.get(System.getProperty("user.home"), "Downloads");

        try {
            Files.createDirectories(downloadDir);
            List<String> urls = Files.readAllLines(urlFile, StandardCharsets.UTF_8);

            for (String url : urls) {
                if (url.isBlank())
                    continue;
                downloadWithRetry(url.trim(), downloadDir);
            }

            System.out.println("下载任务完成");

        } catch (IOException e) {
            System.err.println("程序初始化失败");
            e.printStackTrace();
        }
    }

    /**
     * 带重试机制的下载
     */
    private static void downloadWithRetry(String fileUrl, Path downloadDir) {
        for (int i = 1; i <= MAX_RETRY; i++) {
            try {
                downloadFile(fileUrl, downloadDir);
                System.out.println("下载成功: " + fileUrl);
                return;
            } catch (Exception e) {
                System.err.println("第 " + i + " 次下载失败: " + fileUrl);
                if (i == MAX_RETRY) {
                    recordFailedUrl(fileUrl);
                }
            }
        }
    }

    /**
     * 实际下载逻辑
     */
    private static void downloadFile(String fileUrl, Path downloadDir) throws IOException {
        URI uri;
        try {
            uri = new URI(fileUrl);
        } catch (URISyntaxException e) {
            System.err.println("URL 语法错误: " + fileUrl);
            throw new IOException("无效的 URL: " + fileUrl, e);
        }
        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(15_000);
        conn.setUseCaches(false);

        // 关键：设置 User-Agent
        conn.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK &&
                responseCode != HttpURLConnection.HTTP_PARTIAL) {
            throw new IOException("HTTP 响应码异常: " + responseCode);
        }

        String fileName = extractFileName(url);
        Path targetFile = downloadDir.resolve(fileName);

        try (
                InputStream in = new BufferedInputStream(conn.getInputStream());
                OutputStream out = new BufferedOutputStream(
                        Files.newOutputStream(targetFile,
                                StandardOpenOption.CREATE,
                                StandardOpenOption.TRUNCATE_EXISTING))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } finally {
            conn.disconnect();
        }
    }

    /**
     * 从 URL 中提取文件名
     */
    private static String extractFileName(URL url) {
        String path = url.getPath();
        int idx = path.lastIndexOf('/');
        return (idx >= 0) ? path.substring(idx + 1) : "unknown.file";
    }

    /**
     * 记录失败 URL
     */
    private static void recordFailedUrl(String url) {
        try {
            Files.writeString(
                    Paths.get(failed_url_file_path),
                    url + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("无法记录失败 URL: " + url);
        }
    }
}
