package net;

import java.io.IOException;
import java.net.URISyntaxException;

import Main.ItemTranslation;
import MetaData.TestMetaData;
import NetAccess.NetAccess;

public class TestNet {
    public static void main(String[] args) throws URISyntaxException, IOException {
        System.out.println("Starting TestNetAccess...");

        var meta = TestMetaData.meta_公主管弦乐;
        var epi_info_list = NetAccess.FetchEpisodeInfo(meta.ANI_ID);
        for (var map : epi_info_list) {
            for (var entry : map.entrySet()) {
                // var key = entry.getKey();
                var value = entry.getValue();

                if (value != null && value.equals("1570388")) {
                    System.out.println("Found episode with ID 1570388");
                }
            }
        }

        for (var epi : epi_info_list) {
            var infoItem = ItemTranslation.transieEpiUpsert(epi);
            System.out.println(infoItem);

            var infoItem2 = ItemTranslation.convertInfoEpiFetch(epi);
            System.out.println(infoItem2);
        }
    }
}