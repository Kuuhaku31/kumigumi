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
    private final Workbook workbook;

    public
    ExcelReader(String file_path) throws IOException
    {
        workbook = new XSSFWorkbook(new FileInputStream(file_path));
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
    void TestTableData()
    {
        Sheet sheet = workbook.getSheet("test");

        ColumnList column_list = new ColumnList();
        column_list.Add("Main", 0);
        column_list.Add("name", 1);

        TableData td = new TableData(sheet, "test", 8, 12, column_list);

        td.PrintInfo();
    }
}
