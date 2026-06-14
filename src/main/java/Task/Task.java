package Task;

import static Utils.UtilityFunctions.color;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import Utils.ColorCode;
import Utils.UtilityFunctions;


public abstract class Task {

    Integer       totalTasks    = null;
    AtomicInteger startedCount  = null;
    AtomicInteger finishedCount = null;

    private TaskStatus status = TaskStatus.NOT_STARTED;


    /**
     * 子类实现具体任务
     */
    public abstract void execute();

    final protected void start() {
        if(startedCount == null) return;

        startedCount.incrementAndGet();

        printCounts(startedCount.get(), finishedCount.get(), totalTasks);
    }

    final protected void complete() {
        if(finishedCount == null) return;

        finishedCount.incrementAndGet();
        status = TaskStatus.SUCCEEDED;

        printCounts(startedCount.get(), finishedCount.get(), totalTasks);
    }

    final protected void fail() {
        if(finishedCount == null) return;

        finishedCount.incrementAndGet();
        status = TaskStatus.FAILED;

        printCounts(startedCount.get(), finishedCount.get(), totalTasks);
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Map<String, Object> getInfo() {
        var info = new java.util.HashMap<String, Object>();
        info.put("HashCode", Integer.toHexString(System.identityHashCode(this)));
        info.put("Status", status);
        return info;
    }

    // 默认实现 toString 方法，输出任务实例HashCode和状态
    @Override
    public String toString() {
        var info = getInfo();
        return getClass().getSimpleName() + UtilityFunctions.getInfoString(info);
    }


    // 并行执行任务集合
    public static void ParallelExecution(Set<? extends Task> tasks) {

        // 如果任务集合为空，直接返回
        if(tasks == null || tasks.isEmpty()) return;


        // 统计任务数量，并创建计数器
        final int           taskSize      = tasks.size();
        final AtomicInteger startedCount  = new AtomicInteger(0);
        final AtomicInteger finishedCount = new AtomicInteger(0);

        // 将计数器传递给每个任务实例
        for(Task task : tasks) {
            task.totalTasks    = taskSize;
            task.startedCount  = startedCount;
            task.finishedCount = finishedCount;
        }

        System.out.println();

        // 创建线程池并执行任务，最大线程数为 2，使用 CountDownLatch 等待所有任务完成
        // var executor = Executors.newCachedThreadPool();
        var executor = Executors.newFixedThreadPool(16);
        var latch    = new CountDownLatch(taskSize);
        for(Task task : tasks) {
            executor.submit(() -> {
                task.execute();
                latch.countDown();
            });
        }

        // 等待所有任务完成
        try {
            latch.await();
        } catch(InterruptedException e) {
        }
        System.out.println();

        // 关闭线程池
        executor.shutdown();
    }


    // 同步打印进度条，确保输出不会被多个线程交错
    private static synchronized void printCounts(int started, int finished, int totalTasks) {

        // 使用 ANSI 转义码移动光标到上两行，覆盖之前的输出
        System.out.print("\033[1A");

        printProgressBar(started, totalTasks, ColorCode.BOLD_YELLOW);
        System.out.println();
        printProgressBar(finished, totalTasks, ColorCode.BOLD_CYAN);
    }

    private static void printProgressBar(int finished, int total, ColorCode colorCode) {

        final int width = 64;

        int filled  = (int)Math.round((double)finished * width / total);
        int percent = (int)Math.round((double)finished * 100 / total);

        var bar = new StringBuilder("[");
        for(int i = 0; i < width; i++) {
            bar.append(i < filled ? '=' : ' ');
        }
        bar.append(']');

        // 根据位数在数字前补0
        var total_str    = String.valueOf(total);
        var total_digits = total_str.length();
        var finished_str = String.format("% " + total_digits + "d", finished);
        var msg          = String.format("%s %3d%% (%s/%s)", bar, percent, finished_str, total_str);

        System.out.print("\r" + color(msg, colorCode));
        System.out.flush();
    }
}
