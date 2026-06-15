package Main;


enum ExcelCommand {

    PRINT_MESSAGE("_print_message", "_pm"),
    PRINT_VARIABLE("_print_variable", "_pv"),
    SAVE_LOG("_save_log", "_safe_log"),

    MAKE_ITEM_EPISODE_RECORD("_make_info_episode_record", "_mier", "_make_episode_record", "_item_episode_record"),
    MAKE_ITEM_RSS("_make_info_rss", "_mir", "_make_rss", "_item_rss"),

    MAKE_FETCH_TASK_ANIME("_make_task_fetch_anime", "_mtfa", "_make_fetch_task_anime", "_mfa", "_fetch_task_ani", "_fetch_anime"),
    MAKE_FETCH_TASK_EPISODE("_make_task_fetch_episode", "_mtfe", "_make_fetch_task_episode", "_mfe", "_fetch_task_epi", "_fetch_episode"),
    MAKE_FETCH_TASK_TORRENT_PAGE("_make_task_fetch_torrent_page", "_mtftp", "_make_fetch_task_torrent_page", "_mftp", "_fetch_task_tor", "_fetch_torrent_page"),
    RUN_TASK("_run_task", "_run_fetch_task", "_rft"),

    TO_DB("_to_db"),

    UPDATE_TORRENT("_update_torrent"),

    ITEM_ANI("_item_ani", "_item_anime"),
    ITEM_EPI("_item_epi", "_item_episode"),
    ITEM_TORRENT_PAGE("_item_torrent_page"),
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
