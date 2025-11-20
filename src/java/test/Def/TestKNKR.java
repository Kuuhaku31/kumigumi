package Def;

public
class TestKNKR
{

    static
    void main()
    {
        // List<Map<String, String>> anime_fetch = new ArrayList<>();
        //
        // int id = 533027;
        //
        // new TaskFetchAni(anime_fetch, id).run();
        //
        // for(var recode : anime_fetch)
        // {
        //     for(var key : recode.keySet())
        //     {
        //         System.out.println(key + " : " + recode.get(key));
        //     }
        // }
        //
        // String[] ANIME_HEADERS_FETCH = {
        //     DBStructure.Headers.ANI_ID.toString(),
        //     DBStructure.Headers.air_date.toString(),
        //     DBStructure.Headers.title.toString(),
        //     DBStructure.Headers.title_cn.toString(),
        //     DBStructure.Headers.aliases.toString(),
        //     DBStructure.Headers.description.toString(),
        //     DBStructure.Headers.episode_count.toString(),
        //     DBStructure.Headers.url_official_site.toString(),
        //     DBStructure.Headers.url_cover.toString(),
        // };
        // var ani_upsert = new TableData(ANIME_HEADERS_FETCH);
        //
        // // 遍历每个 map，生成记录
        // for(var map : anime_fetch)
        // {
        //     var recode = ani_upsert.new Record();
        //     for(var header : ANIME_HEADERS_FETCH)
        //     {
        //         if(map.containsKey(header))
        //         {
        //             recode.Set(header, map.get(header));
        //         }
        //     }
        // }
        //
        // KG_SQLiteAccess.Open();
        // new TaskUpsert(KG_SQLiteAccess.TableName.anime, ani_upsert).run();
        // KG_SQLiteAccess.Close();

    }
}
