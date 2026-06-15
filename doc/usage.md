# 运行与配置

## 环境要求

- JDK 25。
- Maven。
- SQLite 数据库文件由程序自动创建。
- Excel 输入文件需要是 `.xlsx`。

## 构建和测试

```powershell
mvn -q test
mvn -q -DskipTests compile
```

测试启用了 Maven module path，并通过 `--enable-native-access=ALL-UNNAMED` 允许 SQLite JDBC 测试运行。

## 启动入口

主入口是模块化类名：

```text
kumigumi/Main.Main
```

从 IDE 启动时应使用模块化主类写法。命令行运行时推荐先用 Maven/IDE 生成 classpath 或后续补充 exec/shade 打包方式。

## 参数

`Main.Main` 支持：

| 参数                        | 说明                                                      |
| --------------------------- | --------------------------------------------------------- |
| `--excel_file_path` / `-ex` | Excel 工作簿路径                                          |
| `--database_path` / `-db`   | SQLite 数据库路径                                         |
| `--log_path`                | 日志根目录；运行时会追加 `yyyyMMdd_HHmmss/`               |
| `--export_dir`              | `_export_torrent` 输出目录；缺省为 `./exported_torrents/` |
| `--use_config [path]`       | 从配置文件读取路径；不传 path 时使用 `./kumigumi.ini`     |

正常运行应显式传参，或使用 `--use_config`。

## 配置文件

`kumigumi.ini` 使用简单的 `KEY=value` 格式：

```ini
EXCEL_FILE_PATH=ignore/input.xlsx
DATABASE_PATH=ignore/kumigumi.db
LOG_PATH=ignore/logs/
EXPORT_DIR=ignore/torrents/
```

支持的键只有：

- `EXCEL_FILE_PATH`
- `DATABASE_PATH`
- `LOG_PATH`
- `EXPORT_DIR`

未知键会打印提示并忽略。

## 推荐运行方式

```powershell
mvn -q -DskipTests compile
```

然后在 VS Code / Java IDE 中使用 `Kumigumi: Main` 启动配置，参数为：

```text
--use_config
```

该配置会读取工作区根目录下的 `kumigumi.ini`。

## 标准工作流

1. 在 Excel 中定义 `#define` 变量和 `#block` 数据块。
2. 用 `_make_info_rss` / `_make_info_episode_record` 创建本地 Info 数据。
3. 用 `_make_task_fetch_anime` / `_make_task_fetch_episode` / `_make_task_fetch_torrent_page` 创建抓取任务。
4. 用 `_run_task` 执行任务并把结果合并成 Info 集合。
5. 用 `_to_db` 将 Info 集合写入 SQLite。
6. 用 `_update_torrent` 根据数据库中的 `torrent_page` 补齐 torrent 文件。
7. 用 `_export_torrent` 按 `TOR_HASH` 从数据库导出 `.torrent` 文件。

## 日志与导出

`--log_path` 是日志根目录。程序会在该路径后追加当前时间戳目录，例如：

```text
ignore/logs/20260615_173012/
```

`_save_log` 会把指定变量的打印结果写入这个时间戳目录下。

`--export_dir` 是 `_export_torrent` 的输出目录。若目录不存在，命令会尝试创建。
