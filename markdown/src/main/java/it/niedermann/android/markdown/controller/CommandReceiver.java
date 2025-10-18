package it.niedermann.android.markdown.controller;

import androidx.annotation.NonNull;

import java.util.concurrent.Future;

public interface CommandReceiver {

    void executeCommand(@NonNull Command command) throws UnsupportedOperationException;

    /**
     * @param controller that should be notified about {@link EditorState} changes and may {@link #executeCommand(Command)}s.
     * @return future returned when the {@param controller} has been registered and successfully notified.
     */
    @NonNull
    Future<Void> registerController(@NonNull MarkdownController controller);

    /**
     * @implSpec will be called implicitly when the {@link CommandReceiver} gets detached.
     * @param controller to be unregistered
     */
    void unregisterController(@NonNull MarkdownController controller);
}