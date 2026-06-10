package Excel;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import Util.TableData;

class ExcelResultTest {

    @Test
    void exposesCommandsVariablesAndBlockData() {
        var table = new TableData(new String[] { "ANI_ID", "title" });
        var row = table.new Record();
        row.Set("ANI_ID", "100");
        row.Set("title", "Anime");

        var result = new ExcelResult(
            Map.of("name", "value"),
            List.of(List.of("run", "arg")),
            Map.of("AnimeRows", table)
        );

        assertEquals("value", result.variables().get("name"));
        assertEquals(List.of("run", "arg"), result.commands().get(0));
        assertSame(table, result.blockDataList().get("AnimeRows"));
        assertTrue(result.getCommandsInfo().contains("run"));
        assertTrue(result.getVariables().contains("name"));
        assertTrue(result.getBlocksInfo().contains("AnimeRows"));
    }
}
