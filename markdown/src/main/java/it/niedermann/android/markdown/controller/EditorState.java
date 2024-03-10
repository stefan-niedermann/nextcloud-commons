package it.niedermann.android.markdown.controller;

import android.content.Context;
import android.text.Spannable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

public record EditorState(
        @NonNull Map<Command, CommandState> commands,
        @ColorInt int color
) {

    public static class Factory {
        @WorkerThread
        @NonNull
        public EditorState build(@NonNull Context context,
                                 @NonNull ExecutorService executor,
                                 boolean editorIsEnabled,
                                 @ColorInt int color,
                                 @NonNull Spannable content,
                                 int selectionStart,
                                 int selectionEnd) throws InterruptedException {

            final int commandCount = Command.values().length;
            final var latch = new CountDownLatch(commandCount);
            final var commandStates = new HashMap<Command, CommandState>(commandCount);

            for (final var command : Command.values()) {
                executor.submit(() -> {
                    try {
                        final var state = new CommandState(
                                editorIsEnabled && command.isEnabled(context, content, selectionStart, selectionEnd),
                                command.isActive(context, content, selectionStart, selectionEnd)
                        );

                        commandStates.put(command, state);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            return new EditorState(commandStates, color);
        }
    }
}
