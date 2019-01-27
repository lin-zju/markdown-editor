package lib;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class Window extends JFrame {
    public final static int WIDTH = 1000;
    public final static int HEIGHT = 800;
    private Model model = null;
    private Controller controller = null;

    private JLabel statusLabel;

    public Window(Model model, EditorView eview, OutlineView oview, RenderView rview, Controller c) {
        // layout
        setLayout(new BorderLayout());

        // setup
        setModel(model);
        setController(c);
        setEditorView(eview);

        // editor view and render view
        JSplitPane editorPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JScrollPane leftScrollPane = new JScrollPane(eview);
        JScrollPane rightScrollPane = new JScrollPane(rview);
        editorPane.setLeftComponent(leftScrollPane);
        editorPane.setRightComponent(rightScrollPane);
        editorPane.setOneTouchExpandable(true);
        editorPane.setDividerLocation((WIDTH - 150) / 2);
        editorPane.setContinuousLayout(true);
        //Provide minimum sizes for the two components in the split pane
        Dimension minimumSize = new Dimension(100, 0);
        leftScrollPane.setMinimumSize(minimumSize);
        rightScrollPane.setMinimumSize(minimumSize);

        // plus outline view
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JScrollPane outlinePane = new JScrollPane(oview);
        splitPane.setLeftComponent(outlinePane);
        splitPane.setRightComponent(editorPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(150);
        splitPane.setContinuousLayout(true);
        //Provide minimum sizes for the two components in the split pane
        outlinePane.setMinimumSize(minimumSize);
        editorPane.setMinimumSize(minimumSize);

        this.add(splitPane, BorderLayout.CENTER);



        // add menu buttons
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // file menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem exportItem = new JMenuItem("Export to HTML");
        fileMenu.add(saveItem);
        fileMenu.add(openItem);
        fileMenu.add(newItem);
        fileMenu.add(exportItem);
        saveItem.addActionListener(controller.new SaveFileListener());
        openItem.addActionListener(controller.new OpenFileListener());
        newItem.addActionListener(controller.new CreateFileListener());
        exportItem.addActionListener(controller.new ExportFileListener());

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

        // status bar
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        this.add(statusPanel, BorderLayout.SOUTH);
        statusPanel.setPreferredSize(new Dimension(this.getWidth(), 16));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        statusLabel = new JLabel("Local Mode");
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusPanel.add(statusLabel);

        // display the window
        setTitle("untitled.md*");
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void setModel(Model model) {
        this.model = model;
        model.addActionListener(
            e -> {
                this.setTitle(model.getTitle() + (model.isModified() ? "*" : ""));
                this.statusLabel.setText(model.getStatusText());
                this.repaint();
            }
        );
    }

    public void setEditorView(EditorView eview) {
//        this.add(eview, BorderLayout.CENTER);
        DocumentListener dl = controller.new EditFileListener();
        eview.getDocument().addDocumentListener(dl);
        model.initDoc(eview.getDocument());
        model.setDocumentListener(dl);
    }


    public void setController(Controller c) {
        this.controller = c;
    }


    public static void main(String[] args) {
//        Model model = new Model();
//        Controller c = new Controller(model);
//        EditorView eview = new EditorView(model);
//        OutlineView oview = new OutlineView(model);
//        RenderView rview = new RenderView(model);
//        Window window = new Window(model, eview, oview, rview, c);
    }
}
