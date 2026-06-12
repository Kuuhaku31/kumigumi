package Task;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

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
