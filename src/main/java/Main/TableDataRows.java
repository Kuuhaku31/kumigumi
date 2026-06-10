package Main;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import Util.TableData;


final class TableDataRows {

    private TableDataRows() {}

    static Set<Integer> getAnimeIds(TableData tableData) {
        var result = new LinkedHashSet<Integer>();
        var index  = tableData.GetHeaderIndex("ANI_ID");
        if(index == -1) return result;

        for(var row : tableData.GetData()) {
            var parsed = parseInt(getCell(row, index));
            if(parsed != null) result.add(parsed);
        }
        return result;
    }

    static Set<String> getRSSUrls(TableData tableData) {
        var result = new LinkedHashSet<String>();
        var index  = tableData.GetHeaderIndex("URL_RSS");
        if(index == -1) index = tableData.GetHeaderIndex("url_rss");
        if(index == -1) return result;

        for(var row : tableData.GetData()) {
            var value = getCell(row, index);
            if(value == null || value.isBlank()) continue;
            for(var item : value.split(";")) {
                var rssUrl = item.trim();
                if(!rssUrl.isEmpty()) result.add(rssUrl);
            }
        }
        return result;
    }

    static Map<String, String> rowToMap(TableData tableData, String[] row) {
        var headers = tableData.GetHeaders();
        var result  = new HashMap<String, String>();
        for(var i = 0; i < headers.length; i++) {
            result.put(headers[i], getCell(row, i));
        }
        return result;
    }

    private static String getCell(String[] row, int index) {
        if(row == null || index < 0 || index >= row.length) return null;
        return row[index];
    }

    private static Integer parseInt(String value) {
        if(value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value);
        } catch(NumberFormatException _) {
            return null;
        }
    }
}
