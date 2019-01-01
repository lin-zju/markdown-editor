package lib;

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
import java.util.ArrayList;
import java.util.Iterator;

public class State {

    protected boolean connected = false;
    protected Model model;
    protected File file = null;
    static final int serverPort = 8000;

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
        if (model.isModified() || connected) {
            confirm = getConfirmation(title, prompt);
        }
        if (confirm) {
            openFile();
        }
        return new Local(this);
    }

    // new file
    public State create() {
        String title = "new file";
        String prompt =
                "create a new file will cause change loss and connection loss, " +
                        "are you sure to proceed?";
        boolean confirm = true;
        if (model.isModified() || connected) {
            confirm = getConfirmation(title, prompt);
        }
        if (confirm) {
            createFile();
        }
        return new Local(this);
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
        return new Local(this);
    }

    // export html
    public State export() {
        // get file name
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "html (*.html)", "html");
        fileChooser.setFileFilter(filter);
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (!selectedFile.getName().endsWith(".html")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".html");
            }
            try {
                BufferedWriter output =
                        new BufferedWriter(
                                new OutputStreamWriter(
                                        new FileOutputStream(selectedFile)));
                // write the document to the file
                String text = model.getDoc().getText();
                String html = HtmlRender.getHtml(text);
                output.write(html);
                output.close();

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
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
            // write the document to the file
            output.write(model.getDoc().getText());
            output.close();

            file = selectedFile;
            // set model flags
            model.setModified(false);
            model.setTitle(file.getName());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    protected void openFile() {
        // get file name
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
                // create empty document
                StringBuffer stringBuffer = new StringBuffer();
                String line;
                // write content into the document
                while ((line = input.readLine()) != null) {
                    stringBuffer.append(line + "\n");
                }
                input.close();
                this.file = selectedFile;
                // set model data
                model.setDoc(new MyDoc(stringBuffer.toString()));
                model.setTitle(file.getName());
                model.setModified(false);

            } catch (IOException e1) {
                e1.printStackTrace();
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
        model.setDoc(new MyDoc());
        model.setTitle("untitled.md");
        model.setModified(true);
    }
}



class Local extends State {
    public Local(Model model) {
        super(model);
        connected = false;
        model.setMode(Model.LOCAL);
    }

    public Local(State s) {
        super(s);
        connected = false;
        model.setMode(Model.LOCAL);
    }
    // server mode
    public State serverMode() {
        return new Server(this);
    }

    // client mode
    public State clientMode() {
        return new Client(this);
    }
}

class Server extends State {

    private ArrayList<Session> sessionList = new ArrayList<>();
    ServerSocket ss = null;

    public Server(State state) {
        super(state);
        connected = true;
        model.setMode(Model.SERVER);
        model.setClientNum(0);
        JOptionPane.showMessageDialog(null, "Enter server mode, wait for clients");
        new Thread(new GetConnection()).start();
    }
    public State edit(DocChange c) {
        super.edit(c);
        broadCastChange(null, c);
        return this;
    }

    class GetConnection implements Runnable {

        @Override
        public void run() {
            try {
                ss = new ServerSocket(serverPort);
                while (connected) {
                    Socket s = ss.accept();
                    Session session = new Session(s);
                    Thread receiver = new Thread(new ReceiveChange(session, session.getInput()));
                    session.setThread(receiver);
                    // send initial file
                    session.getOutput().writeObject(model.getDoc());
                    // add to pool
                    sessionList.add(session);
                    session.start();
                    // set connected to true
//                    JOptionPane.showMessageDialog(null, "connected to client");
                    model.setClientNum(sessionList.size());
                }
            } catch (IOException e) {
            }
        }
    }
    class ReceiveChange implements Runnable {
        Session session;
        ObjectInputStream in;
        public ReceiveChange(Session session, ObjectInputStream in) {
            this.session = session;
            this.in = in;
        }

        @Override
        public void run() {
            while (connected) {
                if (session.isClosed()) {
                    break;
                }
                try {
                    Object o = in.readObject();
                    DocChange docChange = (DocChange) o;
                    model.changeDoc(docChange);
                    broadCastChange(in, docChange);
                }
                catch (IOException | ClassNotFoundException e) {
                    break;
                }
            }
        }
    }

