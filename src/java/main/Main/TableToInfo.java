package Main;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import Database.InfoItem.InfoAni.InfoAniStore;
import Database.InfoItem.InfoEpi.InfoEpiStore;
import Database.InfoItem.InfoTor.InfoTorStore;
import util.TableData.TableData;

public class TableToInfo {
    public static List<InfoAniStore> convertInfoAniFetch(TableData tableData) {
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

    public static List<InfoEpiStore> convertInfoEpiFetch(TableData tableData) {
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

    public static List<InfoTorStore> convertInfoTorFetch(TableData tableData) {
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
}
