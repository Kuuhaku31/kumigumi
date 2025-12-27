package net;

import java.io.IOException;
import java.net.URISyntaxException;

import static NetAccess.NetAccess.*;

import MetaData.TestMetaData;

public class TestNetAccess {

    static void main() throws URISyntaxException, IOException {

        var meta = TestMetaData.meta_公主管弦乐;

        // var ani_info = FetchAnimeInfo(meta.ANI_ID);
        var epi_info = FetchEpisodeInfo(meta.ANI_ID);
        // var tor_info = FetchTorrentInfo(meta.url_rss, meta.ANI_ID);

        for (var map : epi_info) {
            // System.out.println("--- Map ------------------------------------------");
            for (var entry : map.entrySet()) {
                // var key = entry.getKey();
                // var value = entry.getValue();
                // System.out.println("[" + key + "]: " + value);
                var key = entry.getKey();
                if (key != null && key.equals("1570388")) {
                    System.out.println("Found episode with ID 1570388");
                }
            }
        }

        // printMap(ani_info);
        util.Util.printMapList(epi_info);
        // printMapList(tor_info);

        // var aniFetch = convertInfoAniFetch(ani_info);
        // List<InfoEpiFetch> epiFetchLis = new ArrayList<>();
        // for (var epi : epi_info) {
        // epiFetchLis.add(convertInfoEpiFetch(epi));
        // }
        // List<InfoTorFetch> torFetchLis = new ArrayList<>();
        // for (var tor : tor_info) {
        // torFetchLis.add(convertInfoTorFetch(tor));
        // }

        // // 将结果输出到文件
        // var outputPath = "ignore/net_test_output.txt";
        // try (var writer = new java.io.BufferedWriter(new
        // java.io.FileWriter(outputPath))) {
        // writer.write("Anime Info:\n");
        // writer.write(aniFetch.toString() + "\n\n");

        // writer.write("Episode Info:\n");
        // for (var epiFetch : epiFetchLis) {
        // writer.write(epiFetch.toString() + "\n");
        // }
        // writer.write("\n");

        // writer.write("Torrent Info:\n");
        // for (var torFetch : torFetchLis) {
        // writer.write(torFetch.toString() + "\n");
        // }
        // }
    }
}
