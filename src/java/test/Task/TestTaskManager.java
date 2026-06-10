package Task;

import java.util.Map;
import java.util.Set;

import Util.ColorCode;
import Util.Util;

import static Util.ColorCode.BLUE;
import static Util.ColorCode.BOLD_BLUE;
import static Util.ColorCode.BOLD_YELLOW;
import static Util.Util.color;


public class TestTaskManager {
    public static void main(String[] args) throws Exception {

        // System.out.println("测试 TaskManager...");
        // System.out.println("测试 TaskManager...");
        // System.out.print("\033[1A");
        // System.out.println("xxxxxxxxxxxxxx");
        // System.out.println("xxxxxxxxxxxxxx");
        // System.exit(0);

        // 创建任务集合
        Set<Task> tasks = new java.util.HashSet<>();
        for(var i = 1; i <= 100; i++) tasks.add(new DownloadTask(i, "DownloadTask_" + i));

        System.out.println("任务集合: ");
        for(Task task : tasks) {
            System.out.println(color(task.toString(), BLUE));
        }

        // 并行处理任务
        Task.ParallelExecution(tasks);

        // 打印结果
        for(Task task : tasks) System.out.println(task);

        System.out.println("测试完成");
    }
}


class DownloadTask extends Task {

    private final int    id;
    private final String name;
    private final long   sleepTime;
    private String       msg;


    // 构造函数接受任务名称和两个计数器，用于统计开始和完成的任务数量
    public DownloadTask(int id, String name) {
        this.id        = id;
        this.name      = name;
        this.sleepTime = (long)(Math.random() * 3000) + 500; // 模拟随机执行时间
    }

    @Override
    public void execute() {

        start();

        try {
            Thread.sleep(sleepTime);
        } catch(InterruptedException e) {
        }

        // 故意制造一个失败的任务
        if(name.equals("DownloadTask_2")) {
            var err_msg = color("一个故意的错误", BOLD_YELLOW);
            msg         = "DownloadTask: " + color(name, BOLD_BLUE) + " 执行失败: " + err_msg;
            msg         = color(msg, ColorCode.GRAY);

            fail();
        } else {
            msg = "DownloadTask: " + color(name, BOLD_BLUE) + " 执行成功";

            complete();
        }
    }

    // 重写 equals 和 hashCode 方法，确保任务唯一性基于 id
    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;

        DownloadTask other = (DownloadTask)obj;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public Map<String, Object> getInfo() {
        var info = super.getInfo();
        info.put("ID", id);
        info.put("Name", name);
        info.put("SleepTime", sleepTime);
        info.put("Message", msg);
        return info;
    }

    @Override
    public String toString() {
        var info = getInfo();
        return "DownloadTask" + Util.getInfoString(info);
    }
}
