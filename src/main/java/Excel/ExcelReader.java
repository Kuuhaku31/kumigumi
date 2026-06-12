package Excel;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Utils.ColorCode;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static Utils.UtilityFunctions.color;


public class ExcelReader {

    private ExcelReader() {} // 私有构造函数，禁止实例化

    /**
     * 读取 Excel 文件并解析数据
     * @param filePath Excel 文件路径
     * @throws IOException 如果文件读取失败
     */
    public static ExcelResult Read(String filePath) throws IOException {

        // 创建临时文件（系统自动放在临时目录）
        var temp_file = Files.createTempFile("Temp_", ".xlsx");
        try {
            var source_path = Path.of(filePath);
            if(!Files.exists(source_path)) {
                throw new IOException("Excel file not found: " + filePath);
            }

            // 复制 Excel 文件到临时文件
            try(var is = Files.newInputStream(source_path)) {
                Files.copy(is, temp_file, StandardCopyOption.REPLACE_EXISTING);
            }
            var msg = "ExcelReader: 复制文件到: " + temp_file.toAbsolutePath().toString();
            System.out.println(color(msg, ColorCode.GREEN));

            // 创建工作簿，一次性读取全部 sheet 数据
            try(
                var fis = new FileInputStream(temp_file.toFile());
                var workbook = new XSSFWorkbook(fis)
            ) {
                System.out.println(color("ExcelReader: 成功加载工作簿，开始读取数据...", ColorCode.GREEN));
                var excel_data = read_excel_data(workbook);
                System.out.println(color("ExcelReader: 成功读取数据，开始解析...", ColorCode.GREEN));
                var context = new ExcelReadContext(excel_data);
                return context.parse();
            }
        }
        finally {
            // 删除临时文件
            try { Files.deleteIfExists(temp_file); }
            catch(Exception e) {
                var msg = "Failed to delete temporary file: " + e.getMessage();
                System.out.println(color(msg, ColorCode.RED));
            }
        }
    }

    private static Map<String, List<List<String>>> read_excel_data(XSSFWorkbook workbook) {

        var evaluator = workbook.getCreationHelper().createFormulaEvaluator(); // 创建公式求值器

        // 读取所有 sheet 数据到 excel_data 中
        var excel_data = new LinkedHashMap<String, List<List<String>>>();
        for(var sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {

            // 读取该 sheet 的所有数据到 sheet_data 中
            var sheet = workbook.getSheetAt(sheetIndex);
            var sheet_data = new ArrayList<List<String>>();
            for(var rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {

                // 读取该行数据，如果该行不存在，则添加一个空行
                var row = sheet.getRow(rowIndex);
                if(row == null) {
                    sheet_data.add(new ArrayList<>());
                    continue; // 跳过空行
                }

                // 读取该行的所有单元格数据到 row_data 中
                var row_data = new ArrayList<String>();
                var last_cell_num = row.getLastCellNum();
                for(var col_index = 0; col_index < last_cell_num; col_index++)
                    row_data.add(get_cell_value(evaluator, row.getCell(col_index)));
                sheet_data.add(row_data);
            }
            excel_data.put(sheet.getSheetName(), sheet_data);
        }
        return excel_data;
    }

    private static String get_cell_value(FormulaEvaluator evaluator, Cell cell) {

        // 如果单元格不存在或为空白，则返回 null
        if(cell == null || cell.getCellType() == CellType.BLANK) return null;

        CellValue value = null;

        // 如果是公式单元格，则求值；否则直接获取值
        if(cell.getCellType() == CellType.FORMULA) {

            // 公式求值失败时，捕获异常并打印错误信息
            try { value = evaluator.evaluate(cell); }
            catch(Exception e) {
                var title = color("GetCellValue Error:", ColorCode.BOLD_RED);
                var msg = title + "\n  msg:  " + e.getMessage() + "\n  cell: " + cell;
                System.out.println(color(msg, ColorCode.RED));
            }

            // 如果求值结果为 null，则返回 null；否则根据求值结果的类型返回对应的字符串
            if(value == null) return null;
            else return switch(value.getCellType()) {
                case BOOLEAN -> value.getBooleanValue() ? "1" : "0";
                case NUMERIC -> Double.toString(value.getNumberValue());
                case STRING  -> value.getStringValue();
                default      -> null;
            };
        }

        // 如果不是公式单元格，则根据单元格类型返回对应的字符串
        else return switch(cell.getCellType()) {
            case BOOLEAN -> cell.getBooleanCellValue() ? "1" : "0";
            case NUMERIC -> Double.toString(cell.getNumericCellValue());
            case STRING  -> cell.getStringCellValue();
            default      -> null;
        };
    }
}
