package Main;

import MetaData.ARGS;


final class MainArguments {

    private MainArguments() {}

    static void parse(String[] args) {
        for(int i = 0; i < args.length; i++) {
            switch(args[i]) {
            case "--excel_file_path", "-ex" -> {
                if(i + 1 < args.length) ARGS.EXCEL_FILE_PATH = args[++i];
            }
            case "--database_path", "-db" -> {
                if(i + 1 < args.length) ARGS.DATABASE_PATH = args[++i];
            }
            case "--log_path" -> {
                if(i + 1 < args.length) ARGS.LOG_PATH = args[++i];
            }
            default -> System.out.println("Unknown argument: " + args[i]);
            }
        }
    }
}
