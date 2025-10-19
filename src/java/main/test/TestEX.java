package test;

import Database.MySQLAccess;

import java.io.IOException;
import java.sql.SQLException;

public
class TestEX
{
    void main(String[] args) throws IOException, SQLException
    {
        IO.println("TestEX:");
        String file_path = args[0];

        Excel.ExcelReader reader = new Excel.ExcelReader(file_path);

        var res = reader.ReadData();

        MySQLAccess dba = new MySQLAccess();
        dba.Open();

        for(var table_data : res)
        {
            table_data.PrintInfo();
            dba.Upsert(table_data);
        }
    }
}
