package Task;

import java.util.Set;

import Database.Info.BaseInfo;


public abstract class FetchInfoTask extends Task {
    public abstract Set<? extends BaseInfo> GetInfoSet();
}
