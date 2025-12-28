package Main;

import java.util.List;

import Database.InfoItem.UpdateItem;
import Database.InfoItem.UpsertItem;
import Excel.BlockData;
import FetchTask.FetchTask;
import FetchTask.FetchTaskAni;
import FetchTask.FetchTaskEpi;
import FetchTask.FetchTaskTor;

class MainFunc {
    static void fetch2510ani(
            List<UpsertItem> upsertBuffer,
            List<UpdateItem> fetchBuffer,
            List<FetchTask> fetchTaskList,
            BlockData blockData) {
        System.out.println("fetch2510ani");
        // 创建任务
        System.out.println("Creating fetch tasks...");
        var ani_id_Index = blockData.GetHeaderIndex("ANI_ID");
        var url_rss_Index = blockData.GetHeaderIndex("url_rss");
        if (ani_id_Index != -1 && url_rss_Index != -1) {
            for (var row : blockData.GetData()) {
                Integer ani_id = Integer.parseInt(row[ani_id_Index]);
                String url_rss = row[url_rss_Index];
                fetchTaskList.add(new FetchTaskAni(upsertBuffer, fetchBuffer, ani_id));
                fetchTaskList.add(new FetchTaskEpi(upsertBuffer, fetchBuffer, ani_id));
                if (url_rss != null && !url_rss.isBlank())
                    fetchTaskList.add(new FetchTaskTor(upsertBuffer, fetchBuffer, url_rss, ani_id));
            }
        }
    }

    static void store2510epi(
            List<UpdateItem> updateList,
            BlockData blockData) {
        System.out.println("store2510epi");
        updateList.addAll(ItemTranslation.convertInfoEpiStore(blockData));
    }

    static void storeTor(
            List<UpdateItem> updateList,
            BlockData blockData) {
        System.out.println("storeTor");
        updateList.addAll(ItemTranslation.convertInfoTorStore(blockData));
    }
}