    private void broadCastChange(InputStream sender, DocChange docChange) {
        // null sender to send to all
        Iterator<Session> iter = sessionList.iterator();
        while (iter.hasNext()) {
            Session session = iter.next();
            if (session.getInput() != sender) {
                try {
                    session.getOutput().writeObject(docChange);
                    session.getOutput().flush();
                } catch (IOException e) {
                    iter.remove();
                    model.setClientNum(sessionList.size());
                }

            }
        }
    }


    private void cleanSession() {
        try {
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Session session : sessionList) {
            session.close();
        }
        connected = false;
        model.setConnected(false);

    }

    @Override
    public State create() {
        cleanSession();
        return super.create();
    }

    @Override
    public State open() {
        cleanSession();
        return super.open();
    }
    @Override
    public State serverMode() {
        if (getConfirmation("new server session", "are you sure to abort current server session, and start a new session?")) {
            cleanSession();
            return super.serverMode();
        }
        else
            return this;

    }

    @Override
    public State localMode() {
        if (getConfirmation("exit server mode", "are you sure to abort current server session?")) {
            cleanSession();
            return super.localMode();
        }
        else
            return this;
    }

    @Override
    public State clientMode() {
        if (getConfirmation("exit server mode", "are you sure to abort current server session?")) {
            cleanSession();
            return super.clientMode();
        }
        else
            return this;
    }


}

class Client extends State {

    InetAddress serverAddr;
    private Session session;

    public Client(State state) {
        super(state);
        connected = false;
        model.setMode(Model.CLIENT);
        model.setConnected(false);
        String addr = JOptionPane.showInputDialog("Enter server host name or IP address: ", "localhost");
        try {
                serverAddr = InetAddress.getByName(addr);
                getConnection();
        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(null, "Unknown server");
        }

    }

    @Override
    public State edit(DocChange c) {
        super.edit(c);
        try {
            if (connected) {
                session.getOutput().writeObject(c);
                session.getOutput().flush();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "connection closed by server");
            model.setConnected(false);
            connected = false;
        }
        return this;
    }


    public void getConnection() {
        try {
            Socket socket = new Socket(serverAddr, serverPort);
            session = new Session(socket);
            Thread receiver = new Thread(new ReceiveChange(session, session.getInput()));
            session.setThread(receiver);
            connected = true;
            model.setConnected(true);
            session.start();

        } catch (IOException e) {
//            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Cannot connect to server");
        }
//        JOptionPane.showMessageDialog(null, "connected to server");

    }



    class ReceiveChange implements Runnable {
        Session session;
        ObjectInputStream in;
        public ReceiveChange(Session session, ObjectInputStream in) {
            this.session = session;
            this.in = in;
        }

        @Override
        public void run() {
            while (connected) {
                if (session.isClosed()) {
                    JOptionPane.showMessageDialog(null, "Cannot connect to server, session is down");
                    break;
                }
                try {
                    Object o = in.readObject();
                    if (o instanceof DocChange) {
                        model.changeDoc((DocChange) o);
                    }
                    else if (o instanceof MyDoc){
                        model.setDoc((MyDoc) o);
                    }
                    else {
                        throw new IOException("Cannot recognized object type");
                    }
                } catch (IOException | ClassNotFoundException e) {
                    JOptionPane.showMessageDialog(null, "session closed by server");
                    cleanSession();
                }
            }
        }
    }
    private void cleanSession() {
        if (session != null)
            session.close();
        connected = false;
        model.setConnected(false);
    }

    @Override
    public State create() {
        cleanSession();
        return super.create();
    }

    @Override
    public State open() {
        cleanSession();
        return super.open();
    }
    @Override
    public State serverMode() {
        if (getConfirmation("exit client mode", "are you sure to abort current client session?")) {
            cleanSession();
            return super.serverMode();
        }
        else
            return this;

    }

    @Override
    public State localMode() {
        if (getConfirmation("exit client mode", "are you sure to abort current client session?")) {
            cleanSession();
            return super.localMode();
        }
        else
            return this;
    }

    @Override
    public State clientMode() {
        if (getConfirmation("new client session", "are you sure to abort current client session, and start a new session?")) {
            cleanSession();
            return super.clientMode();
        }
        else
            return this;
    }

}

class Session {
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private Thread thread;


    public Session(Socket socket) {
        try {
            this.socket = socket;
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        thread.start();
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public void close() {
        try {
            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectOutputStream getOutput() {
        return output;
    }

    public ObjectInputStream getInput() {
        return input;
    }

    public Thread getThread() {
        return thread;
    }
}
