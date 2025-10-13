// TestAPI.java

package test;

import utils.Task;

import java.util.Arrays;

public
class TestAPI
{
    static
    void main(String[] args)
    {
        IO.println(Arrays.toString(args));
        IO.println("----- Creating Task -----");
        Task task = new Task(455454, "https://mikanani.me/RSS/Bangumi?bangumiId=3698");
        // Task task = new Task(507634, "https://mikanani.me/RSS/Bangumi?bangumiId=3774");
        task.PrintInfo();
        IO.println("----- Running Task -----");
        task.GetInfo();
        IO.println("----- Finished Task -----");
        task.PrintInfoShort();
        IO.println("----- Upsert to MySQL -----");
        task.UpsertToDB();

    }


}
