package FetchTask;

import java.util.List;

import Database.InfoItem.InfoItem;

public abstract class FetchTask implements Runnable {
    final List<InfoItem> buffer;

    public FetchTask(List<InfoItem> buffer) {
        this.buffer = buffer;
    }
}
