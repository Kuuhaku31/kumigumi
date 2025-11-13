package Task;

public
class Test
{
    static
    void main()
    {
        System.out.println("Task.Test.main()");

        // TaskManager.MyTask t = new TheTask();
        for(int i = 0; i < 5; i++) new TheTaskA(i);

        new TheTaskB("aa");
        new TheTaskB("aB");
        new TheTaskB("BNB");

        TaskManager.ShowAllTasks();
        TaskManager.RunAllTasks();
        TaskManager.ShowAllTasks();
    }
}


class TheTaskA extends TaskManager.MyTask
{
    public final int id;

    public
    TheTaskA(int id)
    {
        this.id = id;
    }

    @Override
    public
    void run()
    {
        if(is_completed) return;

        System.out.println("Running TheTaskA " + id);

        is_completed = true;
        Remove();
    }

    public
    @Override
    String toString()
    { return "TheTaskA: " + GetStatusStr(); }


    @Override
    protected
    String GetStatusStr()
    { return super.GetStatusStr() + "id=" + id + " "; }
}


class TheTaskB extends TaskManager.MyTask
{
    public final String name;

    public
    TheTaskB(String name)
    {
        this.name = name;
    }

    @Override
    public
    void run()
    {
        if(is_completed) return;

        System.out.println("Running TheTaskB " + name);

        new TheTaskC();

        is_completed = true;
    }

    public
    @Override
    String toString()
    { return "TheTaskB: " + GetStatusStr(); }


    @Override
    protected
    String GetStatusStr()
    { return super.GetStatusStr() + "name=" + name + " "; }
}


class TheTaskC extends TaskManager.MyTask
{
    @Override
    public
    void run()
    {
        if(is_completed) return;

        System.out.println("Running TheTaskC!!!");

        is_completed = true;
    }

    public
    @Override
    String toString()
    { return "TheTaskC: " + super.GetStatusStr(); }
}