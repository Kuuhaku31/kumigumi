package Excel;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import Utils.DataBlock;

class DataBlockTest {

    @Test
    void readsRowsByHeaderNameAndIndex() {
        var dataBlock = new DataBlock(
            new String[] { "ANI_ID", "title", "100", "Anime" },
            2
        );

        assertEquals(0, dataBlock.GetColumnIndex("ANI_ID"));
        assertEquals(1, dataBlock.GetColumnIndex("title"));
        assertEquals(-1, dataBlock.GetColumnIndex("missing"));
        assertEquals(1, dataBlock.GetRowSize());
        assertEquals(2, dataBlock.GetHeaderSize());
        assertArrayEquals(new String[] { "ANI_ID", "title" }, dataBlock.GetHeader());
        assertArrayEquals(new String[] { "100", "Anime" }, dataBlock.GetRow(0));
        assertArrayEquals(new int[] { 0, 1, -1 }, dataBlock.GetHeaderBy(new String[] { "ANI_ID", "title", "missing" }));
    }

    @Test
    void returnsCopiedReadArrays() {
        var dataBlock = new DataBlock(
            new String[] { "ANI_ID", "title", "100", "Anime" },
            2
        );

        var data = dataBlock.GetData();
        data[0] = "changed";
        assertEquals("ANI_ID", dataBlock.GetHeader()[0]);

        var header = dataBlock.GetHeader();
        header[0] = "changed";
        assertEquals("ANI_ID", dataBlock.GetHeader()[0]);

        var row = dataBlock.GetRow(0);
        row[0] = "changed";
        assertEquals("100", dataBlock.GetRow(0)[0]);
    }
}
