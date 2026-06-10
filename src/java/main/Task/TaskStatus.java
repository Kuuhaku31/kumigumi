package Task;

import Util.Util;

import static Util.ColorCode.BOLD_RED;
import static Util.ColorCode.GRAY;
import static Util.ColorCode.GREEN;


public enum TaskStatus {
    NOT_STARTED,
    SUCCEEDED,
    FAILED;

    @Override
    public String toString() {
        return switch(this) {
            case NOT_STARTED -> Util.color("NOT_STARTED", GRAY);
            case SUCCEEDED -> Util.color("SUCCEED", GREEN);
            case FAILED -> Util.color("FAILED", BOLD_RED);
        };
    }
}
