```
let
    Source = Excel.CurrentWorkbook(){[Name="tb_rq_ani_id"]}[Content],
    ChangedType = Table.TransformColumnTypes(Source, {{"ANI_ID", Int64.Type}}),
    idText = Text.Combine(List.Transform(ChangedType[ANI_ID], each Text.From(_)), ","),

    Source2 =
        #table(
            {"name", "sql", "enabled"},
            {
                {
                    "rq_anime",
                    "SELECT * FROM anime WHERE ANI_ID IN (" & idText & ") ",
                    true
                },
                {
                    "rq_episode",
                    "SELECT * FROM episode WHERE ANI_ID IN (" & idText & ") ",
                    true
                },
                {
                    "rq_torrent",
                    "SELECT * FROM torrent WHERE ANI_ID IN (" & idText & ") ",
                    true
                }
            }
        ),
    ChangedType2 =
        Table.TransformColumnTypes(
            Source2,
            {
                {"name", type text},
                {"sql", type text},
                {"enabled", type logical}
            }
        )
in
    ChangedType2
```

表

```
let
    Row = Table.SelectRows(sql, each [name] = "rq_anime"),
    sql_text = Row{0}[sql],

    Source = Odbc.Query("dsn=kumigumi", sql_text),
    ChangedType = Table.TransformColumnTypes(Source,{{"ANI_ID", Int64.Type}, {"air_date", type date}, {"rating_before", Int64.Type}, {"rating_after", Int64.Type}})
in
    ChangedType

let
    Row = Table.SelectRows(sql, each [name] = "rq_episode"),
    sql_text = Row{0}[sql],

    Source = Odbc.Query("dsn=kumigumi", sql_text),
    ChangedType = Table.TransformColumnTypes(Source,{{"EPI_ID", Int64.Type}, {"ANI_ID", Int64.Type}, {"ep", Int64.Type}, {"air_date", type date}, {"duration", Int64.Type}, {"rating", Int64.Type}, {"view_datetime", type datetimezone}})
in
    ChangedType

let
    Row = Table.SelectRows(sql, each [name] = "rq_torrent"),
    sql_text = Row{0}[sql],

    Source = Odbc.Query("dsn=kumigumi", sql_text),
    ChangedType = Table.TransformColumnTypes(Source,{{"ANI_ID", Int64.Type}, {"air_datetime", type datetimezone}, {"size", Int64.Type}})
in
    ChangedType
```
