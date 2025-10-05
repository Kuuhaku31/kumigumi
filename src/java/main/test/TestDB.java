// TestDB.java

package test;


import Database.MySQLDemo;

import java.util.Arrays;

public
class TestDB
{
    static
    void main(String[] args)
    {
        IO.println("TestDB:");
        IO.println(Arrays.toString(args));

        MySQLDemo demo = new MySQLDemo();
        demo.Connect();
        demo.PrintData();
        demo.Disconnect();
    }
}
