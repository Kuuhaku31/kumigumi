package FetchTask;

import Database.Item.UpdateItem;
import Database.Item.UpsertItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class FetchTaskManager {

    private final List<UpsertItem> bufferUpsert = new ArrayList<>();
    private final List<UpdateItem> bufferUpdate = new ArrayList<>(); // 待更新数据库的项
    private final List<String> checkTorHashList = new ArrayList<>(); // 需要确认的 TOR_HASH 列表

    /**
     * 任务队列
     */
    private final List<FetchTask> taskQueue = new ArrayList<>();

    /**
     * 添加 FetchTaskAni 到任务队列
     */
    public void addFetchTaskAni(Integer ani_id) {
        var newAniTask = new FetchTaskAni(bufferUpsert, bufferUpdate, ani_id);
        taskQueue.add(newAniTask);
    }

    /**
     * 添加 FetchTaskEpi 到任务队列
     */
    public void addFetchTaskEpi(Integer ani_id) {
        var newEpiTask = new FetchTaskEpi(bufferUpsert, bufferUpdate, ani_id);
        taskQueue.add(newEpiTask);
    }

    /**
     * 添加 FetchTaskTor 到任务队列
     */
    public void addFetchTaskTor(String url_rss, Integer ani_id) {
        var newTorTask = new FetchTaskTor(bufferUpsert, bufferUpdate, checkTorHashList, url_rss, ani_id);
        taskQueue.add(newTorTask);
    }

    /**
     * 获取 UpsertItemList
     */
    public List<UpsertItem> getUpsertItemList() { return bufferUpsert; }

    /**
     * 获取 UpdateItemList
     */
    public List<UpdateItem> getUpdateItemList() { return bufferUpdate; }

    /**
     * 获取需要确认的 TOR_HASH 列表
     */
    public List<String> getCheckTorHashList() { return checkTorHashList; }

    /**
     * 运行所有任务
     * 带进度条的多线程运行 FetchTask
     */
    public void runAllTasks() throws IOException {

        // 如果没有任务，直接返回
        if(taskQueue == null || taskQueue.size() == 0) {
            System.out.println("没有任务需要执行");
            return;
        } else {
            System.out.println("开始执行任务...");
        }

        var MAX_THREADS = 32;                   // 最大线程数，避免过度并发导致系统资源耗尽
        var task_count  = taskQueue.size();     // 总数
        var finished    = new AtomicInteger(0); // 完成数

        showProgress(0, task_count); // 更新进度条

        // 并发执行任务
        var pool = Executors.newFixedThreadPool(MAX_THREADS);
        for(var task : taskQueue) {
            pool.submit(() -> {
                task.run();
                var done = finished.incrementAndGet(); // 完成数加一
                showProgress(done, task_count);        // 更新进度条
            });
        }
        pool.shutdown();

        // 等待全部完成
        var ok = false;
        try {
            ok = pool.awaitTermination(1, TimeUnit.MINUTES);
        } catch(InterruptedException e) {
            System.err.println(e.getMessage());
        }

        // 输出结果
        if(ok) {
            System.out.println("并发任务完成");
        } else {
            System.err.println("并发任务出现异常");
        }
    }


    // 控制台进度条显示函数
    private static synchronized void showProgress(int done, int total) {
        int    percent = (int)((done * 100.0f) / total);
        int    barLen  = 30;
        int    filled  = percent * barLen / 100;
        String bar     = "=".repeat(filled) + " ".repeat(barLen - filled);
        System.out.printf("\r开始并发执行任务: [%s] %3d%% (%d/%d)", bar, percent, done, total);
        if(done == total) System.out.println();
    }
}
