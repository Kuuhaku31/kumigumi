package utils.task;

public abstract
class KGTask implements Runnable
{
    protected boolean is_completed = false;
    
    public
    boolean IsCompleted() { return this.is_completed; }

    @Override
    public
    String toString()
    { return "KMTask"; }
}
