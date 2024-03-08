package it.niedermann.android.markdown.controller;

import it.niedermann.android.markdown.MarkdownController;
import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor;

public class EditorControllerConnector {
    private MarkwonMarkdownEditor editor;
    private MarkdownController controller;

    /**
     * FIXME: ADD ME
     * @param editor
     * @param controller
     */
    public EditorControllerConnector(MarkwonMarkdownEditor editor, MarkdownController controller) {
        this(editor, controller, true);
    }
    public EditorControllerConnector(MarkwonMarkdownEditor editor, MarkdownController controller, boolean instantConnect) {
        if (editor == null || controller == null) {
            throw new IllegalArgumentException("editor and controller may not be null");
        }
        this.editor = editor;
        this.controller = controller;
        if (instantConnect) {
            connect();
        }
    }

    public void connect() {
        editor.registerController(controller);
        controller.setEditor(editor);
    }
    public void disconnect() {
        editor.unregisterController(controller);
        controller.setEditor(null);
    }
}
