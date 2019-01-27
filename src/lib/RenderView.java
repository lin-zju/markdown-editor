package lib;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;

public class RenderView extends JEditorPane {
    private Model model;

    public RenderView(Model model) {
        setFont(new Font("Monospaced", Font.PLAIN, 16));
        setContentType( "text/html" );
        setModel(model);
        setEditable(false);
    }


    public void setModel(Model model) {
        this.model = model;
        model.addActionListener(
                e -> {
                    String text = null;
                    text = model.getDoc().getText();
                    this.setText(HtmlRender.getHtml(text));
                    this.repaint();
                });
    }
}
