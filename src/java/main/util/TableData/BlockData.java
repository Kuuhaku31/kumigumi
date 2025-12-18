package util.TableData;

// 用于插入/更新的数据
public class BlockData extends TableData {
    public final String block_name;

    public BlockData(String block_name, String[] headers) {
        this.block_name = block_name;
        super(headers);
    }

    @Override
    public String toString() {
        var title = "===" + this.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(this))
                + "===";

        return title + "\n" + "block name: " + block_name + "\n" + GetDataStr();
    }
}
