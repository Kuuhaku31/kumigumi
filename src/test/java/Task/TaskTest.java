package Task;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import Database.TorrentDownloader;
import Utils.TableData;
import Utils.Task;
import Utils.TaskStatus;

class TaskTest {

    @Test
    void parallelExecutionUpdatesTaskStatuses() {
        var success = new StubTask("success", true);
        var failure = new StubTask("failure", false);
        Set<StubTask> tasks = new LinkedHashSet<>(Set.of(success, failure));

        Task.ParallelExecution(tasks);

        assertEquals(TaskStatus.SUCCEEDED, success.getStatus());
        assertEquals(TaskStatus.FAILED, failure.getStatus());
        assertTrue(success.executed);
        assertTrue(failure.executed);
    }

    @Test
    void emptyTaskSetIsAccepted() {
        assertDoesNotThrow(() -> Task.ParallelExecution(Set.of()));
    }

    @Test
    void concreteTaskToPrintStringIncludesClassNameAndTaskInfo() {
        var animeTask       = new FetchAnimeInfoTask(123);
        var episodeTask     = new FetchEpisodeInfoTask(456);
        var torrentPageTask = new FetchTorrentPageTask("https://example.com/rss");
        var torrentInfoTask = new FetchTorrentInfoTask(new TorrentDownloader("abc123", List.of("https://example.com/file.torrent")));

        assertAll(
            () -> assertInstanceOf(FetchInfoTask.class, animeTask),
            () -> assertInstanceOf(FetchInfoTask.class, episodeTask),
            () -> assertInstanceOf(FetchInfoTask.class, torrentPageTask),
            () -> assertInstanceOf(FetchInfoTask.class, torrentInfoTask),
            () -> assertTrue(animeTask.toPrintString(false).startsWith("FetchAnimeInfoTask:\n")),
            () -> assertTrue(animeTask.toPrintString(false).contains("ANI_ID:\t123")),
            () -> assertTrue(episodeTask.toPrintString(false).startsWith("FetchEpisodeInfoTask:\n")),
            () -> assertTrue(episodeTask.toPrintString(false).contains("ANI_ID:\t456")),
            () -> assertTrue(torrentPageTask.toPrintString(false).startsWith("FetchTorrentPageTask:\n")),
            () -> assertTrue(torrentPageTask.toPrintString(false).contains("URL_RSS:\thttps://example.com/rss")),
            () -> assertTrue(torrentInfoTask.toPrintString(false).startsWith("FetchTorrentInfoTask:\n")),
            () -> assertTrue(torrentInfoTask.toPrintString(false).contains("abc123")),
            () -> assertEquals(animeTask.toPrintString(false), animeTask.toString())
        );
    }

    @Test
    void parseFetchTasksByTableData() {
        var animeTable = new TableData(new String[] {
            "ANI_ID", "title",
            "100",    "A",
            "",       "Blank",
            "bad",    "Bad",
            "101",    "B"
        }, 2);
        var animeTasks = FetchAnimeInfoTask.ParseFetchAnimeInfoTaskByTableData(animeTable);
        assertEquals(Set.of(100, 101), animeTasks.stream().map(task -> task.ANI_ID).collect(Collectors.toSet()));

        var episodeTasks = FetchEpisodeInfoTask.ParseFetchEpisodeInfoTaskByTableData(animeTable);
        assertEquals(Set.of(100, 101), episodeTasks.stream().map(task -> task.ANI_ID).collect(Collectors.toSet()));

        var rssTable = new TableData(new String[] {
            "URL_RSS", "name",
            " https://example.com/rss.xml ", "rss",
            "",                            "blank",
            "https://example.com/other.xml", "other"
        }, 2);
        var torrentPageTasks = FetchTorrentPageTask.ParseFetchTorrentPageTaskByTableData(rssTable);
        assertEquals(
            Set.of("https://example.com/rss.xml", "https://example.com/other.xml"),
            torrentPageTasks.stream().map(task -> task.URL_RSS).collect(Collectors.toSet())
        );
    }

    private static class StubTask extends Task {
        private final String name;
        private final boolean shouldSucceed;
        private boolean executed;

        StubTask(String name, boolean shouldSucceed) {
            this.name = name;
            this.shouldSucceed = shouldSucceed;
        }

        @Override
        public void execute() {
            executed = true;
            start();
            if(shouldSucceed) complete();
            else fail();
        }

        @Override
        public Map<String, Object> getInfo() {
            var info = super.getInfo();
            info.put("Name", name);
            return info;
        }
    }
}
