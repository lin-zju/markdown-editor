package lib;

import javax.print.Doc;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class State {

    protected Model model;
    protected File file = null;

    public State(Model model) {
        this.model = model;
    }

    public State(State s) {
        this.model = s.model;
        this.file = file;
    }

    // edit operation
    public State edit(DocChange c) {
        model.setModified(true);
        return this;
    }

    // open file
    public State open() {
        String title = "open file";
        String prompt =
                "open a file will cause change loss and connection loss, " +
                "are you sure to proceed?";
        boolean confirm = true;
        if (file != null && (model.isModified())) {
            confirm = getConfirmation(title, prompt);
        }
        if (confirm) {
            openFile();
        }
        return this;
    }

    // new file
    public State create() {
        String title = "new file";
        String prompt =
                "create a new file will cause change loss and connection loss, " +
                        "are you sure to proceed?";
        boolean confirm = true;
        if (file != null && (model.isModified())) {
            confirm = getConfirmation(title, prompt);
        }
        if (confirm) {
            createFile();
        }
        return this;
    }

    // save file
    public State save() {
        saveFile();
        return this;
    }

    // server mode
    public State serverMode() {
        return new Server(this);
    }

    // client mode
    public State clientMode() {
        return new Client(this);
    }

    // local mode
    public State localMode() {
        return this;
    }

    // export
    public State export() {
        return this;
    }

    protected void saveFile() {

        File selectedFile = file;
        // get file if the file is not selected
        if (selectedFile == null) {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "markdown (*.md)", "md");
            fileChooser.setFileFilter(filter);
            if (fileChooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            selectedFile = fileChooser.getSelectedFile();
        }
        if (!selectedFile.getName().endsWith(".md")) {
            selectedFile = new File(selectedFile.getAbsolutePath() + ".md");
        }

        try {
            BufferedWriter output =
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(selectedFile)));
            Document doc = model.getDoc();
            output.write(doc.getText(0, doc.getLength()));
            output.close();

            // set model modified flag to false
            file = selectedFile;
            model.setModified(false);
            model.setTitle(file.getName());
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    protected void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "markdown (*.md)", "md");
        fileChooser.setFileFilter(filter);
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedReader input =
                        new BufferedReader(
                                new InputStreamReader(
                                        new FileInputStream(selectedFile)));
                PlainDocument doc = new PlainDocument();
                String line;
                while ((line = input.readLine()) != null) {
                    doc.insertString(doc.getLength(), line + "\n", null);
                }
                input.close();
                this.file = selectedFile;
                // set model documentation
                model.setDoc(doc);
                model.setTitle(file.getName());
                model.setModified(false);

            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    protected boolean getConfirmation(String title, String message) {
        int option = JOptionPane.showConfirmDialog(
                null,
                message,
                title,
                JOptionPane.OK_CANCEL_OPTION
        );
        return option == JOptionPane.OK_OPTION;
    }

    protected void createFile() {
        file = null;
        model.setDoc(new PlainDocument());
        model.setTitle("untitled.md");
        model.setModified(true);
    }
}

class Share extends State {
    protected Socket s;
    protected boolean connected = false;
    protected ObjectInputStream in = null;
    protected ObjectOutputStream out = null;
    int serverPort = 8000;

    public Share(State s) {
        super(s);
    }

    @Override
    public State edit(DocChange c) {
        super.edit(c);
        if (connected) {
            try {
                out.writeObject(c);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this;
    }


    class ReceiveChange implements Runnable {
        ObjectInputStream in;
        public ReceiveChange(ObjectInputStream in) {
            this.in = in;
        }

        @Override
        public void run() {
            while (connected) {
                try {
                    System.out.println("Wait to receive");
                    Object o = in.readObject();
                    System.out.println("Received " + o.getClass());
                    if (o instanceof DocChange) {
                        model.changeDoc((DocChange) o);
                    }
                    else if (o instanceof Document){
                        model.setDoc((Document) o);
                    }
                    else {
                        throw new IOException("Cannot recognized object type");
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


class Local extends State {
    public Local(Model model) {
        super(model);
    }

    public Local(State s) {
        super(s);
    }
}

class Server extends Share {



    public Server(State state) {
        super(state);
        JOptionPane.showMessageDialog(null, "Enter server mode, wait to be conneced");
        new Thread(new GetConnection()).start();
    }

    class GetConnection implements Runnable {

        @Override
        public void run() {
            try {
                ServerSocket ss = new ServerSocket(serverPort);
                s = ss.accept();
                System.out.println("Accepted");
                connected = true;
                out = new ObjectOutputStream(s.getOutputStream());
                out.flush();
                in = new ObjectInputStream(s.getInputStream());
                System.out.println("hello?");
                out.writeObject(model.getDoc());
                out.flush();
                new Thread(new ReceiveChange(in)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, "connected to client");
        }
    }

}

class Client extends Share {

    InetAddress serverAddr;
    public Client(State state) {
        super(state);
        JOptionPane.showMessageDialog(null, "Enter client mode, wait to be conneced");
        try {
            serverAddr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        getConnection();
    }

    public void getConnection() {
        try {
            s = new Socket(serverAddr, serverPort);
            System.out.println("Connected");
            connected = true;
            out = new ObjectOutputStream(s.getOutputStream());
            out.flush();
            in = new ObjectInputStream(s.getInputStream());
            System.out.println("hello?");
            new Thread(new ReceiveChange(in)).start();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        JOptionPane.showMessageDialog(null, "connected to server");

    }
}
