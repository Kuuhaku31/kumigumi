package Main;

import java.util.ArrayList;
import java.util.List;

import Database.AnimeInfo;
import Database.EpisodeInfo;
import Database.EpisodeRecordInfo;
import Database.RSSInfo;
import Database.TorrentInfo;
import Database.TorrentPageInfo;


final class DatabaseBatch {

    final List<AnimeInfo>         animeItems       = new ArrayList<>();
    final List<EpisodeInfo>       episodeItems     = new ArrayList<>();
    final List<EpisodeRecordInfo> episodeRecords   = new ArrayList<>();
    final List<RSSInfo>           rssItems         = new ArrayList<>();
    final List<TorrentPageInfo>   torrentPageItems = new ArrayList<>();
    final List<TorrentInfo>       torrentItems     = new ArrayList<>();

    void addAll(DatabaseBatch other) {
        animeItems.addAll(other.animeItems);
        episodeItems.addAll(other.episodeItems);
        episodeRecords.addAll(other.episodeRecords);
        rssItems.addAll(other.rssItems);
        torrentPageItems.addAll(other.torrentPageItems);
        torrentItems.addAll(other.torrentItems);
    }

    boolean isEmpty() {
        return animeItems.isEmpty()
            && episodeItems.isEmpty()
            && episodeRecords.isEmpty()
            && rssItems.isEmpty()
            && torrentPageItems.isEmpty()
            && torrentItems.isEmpty();
    }

    List<Object> allItems() {
        var result = new ArrayList<Object>();
        result.addAll(animeItems);
        result.addAll(episodeItems);
        result.addAll(episodeRecords);
        result.addAll(rssItems);
        result.addAll(torrentPageItems);
        result.addAll(torrentItems);
        return result;
    }
}
