package Utils;


public interface Printable {
    public String toPrintString(String indent, boolean enable_color);
    public String toPrintString(String indent);
    public String toPrintString(boolean enable_color);
    public String toPrintString();
}
