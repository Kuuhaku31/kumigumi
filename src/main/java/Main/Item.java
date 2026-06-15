package Main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Info.BaseInfo;
import Main.FetchTask.FetchInfoTask;
import Utils.Printable;
import Utils.DataBlock;


abstract class Item implements Printable {

    @Override
    public abstract String toPrintString(String indent, boolean enable_color);

    @Override
    public String toPrintString() {
        return toPrintString("", true);
    }

    @Override
    public String toPrintString(String indent) {
        return toPrintString(indent, true);
    }

    @Override
    public String toPrintString(boolean enable_color) {
        return toPrintString("", enable_color);
    }

    @Override
    public String toString() {
        return toPrintString("", false);
    }

    protected static String joinPrintable(Iterable<? extends Printable> values, String indent, boolean enable_color) {

        var sb = new StringBuilder();
        var first = true;
        for(var value : values) {
            if(!first) sb.append("\n");
            if(value == null) sb.append(indent).append("null");
            else sb.append(value.toPrintString(indent, enable_color));
            first = false;
        }
        return sb.toString();
    }
}


class StringItem extends Item {
    final String data;
    StringItem(String data) { this.data = data; }

    @Override
    public String toPrintString(String indent, boolean enable_color) {
        return indent + String.valueOf(data);
    }

    static Map<String, StringItem> parse(Map<String, String> input) {
        Map<String, StringItem> result = new HashMap<>();
        for(var entry : input.entrySet()) {
            result.put(entry.getKey(), new StringItem(entry.getValue()));
        }
        return result;
    }
}


class InfoSetItem extends Item {
    final Set<BaseInfo> data = new HashSet<>();

    @Override
    public String toPrintString(String indent, boolean enable_color) {
        return joinPrintable(data, indent, enable_color);
    }
}


class TaskSetItem extends Item {
    final Set<FetchInfoTask> data = new HashSet<>();

    @Override
    public String toPrintString(String indent, boolean enable_color) {
        return joinPrintable(data, indent, enable_color);
    }
}


class DataBlockItem extends Item {
    final DataBlock data;
    DataBlockItem(DataBlock data) { this.data = data; }

    @Override
    public String toPrintString(String indent, boolean enable_color) {
        if(data == null) return indent + "null";
        return data.toPrintString(indent, enable_color);
    }

    static Map<String, DataBlockItem> parse(Map<String, DataBlock> input) {
        Map<String, DataBlockItem> result = new HashMap<>();
        for(var entry : input.entrySet()) {
            result.put(entry.getKey(), new DataBlockItem(entry.getValue()));
        }
        return result;
    }
}
