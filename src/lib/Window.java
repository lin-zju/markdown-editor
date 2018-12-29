package lib;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class Window extends JFrame {
    private Model model = null;
    private Controller controller = null;

    public Window(Model model, EditorView eview, OutlineView oview, Controller c) {
        // layout
        setLayout(new BorderLayout());

        // setup
        setModel(model);
        setController(c);
        setEditorView(eview);
        setOutLineView(oview);

        // add menu buttons
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // file menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        fileMenu.add(saveItem);
        fileMenu.add(openItem);
        fileMenu.add(newItem);
        saveItem.addActionListener(controller.new SaveFileListener());
        openItem.addActionListener(controller.new OpenFileListener());
        newItem.addActionListener(controller.new CreateFileListener());

        // mode
        JMenu modeMenu = new JMenu("Mode");
        menuBar.add(modeMenu);
        JMenuItem localItem = new JMenuItem("Local");
        JMenuItem clientItem = new JMenuItem("Client");
        JMenuItem serverItem = new JMenuItem("Server");
        modeMenu.add(localItem);
        modeMenu.add(clientItem);
        modeMenu.add(serverItem);
        localItem.addActionListener(controller.new LocalModeListener());
        clientItem.addActionListener(controller.new ClientModeListener());
        serverItem.addActionListener(controller.new ServerModeListener());

        // display the window
        setTitle("untitled.md*");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void setModel(Model model) {
        this.model = model;
        model.addActionListener(
            e -> {
                this.setTitle(model.getTitle() + (model.isModified() ? "*" : ""));
                this.repaint();
            }
        );
    }

    public void setEditorView(EditorView eview) {
        this.add(eview, BorderLayout.CENTER);
        DocumentListener dl = controller.new EditFileListener();
        eview.getDocument().addDocumentListener(dl);
        model.initDoc(eview.getDocument());
        model.setDocumentListener(dl);
    }

    public void setController(Controller c) {
        this.controller = c;
    }

    public void setOutLineView(OutlineView o) {
    }

    public static void main(String[] args) {
        Model model = new Model();
        EditorView eview = new EditorView(model);
        Controller c = new Controller(model);
        Window window = new Window(model, eview, null, c);
    }
}
