package Excel;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import Utils.ColorCode;
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
            var msg = "ExcelReader: 已复制文件到: " + temp_file.toAbsolutePath().toString();
            System.out.println(color(msg, ColorCode.GREEN));

            // 创建工作簿，并交给 ExcelReadContext 按需读取单元格数据
            System.out.println(color("ExcelReader: 正在加载工作簿...", ColorCode.GREEN));
            try(
                var fis = new FileInputStream(temp_file.toFile());
                var workbook = new XSSFWorkbook(fis)
            ) {
                System.out.println(color("ExcelReader: 成功加载工作簿，开始解析...", ColorCode.GREEN));
                System.out.println();
                var context = new ExcelReadContext(workbook);
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
}
