package FetchTask;

import java.util.List;

import Database.InfoItem.UpdateItem;
import Database.InfoItem.UpsertItem;

abstract class FetchTask implements Runnable {
    final List<UpsertItem> bufferUpsert;
    final List<UpdateItem> bufferUpdate;

    public FetchTask(List<UpsertItem> bufferUpsert, List<UpdateItem> bufferUpdate) {
        this.bufferUpsert = bufferUpsert;
        this.bufferUpdate = bufferUpdate;
    }
}
