package Task;

import java.util.List;

import Database.Item.UpdateItem;

public class TestFetchTask {

    static final String filename = "ignore/TestFetchTask_Output.txt";
    static final List<UpdateItem> buffer = new java.util.ArrayList<>();

    public static void main(String[] args) {
        System.out.println("TestFetchTask");

        // var meta = MetaData.TestMetaData.meta_永远的黄昏;
        // var meta = MetaData.TestMetaData.meta_笑容职场;
        // var meta = MetaData.TestMetaData.meta_千岁同学;

        // var fetchTaskAni = new FetchTask.FetchTaskAni(buffer, meta.ANI_ID);
        // var fetchTaskEpi = new FetchTask.FetchTaskEpi(buffer, meta.ANI_ID);
        // var fetchTaskTor = new FetchTask.FetchTaskTor(buffer, meta.url_rss,
        // meta.ANI_ID);
        // fetchTaskAni.run();
        // fetchTaskEpi.run();
        // fetchTaskTor.run();

        // printBufferToFile();
    }

    static void printBufferToFile() {
        System.out.println("Writing buffer to file: " + filename);
        try (var writer = new java.io.PrintWriter(new java.io.FileWriter(filename))) {
            for (var item : buffer) {
                writer.println(item);
            }
        } catch (java.io.IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
