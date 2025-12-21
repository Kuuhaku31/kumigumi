package net;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import Database.InfoItem.InfoEpi.InfoEpiFetch;
import Database.InfoItem.InfoTor.InfoTorFetch;

import static NetAccess.NetAccess.*;
import static util.Util.printMap;
import static util.Util.printMapList;
import static Main.TableToInfo.convertInfoAniFetch;
import static Main.TableToInfo.convertInfoEpiFetch;
import static Main.TableToInfo.convertInfoTorFetch;

import MetaData.TestMetaData;

public class TestNetAccess {

    static void main() throws URISyntaxException, IOException {

        var meta = TestMetaData.meta_笑容职场;

        var ani_info = FetchAnimeInfo(meta.ANI_ID);
        var epi_info = FetchEpisodeInfo(meta.ANI_ID);
        var tor_info = FetchTorrentInfo(meta.url_rss, meta.ANI_ID);
        printMap(ani_info);
        printMapList(epi_info);
        // printMapList(tor_info);

        var aniFetch = convertInfoAniFetch(ani_info);
        List<InfoEpiFetch> epiFetchLis = new ArrayList<>();
        for (var epi : epi_info) {
            epiFetchLis.add(convertInfoEpiFetch(epi));
        }
        List<InfoTorFetch> torFetchLis = new ArrayList<>();
        for (var tor : tor_info) {
            torFetchLis.add(convertInfoTorFetch(tor));
        }

        // 将结果输出到文件
        var outputPath = "ignore/net_test_output.txt";
        try (var writer = new java.io.BufferedWriter(new java.io.FileWriter(outputPath))) {
            writer.write("Anime Info:\n");
            writer.write(aniFetch.toString() + "\n\n");

            writer.write("Episode Info:\n");
            for (var epiFetch : epiFetchLis) {
                writer.write(epiFetch.toString() + "\n");
            }
            writer.write("\n");

            writer.write("Torrent Info:\n");
            for (var torFetch : torFetchLis) {
                writer.write(torFetch.toString() + "\n");
            }
        }
    }
}
