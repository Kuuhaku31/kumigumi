package Excel;

import java.util.List;
import java.util.Map;


public record ExcelResult(
    Map<String, String> variables,    // 变量名 -> 变量值
    List<List<String>>  commands,     // 解析出的命令列表，每个命令是一个字符串列表，第一项是命令名，后续项是参数
    List<BlockData>     blockDataList // 解析出的表格数据列表
) {
    public String getCommandsInfo() {
        var sb = new StringBuilder();
        sb.append("#Data List:\n");
        for(var row : commands) {
            sb.append("\tRow: [");
            for(var cell : row) {
                sb.append(cell).append(", ");
            }
            sb.append("]\n");
        }
        return sb.toString();
    }

    public String getVariables() {
        var sb = new StringBuilder();
        sb.append("#Defined Variables:\n");
        for(var entry : variables.entrySet())
            sb.append("\t").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        return sb.toString();
    }

    public String getBlocksInfo() {
        var sb = new StringBuilder();
        for(var blockData : blockDataList) {
            sb.append(blockData).append("\n");
        }
        return sb.toString();
    }

    public BlockData getBlockDataByName(String blockName) {
        for(var blockData : blockDataList) if(blockData.block_name.equals(blockName))
        return blockData;
        return null;
    }
}
