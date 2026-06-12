package Excel;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Utils.ColorCode;
import org.apache.poi.ss.usermodel.DateUtil;

import static Utils.UtilityFunctions.color;


final class ExcelReadContext {
    private Map<String, List<List<String>>> excelData; // sheet名 -> 二维字符串数据

    private ExcelCursor cursor; // 光标位置

    private Map<String, String>    variables;     // 定义的变量
    private List<List<String>>     commands;      // 保存命令列表
    private Map<String, TableData> blockDataList; // 保存块信息

    private String                     blockName    = null;            // 当前块名称
    private BlockMetaData              tmpMetaData  = null;            // 解析过程中使用的元数据
    private Map<String, BlockMetaData> metaDataList = new HashMap<>(); // 解析过程中使用的元数据列表

    private boolean isReading = true;

    ExcelReadContext(Map<String, List<List<String>>> excelData) {

        this.excelData     = excelData;
        this.cursor        = new ExcelCursor(0, 0, "main");
        this.variables     = new HashMap<>();
        this.commands      = new ArrayList<>();
        this.blockDataList = new HashMap<>();
    }

    ExcelResult parse() throws IOException {

        // 遍历所有行
        System.out.println(color("#Start Reading Data.", ColorCode.GREEN));
        while(isReading) {

            // 获取当前行第一个单元格数据
            var cellData = getCell(0);

            // 如果遇到空单元格，结束该行读取
            if(cellData == null) cursor.gotoNextRow();

            // 如果单元格数据以 "#" 开头，则进行预处理
            else if(cellData.startsWith("#")) preprocessing(cellData);

            // 如果正在解析块，添加列信息到块元数据中
            else if(tmpMetaData != null) {
                var header = cellData;
                var colIdx = (int)Double.parseDouble(getCell(1)) - 1;
                var type   = getCell(2);
                tmpMetaData.addColumn(header, type, colIdx);
                cursor.gotoNextRow();
            }

            // 否则将该行数据作为命令保存到 commands 中
            else {
                // 依次读取每一个单元格
                List<String> row_data = new ArrayList<>();
                for(var col_index = 0; ; col_index++) {
                    var cell_value = getCell(col_index);
                    if(cell_value == null) {
                        cursor.gotoNextRow();
                        break;
                    } // 遇到空单元格则结束该行读取
                    row_data.add(cell_value);
                }
                if(row_data.size() != 0) commands.add(row_data);
                System.out.println(color("Read Command: " + row_data, ColorCode.GREEN));
            }
        }

        System.out.println();

        // 创建块数据
        System.out.println(color("开始通过解析的元数据创建表", ColorCode.GREEN));
        for(var entry : metaDataList.entrySet()) {

            var blockName = color(entry.getKey(), ColorCode.BOLD_MAGENTA);
            var blockMeta = entry.getValue();
            System.out.println(color("Creating block: " + blockName + " with metadata:", ColorCode.GREEN));
            System.out.println(blockMeta.toPrintString("  "));

            try {
                create_table_data(blockName, blockMeta);
                System.out.println(color("Success", ColorCode.GREEN));
                System.out.println();
            }
            catch(CreateTableException e) {
                System.out.println(color("Failed with error:", ColorCode.BOLD_RED));
                System.out.println(e.getMessage());
            }
        }

        // 返回结果
        return new ExcelResult(variables, commands, blockDataList);
    }

    /**
     * 创建表格数据对象
     */
    private void create_table_data(String tableName, BlockMetaData tableMeta) throws CreateTableException {

        // 参数检查
        if(tableMeta.sheetName == null || tableMeta.startRow == null || tableMeta.endRow == null) {

            var msg = color("Missing required metadata for block: \n", ColorCode.BOLD_RED);
            if(tableMeta.sheetName == null) msg += color("sheet_name  ", ColorCode.RED);
            if(tableMeta.startRow  == null) msg += color("start_row  ", ColorCode.RED);
            if(tableMeta.endRow    == null) msg += color("end_row", ColorCode.RED);

            throw new CreateTableException(msg);
        }

        // 获取工作表数据
        var sheet = excelData.get(tableMeta.sheetName);
        if(sheet == null) {
            var msg = "Sheet not found: " + tableMeta.sheetName + " for table: " + tableName;
            throw new CreateTableException(msg);
        }

        // 获取表头
        var headerMetaList = new ArrayList<>(tableMeta.headerToColIndex.entrySet()); // 获取表头元数据列表
        var headers        = new String[headerMetaList.size()];
        for(var i = 0; i < headerMetaList.size(); i++) headers[i] = headerMetaList.get(i).getKey();

        var tableValues = new ArrayList<String>();
        for(var header : headers) tableValues.add(header);

        // 遍历表格每一行
        for(var sheet_row_idx = tableMeta.startRow; sheet_row_idx < tableMeta.endRow; sheet_row_idx++) {
            // 遍历每一列（仅考虑 column_list 中定义的列）
            for(var columnIndex = 0; columnIndex < headerMetaList.size(); columnIndex++) {
                var column_map = headerMetaList.get(columnIndex);

                String cell_value;
                cell_value = getCell(tableMeta.sheetName, sheet_row_idx, column_map.getValue().col()); // 提取单元格值
                cell_value = parse_string(cell_value, StringType.FromString(column_map.getValue().type())); // 解析出显示值

                tableValues.add(cell_value);
            }
        }
        blockDataList.put(tableName, new TableData(tableValues.toArray(String[]::new), headers.length));
    }

