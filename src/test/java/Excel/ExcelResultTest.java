package Excel;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import Utils.DataBlock;

class ExcelResultTest {

    @Test
    void exposesCommandsVariablesAndBlockData() {
        var dataBlock = new DataBlock(
            new String[] { "ANI_ID", "title", "100", "Anime" },
            2
        );

        var result = new ExcelResult(
            Map.of("name", "value"),
            List.of(List.of("run", "arg")),
            Map.of("AnimeRows", dataBlock)
        );

        assertEquals("value", result.variables().get("name"));
        assertEquals(List.of("run", "arg"), result.commands().get(0));
        assertSame(dataBlock, result.dataBlockList().get("AnimeRows"));
        assertTrue(result.getCommandsInfo().contains("run"));
        assertTrue(result.getVariables().contains("name"));
        assertTrue(result.getDataBlocksInfo().contains("AnimeRows"));
    }
}
