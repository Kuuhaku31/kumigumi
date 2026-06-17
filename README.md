# kumigumi

kumigumi 是一个用 Excel 指令驱动的、以 SQLite 作为本地数据仓库的追番辅助项目。

---

kumigumi 当前的核心定位是：用 Excel 描述批量操作，用网络访问层补全 Bangumi / RSS / torrent 信息，再把标准化的数据对象写入 SQLite。

当前主流程为：

```text
Excel 工作簿
  -> ExcelReader
  -> variables / commands / DataBlock
  -> MainApplication 执行命令
  -> Main.FetchTask 抓取任务或 Info.*Info 数据对象
  -> SQLiteAccess.UpsertInfo
  -> SQLite
```

## 当前能力

- 从 Excel 读取变量、命令流和 DataBlock 数据块。
- 从 DataBlock 生成 `RSSInfo`、`EpisodeRecordInfo` 或抓取任务。
- 通过 Bangumi API 抓取 `AnimeInfo` 和 `EpisodeInfo`。
- 通过 Mikan / Nyaa RSS 抓取 `TorrentPageInfo`。
- 根据 `torrent_page` 中缺失的 `TOR_HASH` 下载 torrent 文件并生成 `TorrentInfo`。
- 将所有 `Info.*Info` 统一通过 `SQLiteAccess.UpsertInfo` 写入 SQLite。
- 从数据库导出已有 torrent blob 为 `.torrent` 文件。

## 目录结构

```text
src/main/java/
  Main/            程序入口、命令执行、变量模型、内部 FetchTask
  Excel/           Excel 解析器、DataBlock 元数据解析
  Info/            可入库数据对象
  Database/        SQLite 访问层、SQL、JDBC 参数绑定
  NetAccess/       Bangumi、RSS、torrent 下载
  Utils/           DataBlock、Task、打印、日期、torrent 元信息工具
  module-info.java JPMS 模块描述符

src/test/java/
  Database/        SQLite 集成测试
  Excel/           ExcelResult / DataBlock 测试
  Info/            Info 数据对象测试
  Main/            主流程和 FetchTask 测试
  NetAccess/       网络解析器测试
  Utils/           torrent 元信息测试

doc/               项目文档
```

## 重要边界

- `Info` 已经是顶层包，不再位于 `Database.Info`。
- `Database` 只负责 SQLite 初始化、查询、写入和导出，不负责网络请求或 Excel 命令解析。
- `Main.FetchTask` 是 Main 内部实现，包未在 `module-info.java` 中导出。
- 旧的根级 `FetchTask/`、`Database.Item`、`UpsertItem/UpdateItem` 系列已经废弃。
- 当前没有独立的 `Main.ExportTorrent` 入口；torrent 导出通过 Excel 命令 `_export_torrent` 完成。

## 文档地图

- 运行项目、配置 `kumigumi.ini`、执行测试：见 [运行与配置](./doc/usage.md)。
- 编写 Excel 命令和数据块：见 [Excel 指令与数据块](./doc/excel.md)。
- 查看 SQLite 表、字段、外键和 upsert 行为：见 [SQLite Schema](./doc/schema.md)。
- 理解各 Java 包的依赖关系和公开接口：见 [模块结构与接口](./doc/modules.md)。
- 后续维护清单和测试约定：见 [开发维护与 TODO](./doc/development.md)。
