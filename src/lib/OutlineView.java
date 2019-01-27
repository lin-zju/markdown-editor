package lib;

import javax.print.Doc;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Scanner;

public class OutlineView extends JTextArea {
    private Model model;
    private ArrayList<Header> outline = new ArrayList<>();

    public OutlineView(Model model) {
        setFont(new Font("SansSerif", Font.BOLD, 14));
        setModel(model);
        setLineWrap(true);
        setEditable(false);
    }


    public void setModel(Model model) {
        this.model = model;
        model.addActionListener(
                e -> {
                    this.parseHeaders();
                    this.setOutline();
                    this.repaint();
                });
    }

    private void parseHeaders() {
        outline.clear();
        String text = model.getDoc().getText();
        Scanner in = new Scanner(new StringReader(text));
        while (in.hasNextLine()) {
            String line = in.nextLine();
            Header header = Header.parseHeader(line);
            if (header != null) {
                outline.add(header);
            }
        }
    }

    private void setOutline() {
        StringBuffer s = new StringBuffer();
        for (Header header : outline) {
            s.append(header + "\n");
        }
        this.setText(s.toString());
    }
}

class Header {
    private String header;
    private int level;

    public Header(String header, int level) {
        this.header = header;
        this.level = level;
    }

    public String getHeader() {
        return header;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        String tab = "";
        for (int i = 0; i < level; i++)
            tab += "  ";
        return tab + header;
    }

    static Header parseHeader(String line) {
        String prefix = "#";
        boolean isHeader = false;
        while (line.startsWith(prefix)) {
            prefix += "#";
            isHeader = true;
        }
        if (isHeader) {
            int level = prefix.length() - 1;
            return new Header(line.substring(level).trim(), level);
        }
        else
            return null;

    }

}
