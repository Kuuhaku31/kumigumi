// package db;

// import java.sql.SQLException;

// import Def.TestInfo;
// import InfoItem.InfoAni.InfoAni;

// public class TestDB {

//     static final String dbURL = "D:/repositories/kumigumi/resources/test.db";

//     public static void main(String[] args) throws SQLException {
//         System.out.println("TestDB");
//         testUpsert();
//         // testDelete();
//     }

//     static void testUpsert() throws SQLException {
//         System.out.println("TestDB.testUpsert()");
//         TestInfo.main(null);
//         try (@SuppressWarnings("unused")
//         var db = new Database.SQLiteAccess(dbURL)) {
//             System.out.println("Database opened.");
//             // db.Update(TestInfo.infoAniFetch);
//             // db.Update(TestInfo.infoEpiFetch);
//             // db.Update(TestInfo.infoTorFetch);

//             // db.Update(TestInfo.infoAniStore);
//             // db.Update(TestInfo.infoEpiStore);
//             // db.Update(TestInfo.infoTorStore);
//         }
//     }

//     static void testDelete() throws SQLException {
//         System.out.println("TestDB.testDelete()");

//         var infoAni = new InfoAni(14);
//         System.out.println(infoAni);

//         try (@SuppressWarnings("unused")
//         var db = new Database.SQLiteAccess(dbURL)) {
//             System.out.println("Database opened.");
//             // db.Delete(infoAni);
//         }
//     }
// }
