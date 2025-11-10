import Excel.ExcelReader;

void main()
{
    System.out.println("TestEX:");

    var path_str = "D:/OneDrive/kumigumi.xlsx";
    var path     = Path.of(path_str);

    var res = ExcelReader.Read(path);

    for(var row : res)
    {
        System.out.println(row);
    }
}
