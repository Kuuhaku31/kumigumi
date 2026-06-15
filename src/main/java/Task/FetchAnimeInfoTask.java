package Task;

import java.util.Map;
import java.util.Set;

import Database.Info.AnimeInfo;
import Database.Info.BaseInfo;
import Excel.TableData;
import NetAccess.NetAccess;


public class FetchAnimeInfoTask extends FetchInfoTask {

    final Integer     ANI_ID;
    private AnimeInfo result = null;

    public FetchAnimeInfoTask(Integer ANI_ID) {
        this.ANI_ID = ANI_ID;
    }

    public void execute() {
        start();

        try {
            result = NetAccess.FetchAnimeInfo(ANI_ID);
            if(result == null) throw new Exception("获取 AnimeInfo 失败");
            complete();
        } catch(Exception _) {
            fail();
        }
    }

    public AnimeInfo getResult() {
        return result;
    }

    @Override
    public Map<String, Object> getInfo() {
        var info = super.getInfo();
        info.put("ANI_ID", ANI_ID);
        info.put("ResultType", result != null ? result.getClass().getSimpleName() : "null");
        return info;
    }


    @Override
    public Set<? extends BaseInfo> GetInfoSet() {
        if(result == null) return java.util.Set.of();

        var infoSet = new java.util.HashSet<BaseInfo>();
        infoSet.add(result);
        return infoSet;
    }

    public static Set<FetchAnimeInfoTask> ParseFetchAnimeInfoTaskByTableData(TableData tableData) {

        var ani_id_index = tableData.GetColumnIndex("ANI_ID");

        Set<FetchAnimeInfoTask> taskSet = new java.util.HashSet<>();
        for(var rowIndex = 0; rowIndex < tableData.GetRowSize(); rowIndex++) {
            Integer ani_id = null;
            var     row    = tableData.GetRow(rowIndex);

            if(ani_id_index != -1) {
                var ani_id_str = row[ani_id_index];
                if(ani_id_str != null && !ani_id_str.isBlank()) {
                    try { ani_id = Integer.parseInt(ani_id_str.trim()); }
                    catch(NumberFormatException _) {}
                }
            }

            if(ani_id != null) taskSet.add(new FetchAnimeInfoTask(ani_id));
        }
        return taskSet;
    }
}
