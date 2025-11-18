package utils.task;

import java.util.List;
import java.util.Map;

public abstract
class TaskFetch extends TaskManager.Task
{
    protected final List<Map<String, String>> buffer;

    public
    TaskFetch(List<Map<String, String>> buffer)
    { this.buffer = buffer; }
}
