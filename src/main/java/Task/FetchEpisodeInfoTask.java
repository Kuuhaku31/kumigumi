package Task;

import java.util.Map;
import java.util.Set;

import Database.Info.EpisodeInfo;
import Excel.TableData;
import NetAccess.NetAccess;


public class FetchEpisodeInfoTask extends Task {

    final Integer            ANI_ID;
    private Set<EpisodeInfo> result_set = null;

    public FetchEpisodeInfoTask(Integer ANI_ID) {
        this.ANI_ID = ANI_ID;
    }

    public void execute() {
        start();

        try {
            result_set = NetAccess.FetchEpisodeInfoSet(ANI_ID);
            if(result_set == null) throw new Exception("获取 EpisodeInfo 失败");
            complete();
        } catch(Exception _) {
            fail();
        }
    }

    public Set<EpisodeInfo> getResult() {
        return result_set;
    }

    @Override
    public Map<String, Object> getInfo() {
        var info = super.getInfo();
        info.put("ANI_ID", ANI_ID);
        info.put("ResultType", result_set != null ? result_set.getClass().getSimpleName() : "null");
        info.put("ResultSize", result_set == null ? 0 : result_set.size());
        return info;
    }

    public static Set<FetchEpisodeInfoTask> ParseFetchEpisodeInfoTaskByTableData(TableData tableData) {

        var ani_id_index = tableData.GetColumnIndex("ANI_ID");

        Set<FetchEpisodeInfoTask> taskSet = new java.util.HashSet<>();
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

            if(ani_id != null) taskSet.add(new FetchEpisodeInfoTask(ani_id));
        }
        return taskSet;
    }
}
