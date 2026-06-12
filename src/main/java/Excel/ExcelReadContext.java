package Excel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Utils.ColorCode;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static Utils.UtilityFunctions.color;


final class ExcelReadContext {
    private final XSSFWorkbook           workbook;      // Excel 工作簿
    private final FormulaEvaluator       evaluator;     // 公式计算器

    private final ExcelCursor            cursor;        // 光标位置

    private final Map<String, String>    variables;     // 定义的变量
    private final List<List<String>>     commands;      // 保存命令列表
    private final Map<String, TableData> blockDataList; // 保存块信息

    private String                     blockName    = null;            // 当前块名称
    private TableMetaData              tmpMetaData  = null;            // 解析过程中使用的元数据
    private Map<String, TableMetaData> metaDataList = new HashMap<>(); // 解析过程中使用的元数据列表

    private boolean isReading = true;

    ExcelReadContext(XSSFWorkbook workbook) {

        this.workbook      = workbook;
        this.evaluator     = workbook.getCreationHelper().createFormulaEvaluator();
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
    private void create_table_data(String tableName, TableMetaData tableMeta) throws CreateTableException {

        // 参数检查
        if(tableMeta.sheetName == null || tableMeta.startRow == null || tableMeta.endRow == null) {

            var msg = color("Missing required metadata for block: \n  ", ColorCode.BOLD_RED);
            if(tableMeta.sheetName == null) msg += color("sheet_name  ", ColorCode.RED);
            if(tableMeta.startRow  == null) msg += color("start_row  ", ColorCode.RED);
            if(tableMeta.endRow    == null) msg += color("end_row", ColorCode.RED);

            throw new CreateTableException(msg);
        }

        // 获取工作表数据
        var sheet = workbook.getSheet(tableMeta.sheetName);
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
                cell_value = CellStringType.parseCellString(cell_value, CellStringType.fromString(column_map.getValue().type())); // 解析出显示值

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
                tmpMetaData    = new TableMetaData();
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

        // 获取工作表
        if(sheetName == null) return null;
        var sheet = workbook.getSheet(sheetName);
        if(sheet == null) return null;

        // 获取行和单元格
        if(rowIndex < 0 || rowIndex > sheet.getLastRowNum()) return null;
        var row = sheet.getRow(rowIndex);
        if(row == null) return null;

        // 获取单元格
        if(colIndex < 0 || colIndex >= row.getLastCellNum()) return null;
        var cell = row.getCell(colIndex);
        if(cell == null || cell.getCellType() == CellType.BLANK) return null;

        // 如果是公式单元格，则求值；否则直接获取值
        if(cell.getCellType() == CellType.FORMULA) {

            CellValue value;
            // 公式求值失败时，捕获异常并打印错误信息
            try { value = evaluator.evaluate(cell); }
            catch(Exception e) {
                var title = color("GetCellValue Error:", ColorCode.BOLD_RED);
                var msg = title + "\n  msg:  " + e.getMessage() + "\n  cell: " + cell;
                System.out.println(color(msg, ColorCode.RED));
                return null;
            }

            // 如果求值结果为 null，则返回空串；否则根据求值结果的类型返回对应的字符串
            return switch(value.getCellType()) {
                case BOOLEAN -> value.getBooleanValue() ? "1" : "0";
                case NUMERIC -> Double.toString(value.getNumberValue());
                case STRING -> {
                   var str = value.getStringValue().trim();
                   yield str.isEmpty() ? null : str;
                }
                default -> null;
            };
        }

        // 如果不是公式单元格，则根据单元格类型返回对应的字符串
        else return switch(cell.getCellType()) {
            case BOOLEAN -> cell.getBooleanCellValue() ? "1" : "0";
            case NUMERIC -> Double.toString(cell.getNumericCellValue());
            case STRING  -> {
                var str = cell.getStringCellValue().trim();
                yield str.isEmpty() ? null : str;
            }
            default -> null;
        };
    }
}
