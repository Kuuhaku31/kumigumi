package utils;

import java.util.HashMap;
import java.util.Map;

public
class TorrentInfo
{
    public Map<String, String> info = new HashMap<>();

    public
    void PrintInfo()
    {
        for(Map.Entry<String, String> entry : info.entrySet())
        {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    public
    void Translate()
    {
        info = Translate.TranslateMap(info);
    }
}