    private void preprocessing(String cellData) {

        switch(cellData) {

        // 结束读取
        case "#end" -> {
            System.out.println(color("#End of Data.", ColorCode.GREEN));
            isReading = false;
            break;
        }

        // 跳转到指定位置
        case "#goto" -> { jump(0); break; }

        // 定义变量
        case "#define" -> {

            // 读取变量名和值（但不处理）
            var var_name  = getCell(1);
            var var_value = getCell(2);
            if(var_name != null) {
                var_name = var_name.trim();
                if(var_value != null) var_value = var_value.trim();
                variables.put(var_name, var_value);
                System.out.println(color("#Define Variable: " + var_name + " = " + var_value, ColorCode.YELLOW));
            }

            cursor.gotoNextRow();
            break;
        }

        // 创建 TableData
        case "#block" -> {

            var blockName = getCell(1);
            if(blockName == null) {
                var msg = "Block name is required after #block directive";
                msg += " at " + cursor;
                System.out.println(color(msg, ColorCode.RED));
            }
            else {
                this.blockName = blockName;
                tmpMetaData    = new BlockMetaData();
            }

            cursor.gotoNextRow();
            break;
        }

        case "#block_end" -> {

            if(tmpMetaData == null) {
                var msg = "No block metadata found for #block_end directive";
                msg += " at " + cursor;
                System.out.println(color(msg, ColorCode.RED));
            }
            else {
                metaDataList.put(blockName, tmpMetaData);
                blockName  = null;
                tmpMetaData = null;
            }

            cursor.gotoNextRow();
            break;
        }

        case "#from", "#to", "#sheet" -> {

            if(tmpMetaData == null) {
                var msg = "No block metadata found for " + cellData + " directive";
                msg += " at " + cursor;
                System.out.println(color(msg, ColorCode.RED));
            }
            else {
                var value = getCell(1);
                if(value == null) {
                    var msg = "Value is required after " + cellData + " directive";
                    msg += " at " + cursor;
                    System.out.println(color(msg, ColorCode.RED));
                }
                else switch(cellData) {
                case "#from"  -> tmpMetaData.startRow = (int)Double.parseDouble(value) - 1;
                case "#to"    -> tmpMetaData.endRow   = (int)Double.parseDouble(value) - 1;
                case "#sheet" -> tmpMetaData.sheetName = value;
                }
            }

            cursor.gotoNextRow();
            break;
        }

        // 条件跳转
        case "#goto_if" -> {
            var var_name = getCell(1);
            if(variables.containsKey(var_name)) jump(1);
            else cursor.gotoNextRow();
        }

        // 其他情况继续读取下一行
        default -> {
            var msg = "Unknown directive: " + cellData + ". Skipping.";
            System.out.println(color(msg, ColorCode.YELLOW));
            cursor.gotoNextRow();
        }

        }
    }

    private void jump(int dx) {
        // 读取目标位置
        var target_row = getCell(dx + 1);
        var target_col = getCell(dx + 2);
        var r          = (int)Double.parseDouble(target_row) - 1;
        var c          = (int)Double.parseDouble(target_col) - 1;
        var newSheet   = getCell(dx + 3);

        cursor.gotoPosition(r, c, newSheet);
        System.out.println(color("#Goto Position: (" + r + ", " + c + ") in Sheet: " + newSheet, ColorCode.GREEN));
    }

    private String getCell(int dx) {
        return getCell(cursor.sheetName, cursor.row, cursor.col + dx);
    }

    private String getCell(String sheetName, int rowIndex, int colIndex) {

        var sheet = excelData.get(sheetName);
        if(sheet == null) return null;

        if(rowIndex < 0 || rowIndex >= sheet.size()) return null;

        var row = sheet.get(rowIndex);
        if(row == null) return null;

        if(colIndex < 0 || colIndex >= row.size()) return null;

        return row.get(colIndex);
    }

