package Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Excel.ExcelReader;
import Utils.ColorCode;
import Utils.DataBlock;

import static Utils.UtilityFunctions.color;


final class MainApplication {

    final String EXCEL_FILE_PATH;
    final String DATABASE_PATH;
    final String LOG_PATH;
    final String EXPORT_DIR;

    final Map<String, Item> variables    = new java.util.HashMap<>();
    final List<List<String>> commandList = new java.util.ArrayList<>();

    MainApplication(String[] args) {

        String excelFilePath = null;
        String databasePath  = null;
        String logPath       = null;
        String exportDir     = null;

        // 解析命令行参数
        for(int i = 0; i < args.length; i++) switch(args[i]) {

            case "--excel_file_path", "-ex" -> { if(i + 1 < args.length) excelFilePath = args[++i]; }
            case "--database_path", "-db"   -> { if(i + 1 < args.length) databasePath  = args[++i]; }
            case "--log_path"               -> { if(i + 1 < args.length) logPath       = args[++i]; }
            case "--export_dir"             -> { if(i + 1 < args.length) exportDir     = args[++i]; }
            case "--use_config" -> {

                var configPath = "./kumigumi.ini";
                if(i + 1 < args.length) configPath = args[++i];

                var config = read_config_from_env(configPath);
                if(config.containsKey("EXCEL_FILE_PATH")) excelFilePath = config.get("EXCEL_FILE_PATH");
                if(config.containsKey("DATABASE_PATH"))   databasePath  = config.get("DATABASE_PATH");
                if(config.containsKey("LOG_PATH"))        logPath       = config.get("LOG_PATH");
                if(config.containsKey("EXPORT_DIR"))      exportDir     = config.get("EXPORT_DIR");
            }

            default -> System.out.println("Unknown argument: " + args[i]);
        }

        var nowTimeStr  = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        EXCEL_FILE_PATH = excelFilePath;
        DATABASE_PATH   = databasePath;
        LOG_PATH        = logPath + nowTimeStr + "/";

        // 如果 EXPORT_DIR 仍未设置，则使用下载目录
        if(exportDir == null || exportDir.isEmpty()) {
            exportDir = "./exported_torrents/";
        }
        EXPORT_DIR = exportDir;
    }

    private Map<String, String> read_config_from_env(String config_file_path) {

        var config = new java.util.HashMap<String, String>();
        try(var reader = Files.newBufferedReader(Path.of(config_file_path))) {
            String line;
            while((line = reader.readLine()) != null) {
                var parts = line.split("=", 2);
                if(parts.length == 2) {
                    var key   = parts[0].trim();
                    var value = parts[1].trim();
                    switch(key) {
                    case "EXCEL_FILE_PATH", "DATABASE_PATH", "LOG_PATH", "EXPORT_DIR" -> config.put(key, value);
                    default -> System.out.println("Unknown config key: " + key);
                    }
                }
            }
        } catch(IOException e) {
            System.out.println("Failed to read config file: " + e.getMessage());
        }
        return config;
    }

    void Run() throws IOException {

        System.out.println("Excel File Path: " + EXCEL_FILE_PATH);
        System.out.println("Database Path: " + DATABASE_PATH);
        System.out.println("Log Path: " + LOG_PATH);
        System.out.println("Export Dir: " + EXPORT_DIR);

        Files.createDirectories(Path.of(LOG_PATH));

        // 读取 Excel 文件
        var excelResult = ExcelReader.Read(EXCEL_FILE_PATH);
        variables.putAll(StringItem.parse(excelResult.variables()));
        variables.putAll(DataBlockItem.parse(excelResult.dataBlockList()));
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

                case UPDATE_TORRENT -> Commands.updateTorrent(this, cmd);
                case EXPORT_TORRENT -> Commands.exportTorrent(this, cmd);
                case FLUSH_DB_VIEW -> Commands.flushDBView(this, cmd);
                case FLUSH_DB_VIEW_REQUIRED -> Commands.flushDBViewRequired(this, cmd);

                default -> System.out.println("Unknown command: " + cmd.getFirst());
                }

            System.out.println();
        }

        System.out.println("完成");
    }

    Set<DataBlock> getDataBlockByNames(List<String> blockNames) {
        Set<DataBlock> res = new java.util.HashSet<>();
        for(var blockName : blockNames) {
            var dataBlock = variables.get(blockName);
            if(dataBlock == null) {
                System.out.println(color("未找到名为 " + blockName + " 的变量", ColorCode.RED));
            } else if(dataBlock instanceof DataBlockItem dataBlockItem) {
                res.add(dataBlockItem.data);
            } else {
                System.out.println(color("变量 " + blockName + " 不是 DataBlock 类型", ColorCode.RED));
            }
        }
        return res;
    }

    Set<Item> getItemsByNames(List<String> itemNames) {
        Set<Item> res = new HashSet<>();
        for(var itemName : itemNames) {
            var item = variables.get(itemName);
            if(item == null) {
                System.out.println(color("未找到名为 " + itemName + " 的变量", ColorCode.RED));
            }
            else res.add(item);
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
