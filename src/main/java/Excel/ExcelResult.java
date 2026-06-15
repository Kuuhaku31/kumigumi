package Excel;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Utils.ColorCode;
import Utils.TableData;

import static Utils.UtilityFunctions.color;


public record ExcelResult(
    Map<String, String>    variables,    // 变量名 -> 变量值
    List<List<String>>     commands,     // 解析出的命令列表，每个命令是一个字符串列表，第一项是命令名，后续项是参数
    Map<String, TableData> tableDataList // 解析出的表格数据列表
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
        for(var entry : tableDataList.entrySet()) {
            sb.append("\t").append(entry.getKey()).append(": ").append(entry.getValue().toString().replace("\n", "\n\t")).append("\n");
        }
        return sb.toString();
    }

    public TableData getBlockDataByName(String blockName) {
        return tableDataList.get(blockName);
    }

    public Set<TableData> getBlockDataByNames(List<String> blockNames) {
        Set<TableData> res = new HashSet<>();
        for(var blockName : blockNames) {
            var blockData = getBlockDataByName(blockName);
            if(blockData != null) res.add(blockData);
        }
        return res;
    }


    // Variables:
    //     var1: value1
    //     var2: value2
    //     ...
    //     varN: valueN
    //
    // Command List:
    //     row1: [cmd1, arg1, arg2, ...]
    //     row2: [cmd2, arg1, arg2, ...]
    //     ...
    //
    // Block Data List:
    //     block1: TableData{header_size=3, row_size=2, header=[Header1, Header2, Header3], data=[[Data11, Data12, Data13], [Data21, Data22, Data23]]}
    //     block2: TableData{header_size=2, row_size=3, header=[H1, H2], data=[[D11, D12], [D21, D22], [D31, D32]]}
    //     ...
    //
    public String toPrintString(String indent) {

        final var this_indent = "  ";

        var sb = new StringBuilder();

        // 变量列表
        sb.append(indent + color("Variables:\n", ColorCode.BOLD_GREEN));
        for(var entry : variables().entrySet()) {
            sb.append(indent + this_indent);
            sb.append(color(entry.getKey() + ": " + entry.getValue(), ColorCode.GREEN));
            sb.append("\n");
        }

        // 命令列表
        sb.append(indent);
        sb.append("\n" + indent);
        sb.append(color("Command List:", ColorCode.BOLD_BLUE));
        sb.append("\n");
        for(var row : commands()) {
            sb.append(indent + color(this_indent + "Row: [", ColorCode.BOLD_CYAN));
            for(var cell : row) {
                sb.append(color(cell + ", ", ColorCode.CYAN));
            }
            sb.append(color("]\n", ColorCode.BOLD_CYAN));
        }

        // 块数据列表
        sb.append(indent);
        sb.append("\n" + indent);
        sb.append(color("Block Data List:", ColorCode.BOLD_MAGENTA));
        sb.append("\n");
        var it = tableDataList().entrySet().iterator();
        while(it.hasNext()) {
            var entry = it.next();
            sb.append(indent).append(color(this_indent + entry.getKey() + ":\n", ColorCode.BOLD_MAGENTA));
            sb.append(entry.getValue().toPrintString(indent + this_indent.repeat(2)));
            if(it.hasNext()) sb.append("\n");
        }

        return sb.toString();
    }

    public String toPrintString() {
        return toPrintString("");
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
