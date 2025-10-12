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
        task.PrintInfo();
        IO.println("----- Running Task -----");
        task.Run();
        IO.println("----- Finished Task -----");
        task.PrintInfoShort();

    }


}
