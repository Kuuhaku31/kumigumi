// ColumnList.java


package Excel;

import java.util.ArrayList;

public
class ColumnList
{
    private final ArrayList<ColumnMap> list = new ArrayList<>();

    public
    void Add(String column_name, int column_index, String data_type)
    {
        list.add(new ColumnMap(column_name, column_index, data_type));
    }

    // 迭代器
    public
    Iterable<ColumnMap> GetList()
    {
        return list;
    }
}

