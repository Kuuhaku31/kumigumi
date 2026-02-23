package FetchTask;


abstract class FetchTask implements Runnable {

    // 引用外部类实例
    final FetchTaskManager manager;

    /**
     * 构造函数：创建 FetchTask 实例
     * @param manager
     */
    public FetchTask(FetchTaskManager manager) {
        this.manager = manager;
    }
}
