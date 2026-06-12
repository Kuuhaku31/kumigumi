package Main;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import Excel.TableData;


final class TableDataRows {

    private TableDataRows() {}

    static Set<Integer> getAnimeIds(TableData tableData) {
        var result = new LinkedHashSet<Integer>();
        var index  = tableData.GetColumnIndex("ANI_ID");
        if(index == -1) return result;

        for(var rowIndex = 0; rowIndex < tableData.GetRowSize(); rowIndex++) {
            var parsed = parseInt(getCell(tableData.GetRow(rowIndex), index));
            if(parsed != null) result.add(parsed);
        }
        return result;
    }

    static Set<String> getRSSUrls(TableData tableData) {
        var result = new LinkedHashSet<String>();
        var index  = tableData.GetColumnIndex("URL_RSS");
        if(index == -1) index = tableData.GetColumnIndex("url_rss");
        if(index == -1) return result;

        for(var rowIndex = 0; rowIndex < tableData.GetRowSize(); rowIndex++) {
            var value = getCell(tableData.GetRow(rowIndex), index);
            if(value == null || value.isBlank()) continue;
            for(var item : value.split(";")) {
                var rssUrl = item.trim();
                if(!rssUrl.isEmpty()) result.add(rssUrl);
            }
        }
        return result;
    }

    static Map<String, String> rowToMap(TableData tableData, int rowIndex) {
        var headers = tableData.GetHeader();
        var row     = tableData.GetRow(rowIndex);
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
