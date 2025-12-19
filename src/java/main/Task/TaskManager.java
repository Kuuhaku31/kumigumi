package Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class TaskManager {
    private static List<Task> completed_tasks = new ArrayList<>();
    private static List<Task> task_list = new ArrayList<>();
    private static List<Task> failed_tasks = new ArrayList<>();

    public static void addTask(Task task) {
        task_list.add(task);
    }

    public static void addTask(List<Task> tasks) {
        task_list.addAll(tasks);
    }

    public static void runTasks() {
        // 将任务列表复制一份，防止在执行过程中被修改
        var running_list = task_list;
        task_list = new ArrayList<>();

        var task_count = running_list.size(); // 总数
        var finished = new AtomicInteger(0); // 完成数
        ShowProgress(0, task_count); // 更新进度条

        // 并发执行任务
        var pool = Executors.newFixedThreadPool(16);
        for (var task : running_list) {
            pool.submit(() -> {
                task.run();
                var done = finished.incrementAndGet(); // 完成数加一
                ShowProgress(done, task_count); // 更新进度条
            });
        }
        pool.shutdown();

        // 等待全部完成
        var ok = false;
        try {
            ok = pool.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }

        // 输出结果
        if (ok)
            System.out.println("并发任务完成");
        else
            System.err.println("并发任务出现异常");

        // 分类任务运行结果
        for (var task : running_list) {
            if (task.completed)
                completed_tasks.add(task);
            else
                failed_tasks.add(task);
        }
    }

    public static List<Task> getCompletedTask() {
        var res = completed_tasks;
        completed_tasks = new ArrayList<>();
        return res;
    }

    public static List<Task> getFailedTasks() {
        var res = failed_tasks;
        failed_tasks = new ArrayList<>();
        return res;
    }

    public static void showTasks() {
        System.out.println("Current Tasks:");
        for (var task : task_list) {
            System.out.println(task);
        }

        System.out.println("Failed Tasks:");
        for (var task : failed_tasks) {
            System.out.println(task);
        }

        System.out.println("Completed Tasks:");
        for (var task : completed_tasks) {
            System.out.println(task);
        }
    }

    // 控制台进度条显示函数
    private static synchronized void ShowProgress(int done, int total) {
        int percent = (int) ((done * 100.0f) / total);
        int barLen = 30;
        int filled = percent * barLen / 100;
        String bar = "=".repeat(filled) + " ".repeat(barLen - filled);
        System.out.printf("\r开始并发执行任务: [%s] %3d%% (%d/%d)", bar, percent, done, total);
        if (done == total)
            System.out.println();
    }

    public abstract static class Task implements Runnable {
        private final List<String> log = new ArrayList<>();

        private boolean completed = false;

        protected final void addLog(String log_str) {
            String time = LocalDateTime.now().toString();
            log.add("[" + time + "]: " + log_str);
        }

        public final List<String> getLog() {
            return log;
        }

        protected final void completed() {
            completed = true;
        }

        protected final void completed(String log_str) {
            addLog(log_str);
            completed = true;
        }

        @Override
        public String toString() {
            return getClass().getName() + "@" + Integer.toHexString(hashCode()) + getStatusStr();
        }

        protected abstract String getStatusStr();
    }
}
