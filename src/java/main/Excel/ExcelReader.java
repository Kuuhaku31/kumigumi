// ExcelReader.java

package Excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;

public
class ExcelReader
{
    private final Workbook         workbook;
    private final FormulaEvaluator evaluator;

    public
    ExcelReader(String file_path) throws IOException
    {
        workbook  = new XSSFWorkbook(new FileInputStream(file_path));
        evaluator = workbook.getCreationHelper().createFormulaEvaluator();
    }

    static
    void main(String[] args) throws IOException
    {
        FileInputStream fis      = new FileInputStream("D:/repositories/kumigumi/ignore/test.xlsx");
        Workbook        workbook = WorkbookFactory.create(fis);
        Sheet           sheet    = workbook.getSheetAt(0);
        Row             row      = sheet.getRow(0);
        Cell            cell     = row.getCell(0);


        // String value_str = GetCellValueAsString(cell);

        // IO.println(value_str);

        // workbook.close();
    }


    // 以键值对返回前两列的数据
    public
    ArrayList<AbstractMap.SimpleEntry<String, String>> ReadShell(String sheetName)
    {
        // 检查工作簿或工作表是否存在
        Sheet sheet = workbook.getSheet(sheetName);
        if(sheet == null) throw new IllegalArgumentException("Sheet \"" + sheetName + "\" does not exist.");

        // 读取数据
        var result = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
        for(Row row : sheet)
        {
            // 跳过空行
            if(row == null) continue;

            Cell keyCell   = row.getCell(0);
            Cell valueCell = row.getCell(1);

            // 跳过空键
            if(keyCell == null || keyCell.getCellType() == CellType.BLANK) continue;

            String key   = keyCell.toString().trim();
            String value = (valueCell != null) ? valueCell.toString().trim() : "";

            result.add(new AbstractMap.SimpleEntry<>(key, value));
        }

        return result;
    }

    public
    ArrayList<TableData> ReadData() //
    {
        String sheet_main = "main";
        Sheet  sheet      = workbook.getSheet(sheet_main);

        ArrayList<TableData> table_data_list = new ArrayList<>();

        Sheet      dst_sheet       = null;
        String     table_name      = "";
        int        start_row       = 0;
        int        end_row         = 0;
        ColumnList column_list_buf = null;

        // 遍历所有行
        boolean is_table = false;
        for(Row row : sheet)
        {
            // 忽略空行
            if(row == null) continue;
            Cell key_cell = row.getCell(0);
            if(key_cell == null || key_cell.getCellType() == CellType.BLANK) continue;

            // 对于每一个键
            String key = key_cell.toString().trim();
            if(is_table) // 处于读取表格信息模式
            {
                switch(key)
                {
                case "_table_end": // 结束表格信息读取
                    // 创建表格数据对象并添加到列表
                    TableData td = new TableData(dst_sheet, evaluator, table_name, start_row, end_row, column_list_buf);
                    table_data_list.add(td);

                    is_table = false;
                    break;

                case "_sheet":
                    dst_sheet = workbook.getSheet(row.getCell(1).toString().trim());
                    break;

                case "_from":
                    start_row = (int) row.getCell(1).getNumericCellValue();
                    break;

                case "_to":
                    end_row = (int) row.getCell(1).getNumericCellValue();
                    break;

                default:
                    int column_idx = (int) row.getCell(1).getNumericCellValue();
                    Cell cell = row.getCell(2);
                    String string_type = cell == null ? "" : cell.toString().trim();
                    column_list_buf.Add(key, column_idx, string_type);
                    break;
                }
            }
            else if(key.equals("_table")) // 开始读取表格信息
            {
                table_name      = row.getCell(1).toString().trim();
                column_list_buf = new ColumnList();

                is_table = true;
            }
        }

        return table_data_list;
    }
}
