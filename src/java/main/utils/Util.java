package utils;


import utils.task.KGTask;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public
class Util
{
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

    /**
     *
     * 批量执行任务，用多线程
     * <p>
     * 丢出 中断异常
     */
    public static
    void Multithreading(List<? extends KGTask> task_list)
    {
        var task_count = task_list.size();     // 总数
        var finished   = new AtomicInteger(0); // 完成数
        ShowProgress(0, task_count);           // 更新进度条

        // 提交任务
        var pool = Executors.newFixedThreadPool(12); // 固定大小线程池
        for(var task : task_list)
        {
            pool.submit(() ->
            {
                task.run();
                var done = finished.incrementAndGet(); // 完成数加一
                ShowProgress(done, task_count);        // 更新进度条
            });
        }
        pool.shutdown(); //

        // 等待全部完成
        boolean ok = false;
        try { ok = pool.awaitTermination(3, TimeUnit.MINUTES); }
        catch(InterruptedException e) { System.err.println(e.getMessage()); }

        if(ok) System.out.println("并发任务完成！");
        else System.err.println("出现异常");
    }
}
