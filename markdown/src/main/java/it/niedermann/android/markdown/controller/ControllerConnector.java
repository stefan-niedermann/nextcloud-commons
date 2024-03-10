package it.niedermann.android.markdown.controller;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor;

/**
 * A {@link Lifecycle} aware implementation to connect a {@link MarkdownController} with a {@link CommandReceiver}
 */
public class ControllerConnector implements DefaultLifecycleObserver {

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
    public static ControllerConnector connect(@NonNull LifecycleOwner lifecycleOwner,
                                              @NonNull CommandReceiver commandReceiver,
                                              @NonNull MarkdownController controller) {
        final var connector = new ControllerConnector(commandReceiver, controller);
        lifecycleOwner.getLifecycle().addObserver(connector);
        return connector;
    }

    @Override
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void onResume(@NonNull LifecycleOwner owner) {
        commandReceiver.registerController(controller);
        controller.setCommandReceiver(commandReceiver);
    }

    @Override
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void onPause(@NonNull LifecycleOwner owner) {
        commandReceiver.unregisterController(controller);
        controller.setCommandReceiver(null);
    }
}
