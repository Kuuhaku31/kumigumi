package test;

public
class TestEX
{
    void main(String[] args)
    {
        IO.println("TestEX:");
        String filePath = args[0];
        Excel.ExcelReader reader = new Excel.ExcelReader();
        reader.OpenFile(filePath);
    }
}
