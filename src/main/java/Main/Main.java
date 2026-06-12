package Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import Excel.ExcelReader;
import Excel.ExcelResult;
import MetaData.ARGS;


public class Main {

    public static void main(String[] args) throws IOException {
        new MainApplication().run(args);
    }

    // 读取 Excel 文件并返回结果
    public static ExcelResult ReadExcel(String excelFilePath) throws IOException {
        System.out.println("Reading excel file...");
        var result = ExcelReader.Read(excelFilePath);

        System.out.println("Saving commands to file...");
        try(var writer = Files.newBufferedWriter(Path.of(ARGS.LOG_PATH + "00.commands.txt"))) {
            writer.write(result.getCommandsInfo());
        }

        System.out.println("Saving block data...");
        try(var writer = Files.newBufferedWriter(Path.of(ARGS.LOG_PATH + "00.blocks.txt"))) {
            writer.write(result.getBlocksInfo());
        }

        return result;
    }
}
