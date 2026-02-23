package FetchTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import Database.Item.UpdateItem;
import Database.Item.UpsertItem;
import Excel.BlockData;


public class FetchTaskManager {

    final List<UpsertItem> bufferUpsert = new ArrayList<>(); // 待插入或更新数据库的项
    final List<UpdateItem> bufferUpdate = new ArrayList<>(); // 待更新数据库的项
    final List<String> checkTorHashList = new ArrayList<>(); // 需要确认的 TOR_HASH 列表

    final List<FetchTask> taskQueue     = new ArrayList<>(); // 任务队列

    /**
     * 获取任务队列信息
     * @return
     */
    public String getTaskQueueInfo() {
        var sb = new StringBuilder();
        sb.append("FetchTaskManager: ").append(taskQueue.size()).append(" tasks\n");
        for(var task : taskQueue) {
            sb.append("  - ").append(task.toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * 检查任务队列是否为空
     * @return
     */
    public boolean isEmpty() {
        return taskQueue.isEmpty();
    }
    
    /**
     * 添加 FetchTaskAni 到任务队列
     */
    public void addFetchTaskAni(Integer ani_id) {
        var newAniTask = new FetchTaskAni(this, ani_id);
        taskQueue.add(newAniTask);
    }

    /**
     * 通过 BlockData 添加多个 FetchTaskAni 到任务队列
     * @param blockData
     */
    public void addFetchTaskAni(BlockData blockData) {

        // 参数检查
        if(bufferUpsert == null || bufferUpdate == null || blockData == null) return;

        // 确保关键字段存在
        var ani_id_index = blockData.GetHeaderIndex("ANI_ID");
        if(ani_id_index == -1) return;

        // 创建任务
        for(var row : blockData.GetData()) {
            Integer ani_id;
            try { ani_id = Integer.parseInt(row[ani_id_index]); }
            catch(NumberFormatException e) {
                System.err.println("Invalid ANI_ID: " + row[ani_id_index]);
                continue; // 跳过无效数据
            }
            taskQueue.add(new FetchTaskAni(this, ani_id));
        }
    }

    /**
     * 添加 FetchTaskEpi 到任务队列
     */
    public void addFetchTaskEpi(Integer ani_id) {
        var newEpiTask = new FetchTaskEpi(this, ani_id);
        taskQueue.add(newEpiTask);
    }

    /**
     *  通过 BlockData 添加多个 FetchTaskEpi 到任务队列
     * @param blockData
     */
    public void createFetchTaskEpi(BlockData blockData) {

        // 参数检查
        if(bufferUpsert == null || bufferUpdate == null || blockData == null) return;

        // 确保关键字段存在
        var ani_id_index = blockData.GetHeaderIndex("ANI_ID");
        if(ani_id_index == -1) return;

        // 创建任务
        for(var row : blockData.GetData()) {
            Integer ani_id;
            try {
                ani_id = Integer.parseInt(row[ani_id_index]);
            } catch(NumberFormatException e) {
                System.err.println("Invalid ANI_ID: " + row[ani_id_index]);
                continue; // 跳过无效数据
            }
            taskQueue.add(new FetchTaskEpi(this, ani_id));
        }
    }

    /**
     * 添加 FetchTaskTor 到任务队列
     */
    public void addFetchTaskAniTor(String url_rss, Integer ani_id) {
        var newTorTask = new FetchTaskAniTor(this, url_rss, ani_id);
        taskQueue.add(newTorTask);
    }

    /**
     * 通过 BlockData 添加任务s
     * @param blockData
     */
    public void createFetchTaskAniTor(BlockData blockData) {

        // 参数检查
        if(bufferUpsert == null || bufferUpdate == null || blockData == null) return;

        // 确保关键字段存在
        var ani_id_index  = blockData.GetHeaderIndex("ANI_ID");
        var url_rss_index = blockData.GetHeaderIndex("url_rss");
        if(ani_id_index == -1 || url_rss_index == -1) return;

        // 创建任务
        for(var row : blockData.GetData()) {
            Integer ani_id;
            try {
                ani_id = Integer.parseInt(row[ani_id_index]);
            } catch(NumberFormatException e) {
                // System.err.println("Invalid ANI_ID: " + row[ani_id_index]);
                continue; // 跳过无效数据
            }
            String url_rss = row[url_rss_index];
            if(url_rss == null || url_rss.isBlank()) {
                // System.err.println("Empty url_rss for ANI_ID: " + ani_id);
                continue; // 跳过无效数据
            } else {

                // 根据分号分割多个 RSS URL
                for(var url : url_rss.split(";")) {
                    var trimmedUrl = url.trim();
                    if(!trimmedUrl.isEmpty()) {
                        taskQueue.add(new FetchTaskAniTor(this, trimmedUrl, ani_id));
                    }
                }
            }
        }
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
