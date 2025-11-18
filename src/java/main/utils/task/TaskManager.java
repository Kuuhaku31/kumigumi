package utils.task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public
class TaskManager
{
    private static List<Task> tasks        = new ArrayList<>();
    private static List<Task> failed_tasks = new ArrayList<>();

    // 控制台进度条显示函数
    private static synchronized
    void ShowProgress(int done, int total)
    {
        int    percent = (int) ((done * 100.0f) / total);
        int    barLen  = 30;
        int    filled  = percent * barLen / 100;
        String bar     = "=".repeat(filled) + " ".repeat(barLen - filled);
        System.out.printf("\r开始并发执行任务: [%s] %3d%% (%d/%d)", bar, percent, done, total);
        if(done == total) System.out.println();
    }

    public static
    void runAllTasksFor()
    {
        // 将任务列表复制一份，防止在执行过程中被修改
        var running_list = tasks;
        tasks = new ArrayList<>();

        for(var task : running_list)
        {
            task.run();
        }
    }

    public static
    void runAllTasks()
    {
        // 将任务列表复制一份，防止在执行过程中被修改
        var running_list = tasks;
        tasks = new ArrayList<>();

        var task_count = running_list.size();     // 总数
        var finished   = new AtomicInteger(0); // 完成数
        ShowProgress(0, task_count);           // 更新进度条

        // 并发执行任务
        var pool = Executors.newFixedThreadPool(16);
        for(var task : running_list)
        {
            pool.submit(() ->
            {
                task.run();
                var done = finished.incrementAndGet(); // 完成数加一
                ShowProgress(done, task_count);        // 更新进度条
            });
        }
        pool.shutdown();

        // 等待全部完成
        var ok = false;
        try { ok = pool.awaitTermination(1, TimeUnit.MINUTES); }
        catch(InterruptedException e) { System.err.println(e.getMessage()); }

        // 输出结果
        if(ok) System.out.println("并发任务完成！");
        else System.err.println("出现异常");
    }

    public static
    List<Task> getFailedTasks()
    {
        var res = failed_tasks;
        failed_tasks = new ArrayList<>();
        return res;
    }

    public static
    void showAllTasks()
    {
        System.out.println("Current Tasks:");
        for(var task : tasks) { System.out.println(task); }

        System.out.println("Failed Tasks:");
        for(var task : failed_tasks) { System.out.println(task); }
    }

    private static
    void addTask(Task task)
    {
        tasks.add(task);
        // System.out.println("[TaskManager]: Task added: " + task);
    }

    private static
    void failedTask(Task task)
    {
        failed_tasks.add(task);
        // System.out.println("[TaskManager]: Task failed: " + task);
    }

    private static
    void removeTask(Task task)
    {
        tasks.remove(task);
        // System.out.println("[TaskManager]: Task removed: " + task);
    }

    public abstract static
    class Task implements Runnable
    {
        private final List<String> log = new ArrayList<>();

        public
        Task()
        { addTask(this); }

        protected final
        void addLog(String log_str)
        {
            String time = LocalDateTime.now().toString();
            log.add("[" + time + "]: " + log_str);
        }

        public final
        List<String> getLog()
        { return log; }

        public final
        void failed()
        { failedTask(this); }

        public final
        void failed(String log_str)
        {
            addLog(log_str);
            failedTask(this);
        }

        @Override
        public
        String toString()
        { return getClass().getName() + "@" + Integer.toHexString(hashCode()) + getStatusStr(); }

        protected abstract
        String getStatusStr();
    }
}
