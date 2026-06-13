# TODO

## 数据库与 schema

- 增加 schema migration 机制，处理已有 SQLite 文件从旧结构升级到 `Table.md` 当前结构。
- 为 `SQLiteAccess.UpsertInfo` 增加覆盖真实 SQLite 的集成测试。
- 检查并统一所有时间字段的时区解析策略，尤其是 `EpisodeRecordInfo` 的 `view_datetime` + `timezone` 组合。

## Excel 工作流

- 增加一份最小 Excel fixture，用于验证 `_item_*`、`_fetch_task_*`、`_run_fetch_task`、`_download_torrent`、`_to_db` 的主流程。
- 改善 Excel 命令错误提示：缺字段、字段类型错误、块名不存在时给出更明确的位置。
- 让 `ExcelReader` 在读取完成后关闭 workbook 和临时文件资源，再补回完整的 xlsx 读取单元测试。
- 评估是否保留 `_item_ani`、`_item_epi` 等短别名；如果保留，需要在文档和测试中固定下来。

## 运行与发布

- 增加标准运行方式，例如 Maven exec/shade 打包，避免手工拼接 classpath。
- 将默认路径集中记录到配置文档，明确 `--excel_file_path`、`--database_path`、`--log_path` 的优先级。
- 为 ODBC / Power Query 示例补充可验证的样例库和样例工作簿。

## 代码整理

- 清理已经不再使用的 import 和兼容分支。
- 继续减少 `Main.Main` 中的大型 switch，将 Excel 命令拆成更容易测试的命令处理器。
- 为 `Task.*` 执行结果增加更清晰的成功、跳过和失败统计。
