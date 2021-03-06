package lib;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class EditorView extends JTextArea {
    private Model model;

    public EditorView(Model model) {
        setFont(new Font("Monospaced", Font.PLAIN, 16));
        setModel(model);
        setLineWrap(true);
    }


    public void setModel(Model model) {
        this.model = model;
        model.addActionListener(
                e -> {
                    this.repaint();
                });
    }
}
