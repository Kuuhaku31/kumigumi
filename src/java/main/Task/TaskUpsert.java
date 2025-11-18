package Task;

import Database.KG_SQLiteAccess;
import Database.KG_SQLiteAccess.TableName;
import Database.UpsertException;
import util.TableData.TableData;

import java.sql.SQLException;

public
class TaskUpsert extends TaskManager.Task
{
    final TableName table_name;
    final TableData data;

    public
    TaskUpsert(TableName table_name, TableData data)
    {
        this.table_name = table_name;
        this.data       = data;
    }

    @Override
    public
    void run()
    {
        try { KG_SQLiteAccess.Upsert(table_name, data); }
        catch(SQLException | UpsertException e) // 如果出现异常则标记为失败
        {
            if(e instanceof UpsertException)
            {
                var failed_rows = ((UpsertException) e).failedRowData;
                addLog("Upsert failed for " + failed_rows.length + " rows.");
                for(var row : failed_rows)
                {
                    addLog("Failed row data: " + row);
                }
                failed();
            }
        }
    }

    @Override
    public
    String toString()
    {
        return "";
    }

    @Override
    protected
    String getStatusStr()
    {
        return "";
    }
}
