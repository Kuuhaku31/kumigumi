package Util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TableDataTest {

    @Test
    void storesRowsByHeaderName() {
        var table = new TableData(new String[] { "ANI_ID", "title" });
        var row = table.new Record();
        row.Set("ANI_ID", "100");
        row.Set("title", "Anime");
        row.Set("missing", "ignored");

        assertEquals(0, table.GetHeaderIndex("ANI_ID"));
        assertEquals(1, table.GetHeaderIndex("title"));
        assertEquals(-1, table.GetHeaderIndex("missing"));
        assertArrayEquals(new String[] { "100", "Anime" }, table.GetData().get(0));
    }
}
