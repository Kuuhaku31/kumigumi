package utils;// utils.TableData.java


import java.util.ArrayList;
import java.util.List;

public
class TableData
{
    private final String[]       headers;
    private final List<String[]> data = new ArrayList<>();

    public
    TableData(String[] headers) { this.headers = headers.clone(); }

    public
    List<String[]> GetData() { return data; }

    public
    String[] GetHeaders() { return headers; }

    @Override
    public
    String toString()
    {
        var title = "===" + this.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(this)) + "===";

        return title + "\n" + GetDataStr() + "\n" + "=".repeat(title.length());
    }

    protected
    String GetDataStr()
    {
        StringBuilder sb = new StringBuilder();

        for(var header : headers) sb.append(header).append(" ");
        sb.append("\n");

        for(var row : data)
        {
            for(var value : row) sb.append(value).append(" ");
            sb.append("\n");
        }

        return sb.toString();
    }

    public
    class Record
    {
        private final String[] values = new String[headers.length];

        public
        Record() { data.add(values); }

        // 添加记录
        public
        void Set(String header, String value)
        {
            for(var i = 0; i < headers.length; ++i)
            {
                if(headers[i].equals(header))
                {
                    values[i] = value;
                    return;
                }
            }
        }
    }

}
