package Main;


public enum ExcelCommand {

    PRINT_BLOCK("_print_block"),
    MAKE_EPISODE_RECORD_ITEM("_make_episode_record"),
    MAKE_RSS_ITEM("_make_rss"),
    TO_DB("_to_db"),

    ITEM_ANI("_item_ani", "_item_anime"),
    ITEM_EPI("_item_epi", "_item_episode"),
    ITEM_EPISODE_RECORD("_item_episode_record"),
    ITEM_RSS("_item_rss"),
    ITEM_TORRENT_PAGE("_item_torrent_page"),
    FETCH_TASK_ANI("_fetch_task_ani", "_fetch_anime"),
    FETCH_TASK_EPI("_fetch_task_epi", "_fetch_episode"),
    FETCH_TASK_TOR("_fetch_task_tor", "_fetch_torrent_page"),
    RUN_FETCH_TASK("_run_fetch_task"),
    DOWNLOAD_TORRENT("_download_torrent"),
    UNSUPPORTED_LEGACY_COMMAND("_item_ani_store", "_item_epi_store", "_item_tor_store");

    private final String[] aliases;

    ExcelCommand(String... aliases) {
        this.aliases = aliases;
    }

    /**
     * 根据字符串获取对应的 ExcelCommand 枚举值，支持别名匹配
     * @param commandStr
     * @return
     */
    public static ExcelCommand fromString(String commandStr) {
        for (var command : ExcelCommand.values()) {
            for (String alias : command.aliases) {
                if (alias.equalsIgnoreCase(commandStr)) {
                    return command;
                }
            }
        }
        return null; // 或者抛出异常，取决于你的需求
    }
}
