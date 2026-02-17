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

`InfoItem`

`InfoAni`

`InfoAniFetch`

`InfoAniStore`

`InfoEpi`

`InfoEpiFetch`

`InfoEpiStore`

`InfoTor`

`InfoTorFetch`

`InfoTorStore`

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

2. 创建 `UpdateItemName` 信息项

```
_item_ani_store <UpdateItemName> <BlockName1> <BlockName2> ...
_item_epi_store <UpdateItemName> <BlockName1> <BlockName2> ...
_item_tor_store <UpdateItemName> <BlockName1> <BlockName2> ...
```

- 创建一个名为 `<UpdateItemName>` 的信息项，信息项类型为 `List<InfoAniStore>` / `List<InfoEpiStore>` / `List<InfoTorStore>`
- 信息项内容来源于数据块 `<BlockName1>`、`<BlockName2>` 等

---

3. 创建 `FetchTask` 抓取任务

```
_fetch_task_ani <FetchTaskName> <BlockName1> <BlockName2> ...
_fetch_task_epi <FetchTaskName> <BlockName1> <BlockName2> ...
_fetch_task_tor <FetchTaskName> <BlockName1> <BlockName2> ...
```

- 创建一个名为 `<FetchTaskName>` 的抓取任务，抓取任务类型为 `List<FetchTask>`
- 抓取任务内容来源于数据块 `<BlockName1>`、`<BlockName2>` 等

---

4. 运行 `FetchTask` 抓取任务

```
_run_fetch_task <UpsertItemName> <UpdateItemName> <FetchTaskName1> <FetchTaskName2> ...
```

- 运行名为 `<FetchTaskName>` 的抓取任务
- 生成 `<UpsertItemName>` 和 `<UpdateItemName>` 信息项，分别用于插入和更新数据库

---

5. 用 `UpsertItemName` 和 `UpdateItemName` 信息项更新数据库

```
_to_db <UpsertItemName> <UpdateItemName>
```
