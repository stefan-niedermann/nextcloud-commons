package it.niedermann.android.markdown.controller;

import static java.util.function.Predicate.not;

import android.content.Context;
import android.text.Spannable;

import androidx.annotation.AnyThread;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

public class EditorStateNotifier {

    @NonNull
    private final Collection<? extends EditorStateListener> listeners;
    @NonNull
    private final Collection<ExecutorService> executors = new HashSet<>(2);
    @NonNull
    private final Supplier<ExecutorService> executorFactory;
    @NonNull
    private final Function<Integer, ExecutorService> commandStateBuilderExecutorFactory;
    @Nullable
    private EditorState lastNotifiedState;

    public EditorStateNotifier(@NonNull Collection<? extends EditorStateListener> listeners) {
        this(listeners, Executors::newSingleThreadExecutor, Executors::newFixedThreadPool);
    }

    private EditorStateNotifier(@NonNull Collection<? extends EditorStateListener> listeners,
                                @NonNull Supplier<ExecutorService> executorFactory,
                                @NonNull Function<Integer, ExecutorService> commandStateBuilderExecutorFactory
    ) {
        this.listeners = listeners;
        this.executorFactory = executorFactory;
        this.commandStateBuilderExecutorFactory = commandStateBuilderExecutorFactory;
    }

    @AnyThread
    public void notify(@NonNull Context context,
                       boolean editorIsEnabled,
                       @ColorInt int color,
                       @NonNull Spannable content,
                       int selectionStart,
                       int selectionEnd) {
        listeners.removeIf(Objects::isNull);
        if (listeners.isEmpty()) {
            return;
        }

        final var executor = executorFactory.get();
        final var commandExecutor = commandStateBuilderExecutorFactory.apply(Command.values().length);

        synchronized (executors) {
            executors.stream()
                    .filter(not(ExecutorService::isShutdown))
                    .forEach(ExecutorService::shutdownNow);
            executors.removeIf(ExecutorService::isShutdown);
            executors.addAll(List.of(executor, commandExecutor));
        }

        executor.submit(() -> {
            try {
                final var state = build(context,
                        commandExecutor,
                        editorIsEnabled,
                        color,
                        content,
                        selectionStart,
                        selectionEnd);
                if (Objects.equals(state, lastNotifiedState)) {
                    return;
                }

                listeners.forEach(listener -> {
                    if (!executor.isShutdown()) {
                        listener.onEditorStateChanged(state);
                        lastNotifiedState = state;
                    }
                });
            } catch (InterruptedException ignored) {
            } finally {
                executor.shutdown();
                commandExecutor.shutdown();
            }
        });
    }

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
