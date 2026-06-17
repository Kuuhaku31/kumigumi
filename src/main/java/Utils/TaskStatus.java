package Utils;

import static Utils.ColorCode.BOLD_RED;
import static Utils.ColorCode.GRAY;
import static Utils.ColorCode.GREEN;


public enum TaskStatus {
    NOT_STARTED,
    SUCCEEDED,
    FAILED;

    public String toPrintString() {
        return switch(this) {
            case NOT_STARTED -> UtilityFunctions.color("NOT_STARTED", GRAY);
            case SUCCEEDED -> UtilityFunctions.color("SUCCEED", GREEN);
            case FAILED -> UtilityFunctions.color("FAILED", BOLD_RED);
        };
    }

    @Override
    public String toString() {
        return switch(this) {
            case NOT_STARTED -> "NOT_STARTED";
            case SUCCEEDED -> "SUCCEEDED";
            case FAILED -> "FAILED";
        };
    }
}
