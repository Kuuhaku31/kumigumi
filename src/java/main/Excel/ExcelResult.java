package Excel;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Util.TableData;


public record ExcelResult(
    Map<String, String> variables,    // 变量名 -> 变量值
    List<List<String>>  commands,     // 解析出的命令列表，每个命令是一个字符串列表，第一项是命令名，后续项是参数
    Map<String, TableData> blockDataList // 解析出的表格数据列表
) {
    public String getCommandsInfo() {
        var sb = new StringBuilder();
        sb.append("#Command List:\n");
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
        sb.append("#Block Data List:\n");
        for(var entry : blockDataList.entrySet()) {
            sb.append("\t").append(entry.getKey()).append(": ").append(entry.getValue().toString().replace("\n", "\n\t")).append("\n");
        }
        return sb.toString();
    }

    public TableData getBlockDataByName(String blockName) {
        return blockDataList.get(blockName);
    }

    public Set<TableData> getBlockDataByNames(List<String> blockNames) {
        Set<TableData> res = new HashSet<>();
        for(var blockName : blockNames) {
            var blockData = getBlockDataByName(blockName);
            if(blockData != null) res.add(blockData);
        }
        return res;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ExcelResult:\n");
        sb.append(getVariables()).append("\n");
        sb.append(getCommandsInfo()).append("\n");
        sb.append(getBlocksInfo()).append("\n");
        return sb.toString();
    }
}
