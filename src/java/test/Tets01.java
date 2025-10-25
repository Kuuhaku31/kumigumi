// Test01.java

void main(String[] args)
{
    IO.println(Arrays.toString(args));

    String[] str_list  = new String[] {"a", "b", "c"};
    String[] str_list2 = str_list;

    IO.println(Arrays.toString(str_list));
    IO.println(Arrays.toString(str_list2));

    str_list2 = null;

    IO.println(Arrays.toString(str_list));
    IO.println(Arrays.toString(str_list2));
}
