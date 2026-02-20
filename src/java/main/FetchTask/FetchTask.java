package FetchTask;

import Database.Item.UpdateItem;
import Database.Item.UpsertItem;
import java.util.List;


public abstract class FetchTask implements Runnable {
    final List<UpsertItem> bufferUpsert;
    final List<UpdateItem> bufferUpdate;

    public FetchTask(List<UpsertItem> bufferUpsert, List<UpdateItem> bufferUpdate) {
        this.bufferUpsert = bufferUpsert;
        this.bufferUpdate = bufferUpdate;
    }
}
