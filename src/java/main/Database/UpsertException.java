package Database;


// 插入异常
public
class UpsertException extends Exception
{
    public final int      failedRowIndex;
    public final String[] failedRowData;

    public
    UpsertException(String message, int failedRowIndex, String[] failedRowData, Throwable cause)
    {
        super(message, cause);
        this.failedRowIndex = failedRowIndex;
        this.failedRowData  = failedRowData;
    }
}