package Main;

import Database.Item.UpdateItem;
import Database.Item.UpsertItem;
import Excel.BlockData;
import FetchTask.FetchTaskAni;
import FetchTask.FetchTaskEpi;
import FetchTask.FetchTaskTor;
import InfoItem.InfoAni.*;
import InfoItem.InfoAniTor.*;
import InfoItem.InfoEpi.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import util.TableData;


/**
 * 数据转换工具类
 */
public class ItemTranslation {

    /**
     * Map -> InfoAniUpsert
     */
    public static InfoAniUpsert transAniUpsert(Map<String, String> data) {
        if(data == null || data.isEmpty())
            return null;

        if(!data.containsKey("ANI_ID")) // 保证 ANI_ID 存在
            return null;

        return new InfoAniUpsert(Integer.parseInt(data.get("ANI_ID")));
    }

    /**
     * Map -> InfoEpiUpsert
     */
    public static InfoEpiUpsert transEpiUpsert(Map<String, String> data) {
        if(data == null || data.isEmpty())
            return null;

        if(!data.containsKey("EPI_ID") || !data.containsKey("ANI_ID")) // 保证 EPI_ID 和 ANI_ID 存在
            return null;

        return new InfoEpiUpsert(Integer.parseInt(data.get("EPI_ID")), Integer.parseInt(data.get("ANI_ID")));
    }

    /**
     * Map -> InfoTorUpsert
     */
    public static InfoAniTorUpsert transTorUpsert(Map<String, String> data) {
        if(data == null || data.isEmpty())
            return null;

        if(!data.containsKey("TOR_URL") || !data.containsKey("ANI_ID")) // 保证 TOR_URL 和 ANI_ID 存在
            return null;

        return new InfoAniTorUpsert(Integer.parseInt(data.get("ANI_ID")), data.get("TOR_URL"));
    }

    /**
     * TableData -> InfoAniStore
     */
    public static List<InfoAniStore> convertInfoAniStore(TableData tableData) {
        List<InfoAniStore> infoList = new ArrayList<>();

        var aniIdIndex        = tableData.GetHeaderIndex("ANI_ID");
        var urlRSSIndex       = tableData.GetHeaderIndex("url_rss");
        var ratingBeforeIndex = tableData.GetHeaderIndex("rating_before");
        var ratingAfterIndex  = tableData.GetHeaderIndex("rating_after");
        var remarkIndex       = tableData.GetHeaderIndex("remark");

        for(var row : tableData.GetData()) {

            InfoAniStore info = new InfoAniStore(Integer.parseInt(row[aniIdIndex]));
            if(urlRSSIndex != -1)
                info.url_rss = row[urlRSSIndex];
            if(ratingBeforeIndex != -1)
                try {
                    info.rating_before = Integer.parseInt(row[ratingBeforeIndex]);
                } catch(NumberFormatException e) {
                    info.rating_before = null;
                }
            if(ratingAfterIndex != -1)
                try {
                    info.rating_after = Integer.parseInt(row[ratingAfterIndex]);
                } catch(NumberFormatException e) {
                    info.rating_after = null;
                }
            if(remarkIndex != -1)
                info.remark = row[remarkIndex];
            infoList.add(info);
        }

        return infoList;
    }

    /**
     * TableData -> InfoEpiStore
     */
    public static List<InfoEpiStore> convertInfoEpiStore(TableData tableData) {
        List<InfoEpiStore> infoList = new ArrayList<>();

        var epiIdIndex          = tableData.GetHeaderIndex("EPI_ID");
        var ratingIndex         = tableData.GetHeaderIndex("rating");
        var viewDatetimeIndex   = tableData.GetHeaderIndex("view_datetime");
        var statusDownloadIndex = tableData.GetHeaderIndex("status_download");
        var statusViewIndex     = tableData.GetHeaderIndex("status_view");
        var remarkIndex         = tableData.GetHeaderIndex("remark");

        for(var row : tableData.GetData()) {

            var epi_id = row[epiIdIndex] == null ? null : Integer.parseInt(row[epiIdIndex]);
            if(epi_id == null)
                continue; // 跳过无效数据
            var info = new InfoEpiStore(epi_id);
            if(ratingIndex != -1 && row[ratingIndex] != null)
                info.rating = Integer.parseInt(row[ratingIndex]);
            if(viewDatetimeIndex != -1 && row[viewDatetimeIndex] != null) // 类似：'2025-12-20T23:49:51+08:00'
                info.view_datetime = OffsetDateTime.parse(row[viewDatetimeIndex]);
            if(statusDownloadIndex != -1)
                info.status_download = row[statusDownloadIndex];
            if(statusViewIndex != -1)
                info.status_view = row[statusViewIndex];
            if(remarkIndex != -1)
                info.remark = row[remarkIndex];
            infoList.add(info);
        }

        return infoList;
    }

    /**
     * TableData -> InfoTorStore
     */
    public static List<InfoAniTorStore> convertInfoTorStore(TableData tableData) {
        List<InfoAniTorStore> infoList = new ArrayList<>();

        var aniIdIndex          = tableData.GetHeaderIndex("ANI_ID");
        var torUrlIndex         = tableData.GetHeaderIndex("TOR_URL");
        var statusDownloadIndex = tableData.GetHeaderIndex("status_download");
        var remarkIndex         = tableData.GetHeaderIndex("remark");

        for(var row : tableData.GetData()) {

            InfoAniTorStore info = new InfoAniTorStore(Integer.parseInt(row[aniIdIndex]), row[torUrlIndex]);
            if(statusDownloadIndex != -1)
                info.status_download = row[statusDownloadIndex];
            if(remarkIndex != -1)
                info.remark = row[remarkIndex];
            infoList.add(info);
        }

        return infoList;
    }

