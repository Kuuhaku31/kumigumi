package util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TorrentMetaUtil {

    public static final class TorrentMeta {
        public final String  fileName;
        public final Integer fileSize;

        public TorrentMeta(String fileName, Integer fileSize) {
            this.fileName = fileName;
            this.fileSize = fileSize;
        }
    }

    public static TorrentMeta extractMeta(byte[] file) {
        if(file == null || file.length == 0) {
            throw new IllegalArgumentException("torrent 文件内容不能为空");
        }

        var    cursor = new BencodeCursor(file);
        Object root   = parseBencodeValue(cursor);

        if(!(root instanceof Map)) {
            throw new IllegalArgumentException("无效的 torrent 文件：根节点不是字典");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> rootMap = (Map<String, Object>)root;
        Object              infoObj = rootMap.get("info");
        if(!(infoObj instanceof Map)) {
            throw new IllegalArgumentException("无效的 torrent 文件：缺少 info 字典");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> infoMap = (Map<String, Object>)infoObj;

        String  fileName = decodeUtf8(infoMap.get("name"));
        Integer fileSize = extractFileSize(infoMap);
        return new TorrentMeta(fileName, fileSize);
    }

    private static Integer extractFileSize(Map<String, Object> infoMap) {
        Long singleLength = asLong(infoMap.get("length"));
        if(singleLength != null) {
            return longToInteger(singleLength);
        }

        Object filesObj = infoMap.get("files");
        if(!(filesObj instanceof List<?> filesList)) {
            return null;
        }

        long total = 0L;
        for(Object fileObj : filesList) {
            if(!(fileObj instanceof Map<?, ?> fileMapRaw)) {
                continue;
            }
            Object lengthObj  = fileMapRaw.get("length");
            Long   fileLength = asLong(lengthObj);
            if(fileLength != null) {
                total += fileLength;
            }
        }

        return longToInteger(total);
    }

    private static Integer longToInteger(long value) {
        try {
            return Math.toIntExact(value);
        } catch(ArithmeticException ex) {
            return null;
        }
    }

    private static Long asLong(Object value) {
        if(value instanceof Long longValue) {
            return longValue;
        }
        return null;
    }

    private static String decodeUtf8(Object value) {
        if(value instanceof byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return null;
    }

    private static Object parseBencodeValue(BencodeCursor cursor) {
        byte marker = cursor.peek();

        if(marker == 'd') {
            cursor.next();
            Map<String, Object> result = new LinkedHashMap<>();
            while(cursor.peek() != 'e') {
                String key   = decodeUtf8(parseByteString(cursor));
                Object value = parseBencodeValue(cursor);
                result.put(key, value);
            }
            cursor.next();
            return result;
        }

        if(marker == 'l') {
            cursor.next();
            List<Object> list = new ArrayList<>();
            while(cursor.peek() != 'e') {
                list.add(parseBencodeValue(cursor));
            }
            cursor.next();
            return list;
        }

        if(marker == 'i') {
            cursor.next();
            long value = parseLongUntil(cursor, 'e');
            cursor.next();
            return value;
        }

        if(marker >= '0' && marker <= '9') {
            return parseByteString(cursor);
        }

        throw new IllegalArgumentException("无效的 bencode 数据：未知标记 " + (char)marker);
    }

    private static byte[] parseByteString(BencodeCursor cursor) {
        long length = parseLongUntil(cursor, ':');
        cursor.next();

        if(length < 0 || length > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("无效的字符串长度：" + length);
        }

        int len = (int)length;
        if(cursor.remaining() < len) {
            throw new IllegalArgumentException("无效的 bencode 数据：字符串超出边界");
        }

        byte[] value = new byte[len];
        for(int i = 0; i < len; i++) {
            value[i] = cursor.next();
        }
        return value;
    }

    private static long parseLongUntil(BencodeCursor cursor, char terminator) {
        boolean negative = false;
        if(cursor.peek() == '-') {
            negative = true;
            cursor.next();
        }

        long    value    = 0L;
        boolean hasDigit = false;
        while(cursor.peek() != terminator) {
            byte digit = cursor.next();
            if(digit < '0' || digit > '9') {
                throw new IllegalArgumentException("无效的数字编码");
            }
            hasDigit = true;
            value    = value * 10 + (digit - '0');
        }

        if(!hasDigit) {
            throw new IllegalArgumentException("无效的数字编码：缺少数字");
        }
        return negative ? -value : value;
    }

    private static final class BencodeCursor {
        private final byte[] data;
        private int          index;

        private BencodeCursor(byte[] data) {
            this.data  = data;
            this.index = 0;
        }

        private byte peek() {
            if(index >= data.length) {
                throw new IllegalArgumentException("无效的 bencode 数据：意外结束");
            }
            return data[index];
        }

        private byte next() {
            byte current = peek();
            index++;
            return current;
        }

        private int remaining() {
            return data.length - index;
        }
    }
}
