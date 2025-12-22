package Main;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import Database.InfoItem.InfoAni.*;
import Database.InfoItem.InfoEpi.*;
import Database.InfoItem.InfoTor.*;
import util.TableData.TableData;

public class ItemTranslation {

    public static InfoAniUpsert transniAniUpsert(Map<String, String> data) {
        if (data == null || data.isEmpty())
            return null;

        if (!data.containsKey("ANI_ID")) // 保证 ANI_ID 存在
            return null;

        return new InfoAniUpsert(Integer.parseInt(data.get("ANI_ID")));
    }

    public static InfoEpiUpsert transieEpiUpsert(Map<String, String> data) {
        if (data == null || data.isEmpty())
            return null;

        if (!data.containsKey("EPI_ID") || !data.containsKey("ANI_ID")) // 保证 EPI_ID 和 ANI_ID 存在
            return null;

        return new InfoEpiUpsert(Integer.parseInt(data.get("EPI_ID")), Integer.parseInt(data.get("ANI_ID")));
    }

    public static InfoTorUpsert trantoTorUpsert(Map<String, String> data) {
        if (data == null || data.isEmpty())
            return null;

        if (!data.containsKey("TOR_URL") || !data.containsKey("ANI_ID")) // 保证 TOR_URL 和 ANI_ID 存在
            return null;

        return new InfoTorUpsert(data.get("TOR_URL"), Integer.parseInt(data.get("ANI_ID")));
    }

    public static List<InfoAniStore> convertInfoAniStore(TableData tableData) {
        List<InfoAniStore> infoList = new ArrayList<>();

        var aniIdIndex = tableData.GetHeaderIndex("ANI_ID");
        var urlRSSIndex = tableData.GetHeaderIndex("url_rss");
        var ratingBeforeIndex = tableData.GetHeaderIndex("rating_before");
        var ratingAfterIndex = tableData.GetHeaderIndex("rating_after");
        var remarkIndex = tableData.GetHeaderIndex("remark");

        for (var row : tableData.GetData()) {

            InfoAniStore info = new InfoAniStore(Integer.parseInt(row[aniIdIndex]));
            if (urlRSSIndex != -1)
                info.url_rss = row[urlRSSIndex];
            if (ratingBeforeIndex != -1)
                try {
                    info.rating_before = Integer.parseInt(row[ratingBeforeIndex]);
                } catch (NumberFormatException e) {
                    info.rating_before = null;
                }
            if (ratingAfterIndex != -1)
                try {
                    info.rating_after = Integer.parseInt(row[ratingAfterIndex]);
                } catch (NumberFormatException e) {
                    info.rating_after = null;
                }
            if (remarkIndex != -1)
                info.remark = row[remarkIndex];
            infoList.add(info);
        }

        return infoList;
    }

    public static List<InfoEpiStore> convertInfoEpiStore(TableData tableData) {
        List<InfoEpiStore> infoList = new ArrayList<>();

        var epiIdIndex = tableData.GetHeaderIndex("EPI_ID");
        var ratingIndex = tableData.GetHeaderIndex("rating");
        var viewDatetimeIndex = tableData.GetHeaderIndex("view_datetime");
        var statusDownloadIndex = tableData.GetHeaderIndex("status_download");
        var statusViewIndex = tableData.GetHeaderIndex("status_view");
        var remarkIndex = tableData.GetHeaderIndex("remark");

        for (var row : tableData.GetData()) {

            InfoEpiStore info = new InfoEpiStore(Integer.parseInt(row[epiIdIndex]));
            if (ratingIndex != -1 && row[ratingIndex] != null)
                info.rating = Integer.parseInt(row[ratingIndex]);
            if (viewDatetimeIndex != -1 && row[viewDatetimeIndex] != null) // 类似：'2025-12-20T23:49:51+08:00'
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

        var torUrlIndex = tableData.GetHeaderIndex("TOR_URL");
        var statusDownloadIndex = tableData.GetHeaderIndex("status_download");
        var remarkIndex = tableData.GetHeaderIndex("remark");

        for (var row : tableData.GetData()) {

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

        if (data.containsKey("air_date")) {
            var dateStr = data.get("air_date");
            if (dateStr != null)
                try {
                    var sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    Date parsedDate = sdf.parse(dateStr);
                    info.air_date = parsedDate;
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
        }
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

        if (data.containsKey("ep"))
            info.ep = Integer.parseInt(data.get("ep"));
        if (data.containsKey("sort"))
            info.sort = Float.parseFloat(data.get("sort"));
        if (data.containsKey("air_date")) {
            var dateStr = data.get("air_date");
            if (dateStr != null)
                try {
                    var sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    Date parsedDate = sdf.parse(dateStr);
                    info.air_date = parsedDate;
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
        }
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

        if (data.containsKey("air_datetime")) {
            var datetimeStr = data.get("air_datetime");
            if (datetimeStr != null) {
                var dateTime = LocalDateTime.parse(datetimeStr);
                info.air_datetime = dateTime.atOffset(OffsetDateTime.now().getOffset());
            }
        }
        if (data.containsKey("size"))
            info.size = Long.parseLong(data.get("size"));
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

    public static List<InfoAniUpsert> convertInfoAniUpsert(TableData tableData) {

        var aniIdIndex = tableData.GetHeaderIndex("ANI_ID");
        if (aniIdIndex == -1)
            return null;

        var rows = tableData.GetData();
        var infoList = new ArrayList<InfoAniUpsert>();
        for (var row : rows) {
            var info = new InfoAniUpsert(Integer.parseInt(row[aniIdIndex]));
            infoList.add(info);
        }
        return infoList;
    }

    public static List<InfoEpiUpsert> convertInfoEpiUpsert(TableData tableData) {

        var epiIdIndex = tableData.GetHeaderIndex("EPI_ID");
        var aniIdIndex = tableData.GetHeaderIndex("ANI_ID");
        if (epiIdIndex == -1 || aniIdIndex == -1)
            return null;

        var rows = tableData.GetData();
        var infoList = new ArrayList<InfoEpiUpsert>();
        for (var row : rows) {
            var info = new InfoEpiUpsert(Integer.parseInt(row[epiIdIndex]), Integer.parseInt(row[aniIdIndex]));
            infoList.add(info);
        }
        return infoList;
    }

    public static List<InfoTorUpsert> convertInfoTorUpsert(TableData tableData) {

        var torUrlIndex = tableData.GetHeaderIndex("TOR_URL");
        var aniIdIndex = tableData.GetHeaderIndex("ANI_ID");
        if (torUrlIndex == -1 || aniIdIndex == -1)
            return null;

        var rows = tableData.GetData();
        var infoList = new ArrayList<InfoTorUpsert>();
        for (var row : rows) {
            var info = new InfoTorUpsert(row[torUrlIndex], Integer.parseInt(row[aniIdIndex]));
            infoList.add(info);
        }
        return infoList;
    }
}
