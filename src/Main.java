import lib.*;

public class Main {
    public static void main(String[] args) {
        Model model = new Model();
        Controller c = new Controller(model);
        EditorView eview = new EditorView(model);
        OutlineView oview = new OutlineView(model);
        RenderView rview = new RenderView(model);
        Window window = new Window(model, eview, oview, rview, c);
    }
}
