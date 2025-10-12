// TestAPI.java

package test;

import utils.Task;

public
class TestAPI
{
    static
    void main(String[] args)
    {
        Task task = new Task(455454, "https://mikanani.me/RSS/Bangumi?bangumiId=3698");
        task.PrintInfo();
        task.Run();
        task.PrintInfo();

    }


}
