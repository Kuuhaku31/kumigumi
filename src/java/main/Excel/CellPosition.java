package Excel;

class CellPosition {
    private String sheetName = "main";
    private int row = 0;
    private int col = 0;

    String sheetName() {
        return sheetName;
    }

    int row() {
        return row;
    }

    int col() {
        return col;
    }

    void gotoPosition(int r, int c, String name) {
        row = r;
        col = c;
        sheetName = name == null ? sheetName : name;
    }

    void gotoNextRow() {
        row++;
    }

    CellPosition() {
    }

    CellPosition(int r, int c, String name) {
        row = r;
        col = c;
        sheetName = name;
    }
}