    /**
     * Map -> InfoAniFetch
     */
    public static InfoAniFetch convertInfoAniFetch(Map<String, String> data) {

        if(!data.containsKey("ANI_ID")) // 保证 ANI_ID 存在
            return null;
        var info = new InfoAniFetch(Integer.parseInt(data.get("ANI_ID")));

        if(data.containsKey("air_date")) {
            var dateStr = data.get("air_date");
            if(dateStr != null)
                try {
                    var  sdf        = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    Date parsedDate = sdf.parse(dateStr);
                    info.air_date   = parsedDate;
                } catch(java.text.ParseException e) {
                    e.printStackTrace();
                }
        }
        if(data.containsKey("title"))
            info.title = data.get("title");
        if(data.containsKey("title_cn"))
            info.title_cn = data.get("title_cn");
        if(data.containsKey("aliases"))
            info.aliases = data.get("aliases");
        if(data.containsKey("description"))
            info.description = data.get("description");
        if(data.containsKey("episode_count"))
            info.episode_count = Integer.parseInt(data.get("episode_count"));
        if(data.containsKey("url_official_site"))
            info.url_official_site = data.get("url_official_site");
        if(data.containsKey("url_cover"))
            info.url_cover = data.get("url_cover");

        return info;
    }

    /**
     * Map -> InfoEpiFetch
     */
    public static InfoEpiFetch convertInfoEpiFetch(Map<String, String> data) {

        if(!data.containsKey("EPI_ID")) // 保证 EPI_ID 存在
            return null;
        var info = new InfoEpiFetch(Integer.parseInt(data.get("EPI_ID")));

        if(data.containsKey("ep"))
            info.ep = Integer.parseInt(data.get("ep"));
        if(data.containsKey("sort"))
            info.sort = Float.parseFloat(data.get("sort"));
        if(data.containsKey("air_date")) {
            var dateStr = data.get("air_date");
            if(dateStr != null)
                try {
                    var  sdf        = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    Date parsedDate = sdf.parse(dateStr);
                    info.air_date   = parsedDate;
                } catch(java.text.ParseException e) {
                    e.printStackTrace();
                }
        }
        if(data.containsKey("duration"))
            if(data.get("duration") != null)
                info.duration = Integer.parseInt(data.get("duration"));
        if(data.containsKey("title"))
            info.title = data.get("title");
        if(data.containsKey("title_cn"))
            info.title_cn = data.get("title_cn");
        if(data.containsKey("description"))
            info.description = data.get("description");

        return info;
    }

    /**
     * Map -> InfoTorFetch
     */
    public static InfoAniTorFetch convertInfoTorFetch(Map<String, String> data) {

        if(!data.containsKey("TOR_URL") || !data.containsKey("ANI_ID")) // 保证 TOR_URL 和 ANI_ID 存在
            return null;
        var info = new InfoAniTorFetch(Integer.parseInt(data.get("ANI_ID")), data.get("TOR_URL"));

        if(data.containsKey("air_datetime")) {
            var datetimeStr = data.get("air_datetime");
            if(datetimeStr != null) {
                try {
                    var dateTime      = LocalDateTime.parse(datetimeStr);
                    info.air_datetime = dateTime.atOffset(OffsetDateTime.now().getOffset());
                } catch(Exception _) {
                    info.air_datetime = null;
                }
            }
        }
        // size field not present in InfoAniTorFetch, file_size is in InfoTorUpsert only
        if(data.containsKey("url_page"))
            info.url_page = data.get("url_page");
        if(data.containsKey("title"))
            info.title = data.get("title");
        if(data.containsKey("subtitle_group"))
            info.subtitle_group = data.get("subtitle_group");
        if(data.containsKey("description"))
            info.description = data.get("description");

        return info;
    }

    /**
     * TableData -> InfoAniUpsert
     */
    public static List<InfoAniUpsert> convertInfoAniUpsert(TableData tableData) {

        var aniIdIndex = tableData.GetHeaderIndex("ANI_ID");
        if(aniIdIndex == -1)
            return null;

        var rows     = tableData.GetData();
        var infoList = new ArrayList<InfoAniUpsert>();
        for(var row : rows) {
            var info = new InfoAniUpsert(Integer.parseInt(row[aniIdIndex]));
            infoList.add(info);
        }
        return infoList;
    }

    /**
     * TableData -> InfoEpiUpsert
     */
    public static List<InfoEpiUpsert> convertInfoEpiUpsert(TableData tableData) {

        var epiIdIndex = tableData.GetHeaderIndex("EPI_ID");
        var aniIdIndex = tableData.GetHeaderIndex("ANI_ID");
        if(epiIdIndex == -1 || aniIdIndex == -1)
            return null;

        var rows     = tableData.GetData();
        var infoList = new ArrayList<InfoEpiUpsert>();
        for(var row : rows) {
            var info = new InfoEpiUpsert(Integer.parseInt(row[epiIdIndex]), Integer.parseInt(row[aniIdIndex]));
            infoList.add(info);
        }
        return infoList;
    }

    /**
     * TableData -> InfoTorUpsert
     */
    public static List<InfoAniTorUpsert> convertInfoTorUpsert(TableData tableData) {

        var aniIdIndex  = tableData.GetHeaderIndex("ANI_ID");
        var torUrlIndex = tableData.GetHeaderIndex("TOR_URL");
        if(torUrlIndex == -1 || aniIdIndex == -1)
            return null;

        var rows     = tableData.GetData();
        var infoList = new ArrayList<InfoAniTorUpsert>();
        for(var row : rows) {
            var info = new InfoAniTorUpsert(Integer.parseInt(row[aniIdIndex]), row[torUrlIndex]);
            infoList.add(info);
        }
        return infoList;
    }

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
