package Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract
class TaskFetch extends TaskManager.Task
{
    protected final List<Map<String, String>> buffer = new ArrayList<>();

    public final
    List<Map<String, String>> getBuffer()
    { return buffer; }
}
