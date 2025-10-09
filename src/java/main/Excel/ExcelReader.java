// ExcelReader.java

package Excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;

public
class ExcelReader
{
    public
    void OpenFile(String filePath)
    {
        System.out.println("Opening Excel file: " + filePath);

        try(FileInputStream fis = new FileInputStream(filePath); Workbook workbook = new XSSFWorkbook(fis))
        {
            Sheet sheet = workbook.getSheetAt(0);
            for(Row row : sheet)
            {
                for(Cell cell : row)
                {
                    System.out.print(cell.getStringCellValue() + "\t");
                    switch(cell.getCellType())
                    {
                    case NUMERIC:
                        System.out.print(cell.getNumericCellValue() + "\t");
                        break;

                    case BOOLEAN:
                        System.out.print(cell.getBooleanCellValue() + "\t");
                        break;

                    case STRING: default:
                        System.out.print(cell.getStringCellValue() + "\t");
                        break;
                    }
                }
                System.out.println();
            }
        }
        catch(IOException e)
        {
            System.err.println("Error reading Excel file: " + e.getMessage());
        }
    }
}
