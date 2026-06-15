module kumigumi {
    requires java.net.http;
    requires java.sql;
    requires static jdk.httpserver;

    requires com.apptasticsoftware.rssreader;
    requires org.apache.poi.ooxml;
    requires org.apache.poi.poi;
    requires org.json;
    requires org.xerial.sqlitejdbc;

    exports Main;
    exports Database;
    exports Database.Info;
    exports Excel;
    exports NetAccess;
    exports Utils;
}
