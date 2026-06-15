package Task;

import java.util.Set;

import Database.Info.BaseInfo;
import Utils.Task;


public abstract class FetchInfoTask extends Task {
    public abstract Set<? extends BaseInfo> GetInfoSet();
}
