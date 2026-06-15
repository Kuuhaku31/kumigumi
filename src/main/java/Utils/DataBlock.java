package Utils;


import static Utils.ColorCode.BOLD_CYAN;
import static Utils.ColorCode.BOLD_GREEN;
import static Utils.ColorCode.CYAN;
import static Utils.ColorCode.GREEN;
import static Utils.UtilityFunctions.color;


/**
 * DataBlock（数据块）保存从 Excel #block 指令解析出的表头和行数据。
 */
public class DataBlock implements Utils.Printable {

    private final int header_size;
    private final int row_size;
    private final String data[];


    /**
     * 构造函数，接受一个包含 header 和行数据的一维字符串数组，以及 header 的大小。
     * @param data
     * @param header_size
     */
    public DataBlock(String[] data, int header_size) {

        if(header_size <= 0) throw new IllegalArgumentException("Header size must be greater than 0");
        var data_size = data.length;
        if(data_size % header_size != 0) throw new IllegalArgumentException("Data size must be a multiple of header size");

        this.header_size = header_size;
        this.row_size = data_size / header_size - 1;
        this.data = data;
    }

    public int      GetHeaderSize() { return header_size;  }
    public int      GetRowSize()    { return row_size;     }
    public String[] GetData()       { return data.clone(); }

    // 返回列名在 header 中的索引，如果不存在则返回 -1
    public int GetColumnIndex(String column_name) {

        for(var i = 0; i < header_size; i++) if(data[i].equals(column_name)) return i;

        return -1;
    }

    // 获取 header 部分的数据
    public String[] GetHeader() {

        var header = new String[header_size];
        System.arraycopy(data, 0, header, 0, header_size);

        return header;
    }

    // 根据请求的 header 名称数组返回对应的索引数组，如果某个 header 不存在则对应索引为 -1
    public int[] GetHeaderBy(String[] request_header) {

        var indices = new int[request_header.length];
        for(var i = 0; i < request_header.length; i++) {
            var h_i = GetColumnIndex(request_header[i]);
            indices[i] = h_i;
        }
        return indices;
    }

    // 根据行索引返回对应的数据行，如果索引越界则返回 null
    public String[] GetRow(int row_index) {

        if(row_index < 0 || row_index >= row_size) return null;

        var row = new String[header_size];
        System.arraycopy(data, header_size + row_index * header_size, row, 0, header_size);

        return row;
    }

    // 根据列索引返回对应的数据列，如果索引越界则返回 null
    public String[] GetColumn(int column_index) {

        // 如果列索引越界则返回 null
        if(column_index < 0 || column_index >= header_size) return null;

        var column = new String[row_size];
        for(var i = 0; i < row_size; i++) {
            column[i] = data[header_size + i * header_size + column_index];
        }
        return column;
    }

    // 根据列名返回对应的数据列，如果列名不存在则返回 null
    public String[] GetColumn(String column_name) {
        return GetColumn(GetColumnIndex(column_name));
    }


    @Override
    public String toPrintString(String indent, boolean enable_color) {

        var sb = new StringBuilder();

        var header = GetHeader();
        sb.append(indent + color("Header:\t", BOLD_GREEN, enable_color));
        for(var h : header) {
            sb.append(color(h + "\t", GREEN, enable_color));
        }
        sb.append("\n" + indent);

        var data_row_size = GetRowSize();
        for(var i = 0; i < data_row_size; i++) {
            var row = GetRow(i);
            sb.append(color("Row " + i + ":\t", BOLD_CYAN, enable_color));
            for(var cell : row) {
                sb.append(color(cell + "\t", CYAN, enable_color));
            }
            if(i < data_row_size - 1) sb.append("\n" + indent);
        }

        return sb.toString();
    }

    @Override
    public String toPrintString() {
        return toPrintString("", true);
    }

    @Override
    public String toPrintString(String indent) {
        return toPrintString(indent, true);
    }

    @Override
    public String toPrintString(boolean enable_color) {
        return toPrintString("", enable_color);
    }

    /**
     * 输出 DataBlock（数据块）的表头和行内容，不带颜色。
     */
    @Override
    public String toString() {
        return toPrintString("", false);
    }
}
