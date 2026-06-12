package Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import Excel.ExcelReader;
import MetaData.ARGS;
import Utils.UtilityFunctions;


final class MainApplication {

    void run(String[] args) throws IOException {
        MainArguments.parse(args);

        var nowTimeStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        ARGS.LOG_PATH += nowTimeStr + "/";

        System.out.println("Excel File Path: " + ARGS.EXCEL_FILE_PATH);
        System.out.println("Database Path: " + ARGS.DATABASE_PATH);
        System.out.println("Log Path: " + ARGS.LOG_PATH);

        Files.createDirectories(Path.of(ARGS.LOG_PATH));

        var excelResult = ExcelReader.Read(ARGS.EXCEL_FILE_PATH);
        UtilityFunctions.WriteStringToFile(excelResult.toString(), ARGS.LOG_PATH + "01.excel_result.txt");

        var commandRunner = new CommandRunner(excelResult);
        for(var cmd : excelResult.commands()) {
            if(cmd == null || cmd.isEmpty()) continue;
            commandRunner.run(cmd);
        }

        System.out.println("完成");
    }
}
