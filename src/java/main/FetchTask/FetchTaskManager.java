package FetchTask;

import java.util.List;

public class FetchTaskManager {
    public void createTaskList() {

    }
}

class TaskList {
    final String listName;
    List<FetchTask> tasks;

    public TaskList(String listName) {
        this.listName = listName;
    }
}
