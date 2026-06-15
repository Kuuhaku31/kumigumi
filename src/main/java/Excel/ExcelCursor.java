package Excel;


final class ExcelCursor {
    String sheetName = "main";
    int    row       = 0;
    int    col       = 0;

    ExcelCursor(int r, int c, String name) {
        row       = r;
        col       = c;
        sheetName = name;
    }

    void gotoPosition(int r, int c, String name) {
        row       = r;
        col       = c;
        sheetName = name == null ? sheetName : name;
    }

    void gotoNextRow() {
        row++;
    }


    // 返回类似 main!C9
    @Override
    public String toString() {
        return sheetName + "!" + (char)('A' + col) + (row + 1);
    }
}
