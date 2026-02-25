package FetchTask;


abstract class FetchTask implements Runnable {

    // 引用外部类实例
    final FetchTaskManager manager;

    TaskStatus status = TaskStatus.NOT_STARTED; // 任务状态
    String log = ""; // 任务日志

    /**
     * 构造函数：创建 FetchTask 实例
     * @param manager
     */
    public FetchTask(FetchTaskManager manager) {
        this.manager = manager;
    }

    protected void taskFinally() {

        // manager.taskQueue.remove(this); // 从任务队列中移除当前任务
        manager.incrementFinished();    // 无论成功还是失败，都要增加已完成的任务计数

        // 根据任务状态将任务添加到对应的列表
        switch (status) {
            case SUCCESS -> manager.successTaskList.add(this);
            case FAIL    -> manager.failTaskList.add(this);
            default      -> manager.notStartTaskList.add(this);
        }
    }

    // 定义任务状态枚举
    enum TaskStatus {
        NOT_STARTED,
        SUCCESS,
        FAIL
    }
}
