package Excel;

public class CellPosition {
    private int row = 0;
    private int col = 0;

    int row() {
        return row;
    }

    int col() {
        return col;
    }

    void gotoPosition(int r, int c) {
        row = r;
        col = c;
    }

    void gotoNextRow() {
        row++;
    }

    CellPosition() {
    }

    CellPosition(int r, int c) {
        row = r;
        col = c;
    }
}
