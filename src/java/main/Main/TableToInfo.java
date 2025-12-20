package Main;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Database.InfoItem.InfoAni.InfoAniFetch;
import Database.InfoItem.InfoEpi.InfoEpiFetch;
import Database.InfoItem.InfoTor.InfoTorFetch;
import Database.InfoItem.InfoAni.InfoAniStore;
import Database.InfoItem.InfoEpi.InfoEpiStore;
import Database.InfoItem.InfoTor.InfoTorStore;
import util.TableData.TableData;

public class TableToInfo {
    public static List<InfoAniStore> convertInfoAniStore(TableData tableData) {
        List<InfoAniStore> infoList = new ArrayList<>();

        for (var row : tableData.GetData()) {
            int aniIdIndex = tableData.GetHeaderIndex("ANI_ID");
            int urlRSSIndex = tableData.GetHeaderIndex("url_rss");
            int ratingBeforeIndex = tableData.GetHeaderIndex("rating_before");
            int ratingAfterIndex = tableData.GetHeaderIndex("rating_after");
            int remarkIndex = tableData.GetHeaderIndex("remark");

            InfoAniStore info = new InfoAniStore(Integer.parseInt(row[aniIdIndex]));
            if (urlRSSIndex != -1)
                info.url_rss = row[urlRSSIndex];
            if (ratingBeforeIndex != -1)
                info.rating_before = Integer.parseInt(row[ratingBeforeIndex]);
            if (ratingAfterIndex != -1)
                info.rating_after = Integer.parseInt(row[ratingAfterIndex]);
            if (remarkIndex != -1)
                info.remark = row[remarkIndex];
            infoList.add(info);
        }

        return infoList;
    }

    public static List<InfoEpiStore> convertInfoEpiStore(TableData tableData) {
        List<InfoEpiStore> infoList = new ArrayList<>();

        for (var row : tableData.GetData()) {
            int epiIdIndex = tableData.GetHeaderIndex("EPI_ID");
            int ratingIndex = tableData.GetHeaderIndex("rating");
            int viewDatetimeIndex = tableData.GetHeaderIndex("view_datetime");
            int statusDownloadIndex = tableData.GetHeaderIndex("status_download");
            int statusViewIndex = tableData.GetHeaderIndex("status_view");
            int remarkIndex = tableData.GetHeaderIndex("remark");

            InfoEpiStore info = new InfoEpiStore(Integer.parseInt(row[epiIdIndex]));
            if (ratingIndex != -1)
                info.rating = Integer.parseInt(row[ratingIndex]);
            if (viewDatetimeIndex != -1) // 类似：'2025-12-20T23:49:51+08:00'
                info.view_datetime = OffsetDateTime.parse(row[viewDatetimeIndex]);
            if (statusDownloadIndex != -1)
                info.status_download = row[statusDownloadIndex];
            if (statusViewIndex != -1)
                info.status_view = row[statusViewIndex];
            if (remarkIndex != -1)
                info.remark = row[remarkIndex];
            infoList.add(info);
        }

        return infoList;
    }

    public static List<InfoTorStore> convertInfoTorStore(TableData tableData) {
        List<InfoTorStore> infoList = new ArrayList<>();

        for (var row : tableData.GetData()) {
            int torUrlIndex = tableData.GetHeaderIndex("TOR_URL");
            int statusDownloadIndex = tableData.GetHeaderIndex("status_download");
            int remarkIndex = tableData.GetHeaderIndex("remark");

            InfoTorStore info = new InfoTorStore(row[torUrlIndex]);
            if (statusDownloadIndex != -1)
                info.status_download = row[statusDownloadIndex];
            if (remarkIndex != -1)
                info.remark = row[remarkIndex];
            infoList.add(info);
        }

        return infoList;
    }

    public static InfoAniFetch convertInfoAniFetch(Map<String, String> data) {

        if (!data.containsKey("ANI_ID")) // 保证 ANI_ID 存在
            return null;
        var info = new InfoAniFetch(Integer.parseInt(data.get("ANI_ID")));

        if (data.containsKey("air_date"))
            info.air_date = java.sql.Date.valueOf(data.get("air_date"));
        if (data.containsKey("title"))
            info.title = data.get("title");
        if (data.containsKey("title_cn"))
            info.title_cn = data.get("title_cn");
        if (data.containsKey("aliases"))
            info.aliases = data.get("aliases");
        if (data.containsKey("description"))
            info.description = data.get("description");
        if (data.containsKey("episode_count"))
            info.episode_count = Integer.parseInt(data.get("episode_count"));
        if (data.containsKey("url_official_site"))
            info.url_official_site = data.get("url_official_site");
        if (data.containsKey("url_cover"))
            info.url_cover = data.get("url_cover");

        return info;
    }

    public static InfoEpiFetch convertInfoEpiFetch(Map<String, String> data) {

        if (!data.containsKey("EPI_ID")) // 保证 EPI_ID 存在
            return null;
        var info = new InfoEpiFetch(Integer.parseInt(data.get("EPI_ID")));

        if (data.containsKey("ANI_ID"))
            info.ANI_ID = Integer.parseInt(data.get("ANI_ID"));
        if (data.containsKey("ep"))
            info.ep = Integer.parseInt(data.get("ep"));
        if (data.containsKey("sort"))
            info.sort = Float.parseFloat(data.get("sort"));
        if (data.containsKey("air_date"))
            info.air_date = java.sql.Date.valueOf(data.get("air_date"));
        if (data.containsKey("duration"))
            info.duration = Integer.parseInt(data.get("duration"));
        if (data.containsKey("title"))
            info.title = data.get("title");
        if (data.containsKey("title_cn"))
            info.title_cn = data.get("title_cn");
        if (data.containsKey("description"))
            info.description = data.get("description");

        return info;
    }

    public static InfoTorFetch convertInfoTorFetch(Map<String, String> data) {

        if (!data.containsKey("TOR_URL")) // 保证 TOR_URL 存在
            return null;
        var info = new InfoTorFetch(data.get("TOR_URL"));

        if (data.containsKey("ANI_ID"))
            info.ANI_ID = Integer.parseInt(data.get("ANI_ID"));
        if (data.containsKey("air_datetime"))
            info.air_datetime = OffsetDateTime.parse(data.get("air_datetime"));
        if (data.containsKey("size"))
            info.size = Integer.parseInt(data.get("size"));
        if (data.containsKey("url_page"))
            info.url_page = data.get("url_page");
        if (data.containsKey("title"))
            info.title = data.get("title");
        if (data.containsKey("subtitle_group"))
            info.subtitle_group = data.get("subtitle_group");
        if (data.containsKey("description"))
            info.description = data.get("description");

        return info;
    }

}
