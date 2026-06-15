package Main.FetchTask;

import java.util.Set;

import Info.BaseInfo;
import Utils.Task;


// Main 内部抓取任务基类。Main.FetchTask 包不在 module-info.java 中导出。
public abstract class FetchInfoTask extends Task {
    public abstract Set<? extends BaseInfo> GetInfoSet();
}
