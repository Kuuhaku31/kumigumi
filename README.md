# kumigumi 追番计划

## 版本 2026-01

编程语言：Java

使用数据库：SQLite

Excel 和数据库的连接方式：ODBC

## 功能实现

### 1. 从 Excel 文件获取信息并保存到数据库

参数：

```bash
kumigumi import
```

### 2. 从 Excel 文件读取更新列表，并依据该列表获取最新的番剧信息，更新数据库

参数：

```bash
kumigumi fetch_excel
```

### 3. 同时执行 (1) 和 (2)

参数：

```bash
kumigumi all
```

---

通过环境变量或者命令行传递参数确定 Excel 文件和数据库的位置

`KG_EXCEL_PATH`

`KG_DATABASE_PATH`

`-exPath/to/excel.xlsx`

`-dbPath/to/database.db`

---

## 使用 SQLite 数据库

---

## 关键类

`Database.AnimeInfo`

`Database.EpisodeInfo`

`Database.EpisodeRecordInfo`

`Database.RSSInfo`

`Database.TorrentPageInfo`

`Database.TorrentInfo`

`Task.FetchAnimeInfoTask`

`Task.FetchEpisodeInfoTask`

`Task.FetchTorrentPageTask`

`Task.FetchTorrentInfoTask`

`Database.SQLiteAccess`

---

<!-- cSpell:words TEXTAFTER -->

```
=VALUE(TEXTAFTER([@[episode_bangumi_url]], "/ep/"))

找出“只在 A 中、不在 B 中”的数据（A  B）
```

---

### Excel 指令

1. 创建 `DataBlock` 数据块

```
_block  <BlockName>
_sheet  <SheetName>
_from   <RowIndex>
_to     <RowIndex>

<key1>   <key1 column>
<key2>   <key2 column>
...

_block_end
```

- 创建一个名为 `<BlockName>` 的数据块
- 数据块内容来源于 Excel 文件中名为 `<SheetName>` 的工作表，从第 `<RowIndex>` 行到第 `<RowIndex>` 行的数据

---

2. 创建数据库信息项

```
_item_anime          <ItemName> <BlockName1> <BlockName2> ...
_item_episode        <ItemName> <BlockName1> <BlockName2> ...
_item_episode_record <ItemName> <BlockName1> <BlockName2> ...
_item_rss            <ItemName> <BlockName1> <BlockName2> ...
_item_torrent_page   <ItemName> <BlockName1> <BlockName2> ...
```

- 创建一个名为 `<ItemName>` 的信息项，信息项类型为 `Database.*Info` 集合
- 信息项内容来源于数据块 `<BlockName1>`、`<BlockName2>` 等

---

3. 创建 `Task.*` 抓取任务

```
_fetch_task_ani <TaskName> <BlockName1> <BlockName2> ...
_fetch_task_epi <TaskName> <BlockName1> <BlockName2> ...
_fetch_task_tor <TaskName> <BlockName1> <BlockName2> ...
```

- 创建一个名为 `<TaskName>` 的任务集合
- `_fetch_task_ani` 和 `_fetch_task_epi` 从 `ANI_ID` 列创建任务
- `_fetch_task_tor` 从 `URL_RSS` 列创建 `FetchTorrentPageTask`

---

4. 运行 `Task.*` 抓取任务

```
_run_fetch_task <ItemName> <TaskName1> <TaskName2> ...
```

- 运行名为 `<TaskName>` 的抓取任务集合
- 生成 `<ItemName>` 信息项，用于写入数据库

---

5. 下载缺失的种子文件

```
_download_torrent <ItemName> <TorrentPageItemName1> <TorrentPageItemName2> ...
```

- 根据 `torrent_page` 信息查询数据库中尚未保存文件的 `TOR_HASH`
- 使用 `Task.FetchTorrentInfoTask` 下载种子文件并生成 `TorrentInfo`

---

6. 将信息项写入数据库

```
_to_db <ItemName1> <ItemName2> ...
```

- 使用 `SQLiteAccess.UpsertXxxInfo` 系列方法写入数据库
