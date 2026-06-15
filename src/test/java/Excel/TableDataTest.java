package Excel;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import Utils.TableData;

class TableDataTest {

    @Test
    void readsRowsByHeaderNameAndIndex() {
        var table = new TableData(
            new String[] { "ANI_ID", "title", "100", "Anime" },
            2
        );

        assertEquals(0, table.GetColumnIndex("ANI_ID"));
        assertEquals(1, table.GetColumnIndex("title"));
        assertEquals(-1, table.GetColumnIndex("missing"));
        assertEquals(1, table.GetRowSize());
        assertEquals(2, table.GetHeaderSize());
        assertArrayEquals(new String[] { "ANI_ID", "title" }, table.GetHeader());
        assertArrayEquals(new String[] { "100", "Anime" }, table.GetRow(0));
        assertArrayEquals(new int[] { 0, 1, -1 }, table.GetHeaderBy(new String[] { "ANI_ID", "title", "missing" }));
    }

    @Test
    void returnsCopiedReadArrays() {
        var table = new TableData(
            new String[] { "ANI_ID", "title", "100", "Anime" },
            2
        );

        var data = table.GetData();
        data[0] = "changed";
        assertEquals("ANI_ID", table.GetHeader()[0]);

        var header = table.GetHeader();
        header[0] = "changed";
        assertEquals("ANI_ID", table.GetHeader()[0]);

        var row = table.GetRow(0);
        row[0] = "changed";
        assertEquals("100", table.GetRow(0)[0]);
    }
}
