package FetchTask;

import java.util.List;

import Database.InfoItem.UpdateItem;

public abstract class FetchTask implements Runnable {
    final List<UpdateItem> buffer;

    public FetchTask(List<UpdateItem> buffer) {
        this.buffer = buffer;
    }
}
