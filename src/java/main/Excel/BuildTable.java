package Excel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

public class BuildTable {

    public static void main(String[] args) {
        File jsonFile = new File("resources/config.json");
        File outputTxt = new File("resources/output.txt");

        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode root = mapper.readTree(jsonFile);
            JsonNode columns = root.get("column");

            if (columns == null || !columns.isArray()) {
                throw new IllegalArgumentException("JSON 中缺少 column 数组");
            }

            StringBuilder headerLine = new StringBuilder();
            StringBuilder contentLine = new StringBuilder();

            Iterator<JsonNode> it = columns.elements();
            while (it.hasNext()) {
                JsonNode col = it.next();

                String name = col.get("name").asText();
                String context = col.get("context").asText();

                headerLine.append(name);
                contentLine.append(context);

                if (it.hasNext()) {
                    headerLine.append('\t');
                    contentLine.append('\t');
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputTxt))) {
                writer.write(headerLine.toString());
                writer.newLine();
                writer.write(contentLine.toString());
            }

            System.out.println("TXT 文件生成成功：" + outputTxt.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
