package Database.Info;

import java.time.OffsetDateTime;
import java.util.Date;

import Utils.ColorCode;
import Utils.UtilityFunctions;

public abstract class BaseInfo implements Utils.Printable {

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

    protected static String formatInfo(String typeName, String indent, boolean enable_color, Object[][] fields) {
        final var this_indent = "  ";
        var sb = new StringBuilder();

        sb.append(indent);
        sb.append(UtilityFunctions.color(typeName + ":\n", ColorCode.BOLD_GREEN, enable_color));

        for(var i = 0; i < fields.length; i++) {
            var field = fields[i];
            sb.append(indent).append(this_indent);
            sb.append(UtilityFunctions.color(field[0] + ":\t", ColorCode.BOLD_CYAN, enable_color));
            sb.append(UtilityFunctions.color(printableValue(field[1]), ColorCode.CYAN, enable_color));
            if(i < fields.length - 1) sb.append("\n");
        }

        return sb.toString();
    }

    private static String printableValue(Object value) {
        if(value == null) return "null";
        if(value instanceof Date date) return String.valueOf(UtilityFunctions.getDateString(date));
        if(value instanceof OffsetDateTime datetime) return String.valueOf(UtilityFunctions.getDateString(datetime));
        if(value instanceof byte[] bytes) return "[binary data: " + bytes.length + " bytes]";
        return UtilityFunctions.standardString(String.valueOf(value));
    }
}
