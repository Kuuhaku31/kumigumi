package Excel;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import Utils.TableData;

class ExcelResultTest {

    @Test
    void exposesCommandsVariablesAndBlockData() {
        var table = new TableData(
            new String[] { "ANI_ID", "title", "100", "Anime" },
            2
        );

        var result = new ExcelResult(
            Map.of("name", "value"),
            List.of(List.of("run", "arg")),
            Map.of("AnimeRows", table)
        );

        assertEquals("value", result.variables().get("name"));
        assertEquals(List.of("run", "arg"), result.commands().get(0));
        assertSame(table, result.tableDataList().get("AnimeRows"));
        assertTrue(result.getCommandsInfo().contains("run"));
        assertTrue(result.getVariables().contains("name"));
        assertTrue(result.getBlocksInfo().contains("AnimeRows"));
    }
}
