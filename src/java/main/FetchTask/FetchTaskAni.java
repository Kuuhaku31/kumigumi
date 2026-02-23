package FetchTask;

import java.io.IOException;
import java.net.URISyntaxException;

import InfoItem.InfoAni.InfoAni;
import InfoItem.InfoAni.InfoAniFetch;
import NetAccess.NetAccess;


class FetchTaskAni extends FetchTask {

    // 任务参数
    final Integer ani_id;

    /**
     * 构造函数：创建 FetchTaskAni 实例
     * @param manager
     * @param ani_id
     */
    public FetchTaskAni(FetchTaskManager manager, Integer ani_id) {
        super(manager);       // 调用外部类构造函数
        this.ani_id = ani_id; // 初始化 ani_id
    }

    @Override
    public void run() {
        try {
            var aniInfo = NetAccess.FetchAnimeInfo(ani_id);
            manager.bufferUpsert.add(new InfoAni(aniInfo));
            manager.bufferUpdate.add(new InfoAniFetch(aniInfo));
        } catch(URISyntaxException | IOException e) {
            System.err.println("Error fetching anime info for ANI_ID=" + ani_id + ": " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "FetchTaskAni{ANI_ID=" + ani_id + "}";
    }
}
