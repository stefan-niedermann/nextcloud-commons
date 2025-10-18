package it.niedermann.android.markdown.controller;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor;

/**
 * A {@link Lifecycle} aware implementation to connect a {@link MarkdownController} with a {@link CommandReceiver}
 * @deprecated Use {@link CommandReceiver#registerController(MarkdownController)}
 */
@Deprecated(forRemoval = true)
public class ControllerConnector {

    @NonNull
    private final CommandReceiver commandReceiver;
    @NonNull
    private final MarkdownController controller;

    /**
     * Connects a {@link MarkdownController} to an {@link MarkwonMarkdownEditor}
     *
     * @param commandReceiver sending {@link EditorState} updates and receiving {@link Command}s
     * @param controller      sending {@link Command} and receiving {@link EditorState} updates
     */
    private ControllerConnector(@NonNull CommandReceiver commandReceiver,
                                @NonNull MarkdownController controller) {
        this.commandReceiver = commandReceiver;
        this.controller = controller;
    }

    @NonNull
    public static ControllerConnector connect(@NonNull LifecycleOwner ignored,
                                              @NonNull CommandReceiver commandReceiver,
                                              @NonNull MarkdownController controller) {
        final var connector = new ControllerConnector(commandReceiver, controller);
        commandReceiver.registerController(controller);
        return connector;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void onResume(@NonNull LifecycleOwner owner) {
        commandReceiver.registerController(controller);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void onPause(@NonNull LifecycleOwner owner) {
        commandReceiver.unregisterController(controller);
    }
}
