package Task;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import Database.TorrentDownloader;

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
    void concreteTaskToStringIncludesClassNameAndTaskInfo() {
        var animeTask       = new FetchAnimeInfoTask(123);
        var episodeTask     = new FetchEpisodeInfoTask(456);
        var torrentPageTask = new FetchTorrentPageTask("https://example.com/rss");
        var torrentInfoTask = new FetchTorrentInfoTask(new TorrentDownloader("abc123", List.of("https://example.com/file.torrent")));

        assertAll(
            () -> assertTrue(animeTask.toString().startsWith("FetchAnimeInfoTask{")),
            () -> assertTrue(animeTask.toString().contains("ANI_ID: 123")),
            () -> assertTrue(episodeTask.toString().startsWith("FetchEpisodeInfoTask{")),
            () -> assertTrue(episodeTask.toString().contains("ANI_ID: 456")),
            () -> assertTrue(torrentPageTask.toString().startsWith("FetchTorrentPageTask{")),
            () -> assertTrue(torrentPageTask.toString().contains("URL_RSS: https://example.com/rss")),
            () -> assertTrue(torrentInfoTask.toString().startsWith("FetchTorrentInfoTask{")),
            () -> assertTrue(torrentInfoTask.toString().contains("abc123"))
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
