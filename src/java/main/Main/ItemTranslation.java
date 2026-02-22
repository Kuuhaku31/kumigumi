package Main;

import java.util.ArrayList;
import java.util.List;

import Database.Item.UpdateItem;
import Database.Item.UpsertItem;
import Excel.BlockData;
import FetchTask.FetchTaskAni;
import FetchTask.FetchTaskEpi;
import FetchTask.FetchTaskTor;


/**
 * 数据转换工具类
 */
public class ItemTranslation {


    public static List<FetchTaskAni> createFetchTaskAni(
        List<UpsertItem> upsertBuffer,
        List<UpdateItem> fetchBuffer,
        BlockData        blockData) {

        // 参数检查
        if(upsertBuffer == null || fetchBuffer == null || blockData == null)
            return null;

        // 确保关键字段存在
        var ani_id_index = blockData.GetHeaderIndex("ANI_ID");
        if(ani_id_index == -1)
            return null;

        // 创建任务
        List<FetchTaskAni> res = new ArrayList<>();
        for(var row : blockData.GetData()) {
            Integer ani_id;
            try {
                ani_id = Integer.parseInt(row[ani_id_index]);
            } catch(NumberFormatException e) {
                System.err.println("Invalid ANI_ID: " + row[ani_id_index]);
                continue; // 跳过无效数据
            }
            res.add(new FetchTaskAni(upsertBuffer, fetchBuffer, ani_id));
        }

        return res;
    }

    public static List<FetchTaskEpi> createFetchTaskEpi(
        List<UpsertItem> upsertBuffer,
        List<UpdateItem> fetchBuffer,
        BlockData        blockData) {

        // 参数检查
        if(upsertBuffer == null || fetchBuffer == null || blockData == null)
            return null;

        // 确保关键字段存在
        var ani_id_index = blockData.GetHeaderIndex("ANI_ID");
        if(ani_id_index == -1)
            return null;

        // 创建任务
        List<FetchTaskEpi> res = new ArrayList<>();
        for(var row : blockData.GetData()) {
            Integer ani_id;
            try {
                ani_id = Integer.parseInt(row[ani_id_index]);
            } catch(NumberFormatException e) {
                System.err.println("Invalid ANI_ID: " + row[ani_id_index]);
                continue; // 跳过无效数据
            }
            res.add(new FetchTaskEpi(upsertBuffer, fetchBuffer, ani_id));
        }

        return res;
    }

    public static List<FetchTaskTor> createFetchTaskTor(
        List<UpsertItem> upsertBuffer,
        List<UpdateItem> fetchBuffer,
        BlockData        blockData) {

        // 参数检查
        if(upsertBuffer == null || fetchBuffer == null || blockData == null)
            return null;

        // 确保关键字段存在
        var ani_id_index  = blockData.GetHeaderIndex("ANI_ID");
        var url_rss_index = blockData.GetHeaderIndex("url_rss");
        if(ani_id_index == -1 || url_rss_index == -1)
            return null;

        // 创建任务
        List<FetchTaskTor> res = new ArrayList<>();
        for(var row : blockData.GetData()) {
            Integer ani_id;
            try {
                ani_id = Integer.parseInt(row[ani_id_index]);
            } catch(NumberFormatException e) {
                // System.err.println("Invalid ANI_ID: " + row[ani_id_index]);
                continue; // 跳过无效数据
            }
            String url_rss = row[url_rss_index];
            if(url_rss == null || url_rss.isBlank()) {
                // System.err.println("Empty url_rss for ANI_ID: " + ani_id);
                continue; // 跳过无效数据
            } else {

                // 根据分号分割多个 RSS URL
                for(var url : url_rss.split(";")) {
                    var trimmedUrl = url.trim();
                    if(!trimmedUrl.isEmpty()) {
                        res.add(new FetchTaskTor(upsertBuffer, fetchBuffer, new ArrayList<>(), trimmedUrl, ani_id));
                    }
                }
            }
        }

        return res;
    }
}
