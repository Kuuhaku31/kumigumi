# 开发维护与 TODO

## 开发原则

- `Info.*Info` 是数据库数据契约，字段应与 [SQLite Schema](schema.md) 保持一致。
- `Database` 只处理 SQLite，不处理 Excel、HTTP 或命令编排。
- `Main.FetchTask` 是 Main 内部实现，不应从外部模块直接依赖。
- 数据库写入统一使用 `SQLiteAccess.UpsertInfo`。
- 不恢复旧的 `UpsertItem`、`UpdateItem`、根级 `FetchTask` 或 `Database.Item` 抽象。

## 修改 schema 的 checklist

1. 更新 `Database.SQLiteSQL` 的建表 SQL。
2. 更新对应 `Info.*Info` 字段、构造器和打印输出。
3. 更新 `SQLiteAccess` 中对应类型的参数绑定顺序。
4. 更新 [SQLite Schema](schema.md)。
5. 更新 [Excel 指令与数据块](excel.md) 中相关字段说明。
6. 补充或更新 `SQLiteAccessTest` / `InfoTest`。

## 新增一种 Info 类型

1. 在 `src/main/java/Info/` 下新增 `*Info` 类并继承 `BaseInfo`。
2. 在 `SQLiteSQL` 中新增建表 SQL 和 upsert SQL。
3. 在 `SQLiteSQL.createTableStatements()` 中加入建表语句。
4. 在 `SQLiteAccess.UpsertInfo` 中增加分类集合、类型分支和批量写入顺序。
5. 增加对应测试。
6. 更新 `doc/schema.md` 和 `doc/modules.md`。

## 测试布局

```text
src/test/java/Database/SQLiteAccessTest.java
src/test/java/Excel/DataBlockTest.java
src/test/java/Excel/ExcelResultTest.java
src/test/java/Info/InfoTest.java
src/test/java/Main/FetchTaskTest.java
src/test/java/Main/MainTest.java
src/test/java/NetAccess/*Test.java
src/test/java/Utils/TorrentMetaUtilTest.java
```

运行：

```powershell
mvn -q test
```

## 当前 TODO

### 数据库

- 增加 schema migration 机制，支持已有 SQLite 文件升级。
- 扩展 SQLite 集成测试，覆盖更多真实 upsert 和冲突更新场景。
- 统一所有时间字段的时区解析策略，尤其是 `EpisodeRecordInfo` 的 `view_datetime + timezone`。

### Excel 工作流

- 增加最小 Excel fixture，覆盖完整流程：创建 Info、创建任务、运行任务、写库、下载 torrent、导出 torrent。
- 改善 Excel 命令错误提示，包含缺字段、字段类型错误、块名不存在和行列位置。
- 评估是否补回从 DataBlock 直接创建 `AnimeInfo`、`EpisodeInfo`、`TorrentPageInfo` 的命令。
- 明确保留哪些短别名，并为别名补测试。

### 运行与发布

- 增加 Maven exec 或 shade 打包，避免手工拼接 classpath。
- 修正 `.vscode/launch.json` 中已经失效的旧入口说明。
- 为 ODBC / Power Query 示例补充可验证的样例库和样例工作簿。

### 代码整理

- 清理未使用依赖和 import。
- 将 `Commands` 中的命令处理继续拆成更细的可测试单元。
- 为 FetchTask 结果增加成功、跳过和失败统计。
- 将 `Task.ParallelExecution` 的线程数改成可配置项。
