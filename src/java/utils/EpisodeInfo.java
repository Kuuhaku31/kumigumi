package utils;

import java.util.HashMap;
import java.util.Map;

public
class EpisodeInfo
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
}