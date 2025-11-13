package Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public
class TaskManager
{
    private static final List<MyTask> tasks = new ArrayList<>();

    public static
    void RunAllTasks()
    {
        var pool = Executors.newFixedThreadPool(4);
        for(var task : tasks) pool.submit(task);
        pool.shutdown();

        // 等待全部完成
        var ok = false;
        try { ok = pool.awaitTermination(1, TimeUnit.MINUTES); }
        catch(InterruptedException e) { System.err.println(e.getMessage()); }

        if(ok) System.out.println("并发任务完成！");
        else System.err.println("出现异常");
    }

    public static
    void ShowAllTasks()
    {
        for(var task : tasks) { System.out.println(task); }
    }

    private static
    void AddTask(MyTask task)
    {
        tasks.add(task);
        System.out.println("[TaskManager]: Task added: " + task);
    }

    private static
    void RemoveTask(MyTask task)
    {
        tasks.remove(task);
        System.out.println("[TaskManager]: Task removed: " + task);
    }

    public abstract static
    class MyTask implements Runnable
    {
        protected boolean is_completed = false;

        public
        MyTask()
        { AddTask(this); }

        public final
        void Remove()
        { RemoveTask(this); }

        public final
        boolean IsCompleted()
        { return this.is_completed; }

        @Override
        public
        String toString()
        { return "MyTask: " + GetStatusStr(); }

        protected
        String GetStatusStr()
        { return "is_completed=" + is_completed + " "; }
    }
}
