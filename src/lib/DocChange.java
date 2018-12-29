package lib;

import java.io.Serializable;

public class DocChange implements Serializable {
    public static final int INSERT = 0;


    public static final int REMOVE = 1;

    public DocChange(int offset, String change) {
        this.start = offset;
        this.type = INSERT;
        this.change = change;
    }

    public DocChange(int start, int end) {
        this.type = REMOVE;
        this.start = start;
        this.end = end;
    }

    public String getChange() {
        return change;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getType() {
        return type;
    }

    private String change;
    private int start;
    private int end;
    private int type;
}
