// Test01.java


void main(String[] args)
{
    System.out.println(Arrays.toString(args));

    var en = EnumTest.valueOf("a");


    System.out.println(en);
}

enum EnumTest
{
    A,
    B,
    C
}