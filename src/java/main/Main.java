// Main.java

String URL = "https://mikanani.me/RSS/Bangumi?bangumiId=3671";
String URL2 = "https://bangumi.tv";
String URL3 = "https://nyaa.land";

void main(String... args)
{
    if(args.length > 0) IO.println(Arrays.toString(args));
    IO.println("Hello, World!?");
    // try
    // {
    //     String html = Net.GetHTML(URL3);
    //     IO.println(html);
    // }
    // catch(IOException e)
    // {
    //     IO.println("Error fetching HTML: " + e.getMessage());
    // }
    IO.println("Done.");
}
