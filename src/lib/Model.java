package lib;

import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Model {

    public final static int SERVER = 0;
    public final static int LOCAL = 1;
    public final static int CLIENT = 2;

    private Document doc;
    private String title = "untitled.md";
    private boolean modified = false;
    private int mode = LOCAL;
    private boolean connected = false;
    private int clientNum = 0;
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

    public MyDoc getDoc() {
        try {
            return new MyDoc(doc.getText(0, doc.getLength()));
        } catch (BadLocationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setDoc(MyDoc doc) {
        try {
            this.doc.removeDocumentListener(documentListener);
            this.doc.remove(0, this.doc.getLength());
            this.doc.insertString(0, doc.getText(), null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        } finally {
            this.doc.addDocumentListener(documentListener);
        }

        triggerEvent();
    }

    public void setMode(int mode) {
        this.mode = mode;
        triggerEvent();
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
        triggerEvent();
    }

    public void setClientNum(int clientNum) {
        this.clientNum = clientNum;
        triggerEvent();
    }



    public String getStatusText() {
        if (mode == LOCAL) {
            return "Local Mode";
        }
        else if (mode == SERVER) {
            return "Server Mode, " + clientNum + " connections.";
        }
        else
            return "Client Mode, " + (connected ? "" : "not ") + "connected";

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

