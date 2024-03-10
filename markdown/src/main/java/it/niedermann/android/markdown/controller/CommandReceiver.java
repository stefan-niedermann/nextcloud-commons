package it.niedermann.android.markdown.controller;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import java.util.concurrent.Future;

public interface CommandReceiver {
    void executeCommand(@NonNull Command command) throws UnsupportedOperationException;

    /**
     * @param controller that should be notified about {@link EditorState} changes and may {@link #executeCommand(Command)}s.
     * @return future returned when the {@param controller} has been registered and successfully notified.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    Future<Void> registerController(@NonNull MarkdownController controller);

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    void unregisterController(@NonNull MarkdownController controller);
}