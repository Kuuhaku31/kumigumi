package Excel;

import java.util.ArrayList;
import java.util.List;

class ExcelDataBlock {
    public final String blockName;
    private final String[] headers;
    private final List<Object[]> data;

    ExcelDataBlock(String blockName, List<String> headers) {
        this.blockName = new String(blockName);
        this.headers = removeSame(headers);
        this.data = new ArrayList<>();
    }

    /** 通过行号和列名获取值 */
    Object getValue(int rowIndex, String header) {
        if (rowIndex < 0 || rowIndex >= data.size()) {
            return null;
        }

        var colIndex = -1;
        for (var i = 0; i < headers.length; ++i) {
            if (headers[i].equals(header)) {
                colIndex = i;
                break;
            }
        }
        if (colIndex == -1) {
            return null;
        } else
            return data.get(rowIndex)[colIndex];
    }

    @Override
    public String toString() {
        var title = "===" + this.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(this))
                + "===";
        StringBuilder sb = new StringBuilder();
        sb.append(title).append("\n");
        sb.append("Block Name: ").append(blockName).append("\n");
        sb.append("Headers: ").append(headers).append("\n");
        sb.append("Data: ").append(data).append("\n");
        sb.append("=".repeat(title.length()));
        return sb.toString();
    }

    /** 记录类 */
    public class Record {
        private final Object[] values;

        public Record() {
            values = new Object[headers.length];
            data.add(values);
        }

        // 添加记录
        public void Set(String header, Object value) {
            var colIndex = -1;
            for (var i = 0; i < headers.length; ++i) {
                if (headers[i].equals(header)) {
                    colIndex = i;
                    break;
                }
            }
            if (colIndex == -1)
                return;
            values[colIndex] = value;
        }

        @Override
        public String toString() {
            var sb = new StringBuilder();
            sb.append("Record{");
            sb.append("Block=").append(blockName).append(", ");
            for (var i = 0; i < headers.length; ++i) {
                sb.append(headers[i]).append("=").append(values[i]);
                if (i < headers.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("}");
            return sb.toString();
        }
    }

    private static String[] removeSame(List<String> list) {
        var result = new ArrayList<String>();
        for (var item : list) {
            if (!result.contains(item)) {
                result.add(item);
            }
        }
        return result.toArray(new String[0]);
    }
}
