package lib;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Controller {

    State state = null;

    public Controller(Model model) {
        setModel(model);
    }

    public void setModel(Model model) {
        state = new Local(model);
    }

    public class OpenFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            state = state.open();
        }
    }

    public class SaveFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            state = state.save();
        }
    }

    public class CreateFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            state = state.create();
        }
    }

    public class EditFileListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            Document doc = e.getDocument();
            try {
                DocChange dc = new DocChange(e.getOffset(), doc.getText(e.getOffset(), e.getLength()));
                state = state.edit(dc);
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            Document doc = e.getDocument();
            DocChange dc = new DocChange(e.getOffset(),  e.getLength());
            state = state.edit(dc);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            // not used
        }
    }

    public class LocalModeListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            state = state.localMode();
        }
    }

    public class ServerModeListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            state = state.serverMode();
        }
    }

    public class ClientModeListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            state = state.clientMode();
        }
    }
}
