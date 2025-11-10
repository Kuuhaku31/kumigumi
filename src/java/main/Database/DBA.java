package Database;

import utils.TableData;

import java.util.List;

// 实现数据库功能接口
public
interface DBA
{
    void Open();  // 打开数据库连接
    void Close(); // 关闭连接

    boolean isOpen();

    default
    void Upsert(String table_name, List<TableData> data_list)
    {
        for(var data : data_list) Upsert(table_name, data);
    }

    void Upsert(String table_name, TableData data);
}