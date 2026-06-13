package Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Excel.ExcelReader;
import Excel.ExcelResult;
import Excel.TableData;
import Utils.ColorCode;
import Utils.UtilityFunctions;

import static Utils.UtilityFunctions.color;


final class MainApplication {

    //  = "D:/OneDrive/db/kumigumi/test.xlsx";
    //  = "D:/OneDrive/db/kumigumi/kumigumi.db";
    //  = "ignore/logs/";
    public String  EXCEL_FILE_PATH;
    public String  DATABASE_PATH;
    public String  LOG_PATH;

    ExcelResult excelResult;
    final Map<String, Object> variables = new java.util.HashMap<>();

    MainApplication(String[] args) {

        // 解析命令行参数
        for(int i = 0; i < args.length; i++) {
            switch(args[i]) {
            case "--excel_file_path", "-ex" -> {
                if(i + 1 < args.length) EXCEL_FILE_PATH = args[++i];
            }
            case "--database_path", "-db" -> {
                if(i + 1 < args.length) DATABASE_PATH = args[++i];
            }
            case "--log_path" -> {
                if(i + 1 < args.length) LOG_PATH = args[++i];
            }
            case "--use_config" -> {
                var configPath = "./kumigumi.ini";
                if(i + 1 < args.length) {
                    configPath = args[++i];
                }
                    try(var reader = Files.newBufferedReader(Path.of(configPath))) {
                        String line;
                        while((line = reader.readLine()) != null) {
                            var parts = line.split("=", 2);
                            if(parts.length == 2) {
                                var key = parts[0].trim();
                                var value = parts[1].trim();
                                switch(key) {
                                case "EXCEL_FILE_PATH" -> EXCEL_FILE_PATH = value;
                                case "DATABASE_PATH" -> DATABASE_PATH = value;
                                case "LOG_PATH" -> LOG_PATH = value;
                                default -> System.out.println("Unknown config key: " + key);
                                }
                            }
                        }
                    } catch(IOException e) {
                        System.out.println("Failed to read config file: " + e.getMessage());
                    }
            }
            default -> System.out.println("Unknown argument: " + args[i]);
            }
        }
    }

    void Run() throws IOException {

        var nowTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        LOG_PATH += nowTimeStr + "/";

        System.out.println("Excel File Path: " + EXCEL_FILE_PATH);
        System.out.println("Database Path: " + DATABASE_PATH);
        System.out.println("Log Path: " + LOG_PATH);

        Files.createDirectories(Path.of(LOG_PATH));

        excelResult = ExcelReader.Read(EXCEL_FILE_PATH);
        UtilityFunctions.WriteStringToFile(excelResult.toString(), LOG_PATH + "01.excel_result.txt");

        // 执行命令
        for(var cmd : excelResult.commands()) {

            if(cmd == null || cmd.isEmpty()) continue;

            var cmd_enum = ExcelCommand.fromString(cmd.getFirst());
            if(cmd_enum == null) {
                System.out.println("Unknown command: " + cmd.getFirst());
            }
            else switch(cmd_enum) {
            case PRINT_BLOCK -> Commands.printBlock(this, cmd);
            case MAKE_EPISODE_RECORD_ITEM -> Commands.makeEpisodeRecordItem(this, cmd);
            case MAKE_RSS_ITEM -> Commands.makeRSSItem(this, cmd);
            case TO_DB -> Commands.toDB(this, cmd);
            default -> System.out.println("Unknown command: " + cmd.getFirst());
            }
        }

        // 输出变量信息
        System.out.println("Variables:");
        for(var entry : variables.entrySet()) {
            System.out.println("\t" + color(entry.getKey(), ColorCode.BOLD_GREEN) + ": " + entry.getValue());
        }

        System.out.println("完成");
    }

    Set<TableData> getBlockDataByNames(
        List<String>           blockNames,
        Map<String, TableData> blockDataMap
    ) {
        Set<TableData> res = new java.util.HashSet<>();
        for(var blockName : blockNames) {
            var blockData = blockDataMap.get(blockName);
            if(blockData != null) res.add(blockData);
        }
        return res;
    }
}
