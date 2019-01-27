package lib;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import java.awt.*;


public class HtmlRender {
    public static void main(String[] args) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse("This is *Sparta*");
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String text = renderer.render(document);  // "<p>This is <em>Sparta</em></p>\n"
        text = "<html>" + text + "</html>";

        JEditorPane editor = new JEditorPane();
        editor.setContentType( "text/html" );
        editor.setText(text);
        editor.setEditable(false);

        JFrame frame = new JFrame();
        frame.add(editor);
        frame.setSize(600, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static String getHtml(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String text = renderer.render(document);  // "<p>This is <em>Sparta</em></p>\n"
        text = "<html>" + text + "</html>";
        return text;

    }

}
