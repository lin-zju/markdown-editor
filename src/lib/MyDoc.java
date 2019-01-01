package lib;

import java.io.Serializable;

public class MyDoc implements Serializable {
    public static final long serialVersionUID = 234L;

    public MyDoc() {
        text = "";
    }

    public String getText() {
        return text;
    }

    public MyDoc(String text) {
        this.text = text;
    }

    private String text;

}
