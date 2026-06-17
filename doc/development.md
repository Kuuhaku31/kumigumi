# 开发维护

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
