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
import Utils.ColorCode;
import Utils.TableData;

import static Utils.UtilityFunctions.color;


final class MainApplication {

    //  = "D:/OneDrive/db/kumigumi/test.xlsx";
    //  = "D:/OneDrive/db/kumigumi/kumigumi.db";
    //  = "ignore/logs/";
    public String EXCEL_FILE_PATH;
    public String DATABASE_PATH;
    public String LOG_PATH;

    final Map<String, Item> variables    = new java.util.HashMap<>();
    final List<List<String>> commandList = new java.util.ArrayList<>();

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
                if(i + 1 < args.length) configPath = args[++i];
                read_config_from_env(configPath);
            }
            default -> System.out.println("Unknown argument: " + args[i]);
            }
        }
    }

    private void read_config_from_env(String config_file_path) {
        try(var reader = Files.newBufferedReader(Path.of(config_file_path))) {
            String line;
            while((line = reader.readLine()) != null) {
                var parts = line.split("=", 2);
                if(parts.length == 2) {
                    var key   = parts[0].trim();
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

    void Run() throws IOException {

        var nowTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        LOG_PATH += nowTimeStr + "/";

        System.out.println("Excel File Path: " + EXCEL_FILE_PATH);
        System.out.println("Database Path: " + DATABASE_PATH);
        System.out.println("Log Path: " + LOG_PATH);

        Files.createDirectories(Path.of(LOG_PATH));

        // 读取 Excel 文件
        var excelResult = ExcelReader.Read(EXCEL_FILE_PATH);
        variables.putAll(StringItem.parse(excelResult.variables()));
        variables.putAll(TableDataItem.parse(excelResult.tableDataList()));
        commandList.addAll(excelResult.commands());

        System.out.println();

        // 执行命令
        var cmd_index = 0;
        for(var cmd : commandList) {

            if(cmd == null || cmd.isEmpty()) continue;

            var cmd_enum = ExcelCommand.fromString(cmd.getFirst());

            // 打印命令信息
            var msg = "#" + (++cmd_index) + ": " + cmd_enum + " -> " + color(cmd.subList(1, cmd.size()).toString(), ColorCode.BLUE);
            System.out.println(color(msg, ColorCode.BOLD_BLUE));

            if(cmd_enum == null) {
                System.out.println("Unknown command: " + cmd.getFirst());
            } else
                switch(cmd_enum) {

                case PRINT_MESSAGE -> Commands.printMessage(cmd);
                case PRINT_VARIABLE -> Commands.printVariable(this, cmd);
                case SAVE_LOG -> Commands.saveLog(this, cmd);

                case MAKE_ITEM_EPISODE_RECORD -> Commands.makeEpisodeRecordItem(this, cmd);
                case MAKE_ITEM_RSS -> Commands.makeRSSItem(this, cmd);

                case MAKE_FETCH_TASK_ANIME -> Commands.makeFetchTaskAnime(this, cmd);
                case MAKE_FETCH_TASK_EPISODE -> Commands.makeFetchTaskEpisode(this, cmd);
                case MAKE_FETCH_TASK_TORRENT_PAGE -> Commands.makeFetchTaskTorrentPage(this, cmd);
                case RUN_TASK -> Commands.runTask(this, cmd);

                case TO_DB -> Commands.toDB(this, cmd);

                default -> System.out.println("Unknown command: " + cmd.getFirst());
                }

            System.out.println();
        }

        System.out.println("完成");
    }

    Set<TableData> getBlockDataByNames(List<String> blockNames) {
        Set<TableData> res = new java.util.HashSet<>();
        for(var blockName : blockNames) {
            var blockData = variables.get(blockName);
            if(blockData == null) {
                System.out.println(color("未找到名为 " + blockName + " 的变量", ColorCode.RED));
            } else if(blockData instanceof TableDataItem tableDataItem) {
                res.add(tableDataItem.data);
            } else {
                System.out.println(color("变量 " + blockName + " 不是 TableData 类型", ColorCode.RED));
            }
        }
        return res;
    }

    void putOrMergeItem(String variableName, Item set) {

        var    existing_value = variables.get(variableName);
        String err_msg        = null;

        // 如果不存在，直接放入
        if(existing_value == null) variables.put(variableName, set);

        // 如果是 InfoSetItem，尝试合并为 InfoSetItem
        else if(set instanceof InfoSetItem info_set) {
            if(existing_value instanceof InfoSetItem s)
                s.data.addAll(info_set.data); // 合并数据
            else
                err_msg = "Variable " + variableName + " exists but is of incompatible type.";
        }

        // 如果是 TaskSetItem，尝试合并为 TaskSetItem
        else if(set instanceof TaskSetItem task_set) {
            if(existing_value instanceof TaskSetItem s)
                s.data.addAll(task_set.data); // 合并数据
            else
                err_msg = "Variable " + variableName + " exists but is of incompatible type.";
        }

        // 如果不是上述两种类型
        else
            err_msg = "Unsupported item type for merging: " + set.getClass().getName();

        if(err_msg != null) {
            err_msg = "Error merging item into variable: " + err_msg;
            System.out.println(color(err_msg, ColorCode.BOLD_RED));
        }
    }

    String getVariableAsString(List<String> requested_variable_names, boolean enable_color) {

        var sb = new StringBuilder();
        for(var variable_name : requested_variable_names) {
            sb.append("# ").append(variable_name).append("\n");
            if(!variables.containsKey(variable_name)) {
                sb.append("Variable not found: ").append(variable_name).append("\n\n");
                continue;
            }

            // 将变量内容转换为字符串
            var item = variables.get(variable_name);
            if(item == null)
                sb.append("null");
            else
                sb.append(item.toPrintString(enable_color));
            sb.append("\n\n");
        }
        return sb.toString();
    }
}
