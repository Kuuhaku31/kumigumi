package Excel;

import java.util.Date;

public class TestExcelDataBlock {
    public static void main(String[] args) {
        System.out.println("ExcelDataBlock test.");
        var headers = java.util.Arrays.asList("ID", "Name", "Age", "Datetime");
        var block = new ExcelDataBlock("TestBlock", headers);

        var datetimeNow = new Date();
        System.out.println("Current DateTime: " + datetimeNow.toString());

        var record1 = block.new Record();
        System.out.println(record1);
        record1.Set("ID", 1);
        record1.Set("Name", "Alice");
        record1.Set("Age", 30);
        record1.Set("Datetime", datetimeNow);
        System.out.println(record1);

        var record2 = block.new Record();
        record2.Set("ID", 2);
        record2.Set("Name", "Bob");
        record2.Set("Age", 25);
        record2.Set("Datetime", datetimeNow);
        System.out.println(record2);

        System.out.println(block);
    }
}
