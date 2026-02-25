package Main;

import java.util.ArrayList;
import java.util.List;

public class ExportTorrent {

    /**
     * 导出种子文件
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("正在导出种子文件...");

        // 参数检查
        if(args.length != 3) {
            System.err.println("参数错误: 需要三个参数 - [dt_file_path] [db_path] [export_dir]");
            System.err.println("示例: java ExportTorrent dt.txt kumigumi.db export/");
            return;
        }

        var dt_file_path = args[0];
        var db_path = args[1];
        var export_dir = args[2];

        // 确保导出目录存在
        var exportDirFile = new java.io.File(export_dir);
        if(!exportDirFile.exists()) {
            if(exportDirFile.mkdirs()) {
                System.out.println("创建导出目录: " + export_dir);
            } else {
                System.err.println("创建导出目录失败: " + export_dir);
                return;
            }
        }

        // 读取文件内容
        List<String> hashList = new ArrayList<>();
        try(var reader = new java.io.BufferedReader(new java.io.FileReader(dt_file_path))) {
            String line;
            while((line = reader.readLine()) != null) {
                hashList.add(line.trim());
            }
        } catch(Exception e) {
            System.err.println("读取文件失败: " + e.getMessage());
            return;
        }

        // 访问数据库
        try(var db = new Database.SQLiteAccess(db_path)) {
            db.exportTorrentFiles(hashList, export_dir);
        } catch(Exception e) {
            System.err.println("数据库操作失败: " + e.getMessage());
        }
    }
}
