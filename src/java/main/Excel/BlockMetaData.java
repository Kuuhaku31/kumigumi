package Excel;

import java.util.HashMap;
import java.util.Map;

public class BlockMetaData {
    String blockName;
    Integer startRow;
    Integer endRow;
    String sheetName;

    Map<String, pair> headerToColIndex = new HashMap<>();

    void addColumn(String header, String type, Integer col) {
        headerToColIndex.put(header, new pair(type, col));
    }

    BlockMetaData(String blockName) {
        this.blockName = blockName;
    }

    @Override
    public String toString() {
        var title = "===" + this.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(this))
                + "===";
        StringBuilder sb = new StringBuilder();
        sb.append(title).append("\n");
        sb.append("Block Name: ").append(blockName).append("\n");
        sb.append("Start Row: ").append(startRow).append("\n");
        sb.append("End Row: ").append(endRow).append("\n");
        sb.append("Columns: ").append("\n");
        for (var entry : headerToColIndex.entrySet()) {
            sb.append("\t").append(entry.getKey()).append(" -> Type: ").append(entry.getValue().type())
                    .append(", Col: ").append(entry.getValue().col()).append("\n");
        }
        sb.append("=".repeat(title.length()));
        return sb.toString();
    }
}

record pair(String type, Integer col) {
}