    // 解析字符串
    private static String parse_string(String value, StringType type) {
        // 如果为空白串，则返回 null
        if(value == null || value.isEmpty()) return null;
        return switch(type) {
            case Bool -> value.equals("0") ? "FALSE" : "TRUE";
            case Text -> value;
            default -> {
                try {
                    var double_value = Double.parseDouble(value);
                    if(type == StringType.Int)
                        yield String.valueOf((int)double_value);

                    var date     = DateUtil.getJavaDate(double_value);
                    var datetime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());

                    var pattern = switch(type) {
                        case Date -> "yyyy-MM-dd";
                        case Time -> "HH:mm:ss";
                        case Datetime -> "yyyy-MM-dd'T'HH:mm:ss";
                        default -> null;
                    };
                    var   fmt = DateTimeFormatter.ofPattern(pattern);
                    yield datetime.format(fmt);
                } catch(Exception e) {
                    yield null; // 解析失败则返回空
                }
            }
        };
    }

    private enum StringType {
        Int,
        Date,
        Time,
        Datetime,
        Bool,
        Text;

        static StringType FromString(String str) {
            if(str == null)
                return Text;
            return switch(str.toLowerCase()) {
                case "int" -> Int;
                case "date" -> Date;
                case "time" -> Time;
                case "datetime" -> Datetime;
                case "bool" -> Bool;
                default -> Text;
            };
        }
    }

    private static final class ExcelCursor {
        String sheetName = "main";
        int    row       = 0;
        int    col       = 0;

        ExcelCursor(int r, int c, String name) {
            row       = r;
            col       = c;
            sheetName = name;
        }

        void gotoPosition(int r, int c, String name) {
            row       = r;
            col       = c;
            sheetName = name == null ? sheetName : name;
        }

        void gotoNextRow() {
            row++;
        }


        // 返回类似 main!C9
        @Override
        public String toString() {
            return sheetName + "!" + (char)('A' + col) + (row + 1);
        }
    }

    private static final class BlockMetaData {

        String  sheetName = null; // 工作表名称
        Integer startRow  = null; // 起始行
        Integer endRow    = null; // 结束行

        private final Map<String, pair> headerToColIndex = new HashMap<>();

        void addColumn(String header, String type, Integer col) {
            headerToColIndex.put(header, new pair(type, col));
        }

        public String toPrintString(String indent) {

            final var this_indent = "  ";

            var sb = new StringBuilder();

            sb.append(indent);
            sb.append(color("Sheet Name: ", ColorCode.BOLD_GREEN));
            sb.append(color(String.valueOf(sheetName), ColorCode.GREEN));
            sb.append("\n");

            sb.append(indent);
            sb.append(color("Start Row: ", ColorCode.BOLD_GREEN));
            sb.append(color(String.valueOf(startRow), ColorCode.GREEN));
            sb.append("\n");

            sb.append(indent);
            sb.append(color("End Row: ", ColorCode.BOLD_GREEN));
            sb.append(color(String.valueOf(endRow), ColorCode.GREEN));
            sb.append("\n");

            sb.append(indent);
            sb.append(color("Columns:", ColorCode.BOLD_CYAN));
            if(headerToColIndex.isEmpty()) return sb.toString();

            sb.append("\n");
            var it = headerToColIndex.entrySet().iterator();
            while(it.hasNext()) {
                var entry = it.next();
                var meta  = entry.getValue();

                sb.append(indent).append(this_indent);
                sb.append(color(entry.getKey(), ColorCode.CYAN));
                sb.append(color(" -> Type: ", ColorCode.BOLD_CYAN));
                sb.append(color(String.valueOf(meta.type()), ColorCode.CYAN));
                sb.append(color(", Col: ", ColorCode.BOLD_CYAN));
                sb.append(color(String.valueOf(meta.col()), ColorCode.CYAN));
                if(it.hasNext()) sb.append("\n");
            }

            return sb.toString();
        }

        @SuppressWarnings("unused")
        public String toPrintString() {
            return toPrintString("");
        }

        @Override
        public String toString() {
            var title = "===" + this.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(this)) + "===";
            StringBuilder sb = new StringBuilder();
            sb.append(title).append("\n");
            sb.append("Start Row: ").append(startRow).append("\n");
            sb.append("End Row: ").append(endRow).append("\n");
            sb.append("Columns: ").append("\n");
            for(var entry : headerToColIndex.entrySet()) {
                sb.append("\t").append(entry.getKey()).append(" -> Type: ").append(entry.getValue().type()).append(", Col: ").append(entry.getValue().col()).append("\n");
            }
            sb.append("=".repeat(title.length()));
            return sb.toString();
        }
    }

    record pair(String type, Integer col) {
    }
}
