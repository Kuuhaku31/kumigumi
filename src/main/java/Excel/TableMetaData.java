package Excel;

import java.util.HashMap;
import java.util.Map;

import Utils.ColorCode;

import static Utils.UtilityFunctions.color;


final class TableMetaData {

    String  sheetName = null; // 工作表名称
    Integer startRow  = null; // 起始行
    Integer endRow    = null; // 结束行

    Map<String, pair> headerToColIndex = new HashMap<>();

    void addColumn(String header, String type, Integer col) {
        headerToColIndex.put(header, new pair(type, col));
    }

    String toPrintString(String indent) {

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

    String toPrintString() {
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

record pair(String type, Integer col) { }
