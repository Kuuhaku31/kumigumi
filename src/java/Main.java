//Main.java

import NetAccess.Net;

String URL = "https://baidu.com";

void main(String... args)
{
    if(args.length > 0) IO.println(Arrays.toString(args));
    IO.println("Hello, World!?");
    try
    {
        String html = Net.GetHTML(URL);
        IO.println(html);
    }
    catch(IOException e)
    {
        IO.println("Error fetching HTML: " + e.getMessage());
    }
    IO.println("Done.");
}