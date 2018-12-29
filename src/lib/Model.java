package lib;

import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Model {

    private Document doc;
    private String title = "untitled.md";
    private boolean modified = false;
    private ArrayList<ActionListener> actionListenerList = new ArrayList<>();
    private DocumentListener documentListener;

    public void setDocumentListener(DocumentListener documentListener) {
        this.documentListener = documentListener;
    }

    public void initDoc(Document doc) {
        this.doc = doc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        triggerEvent();
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
        triggerEvent();
    }

    public Document getDoc() {
        return doc;
    }

    public void setDoc(Document doc) {
        try {
            this.doc.removeDocumentListener(documentListener);
            this.doc.remove(0, this.doc.getLength());
            this.doc.insertString(0, doc.getText(0, doc.getLength()), null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        } finally {
            this.doc.addDocumentListener(documentListener);
        }

        triggerEvent();
    }

    public void changeDoc(DocChange c) {
        try {
            // must disable change event to be fired
            doc.removeDocumentListener(documentListener);
            if (c.getType() == DocChange.INSERT) {
                doc.insertString(c.getStart(), c.getChange(), null);
            } else if (c.getType() == DocChange.REMOVE) {
                doc.remove(c.getStart(), c.getLength());
            }
            triggerEvent();
        } catch (BadLocationException e) {
            e.printStackTrace();
        } finally {
            doc.addDocumentListener(documentListener);
        }
    }


    public synchronized void addActionListener(ActionListener l) {
        actionListenerList.add(l);
    }

    private void triggerEvent() {
        ArrayList<ActionListener> list = null;

        synchronized (this) {
            list = (ArrayList<ActionListener>) actionListenerList.clone();
        }

        for (ActionListener listener : list) {
            listener.actionPerformed(
                    new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "doc change"));
        }

    }

}

