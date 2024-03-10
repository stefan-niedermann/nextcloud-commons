package it.niedermann.android.markdown.controller;

import static java.util.function.Predicate.not;

import android.content.Context;
import android.text.Spannable;

import androidx.annotation.AnyThread;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class EditorStateNotifier {

    @NonNull
    private final Collection<? extends EditorStateListener> listeners;
    @NonNull
    private final ExecutorService firstNotifyExecutorService;
    @NonNull
    private final Collection<ExecutorService> executors = new HashSet<>(2);
    @NonNull
    private final Supplier<ExecutorService> executorFactory;
    @NonNull
    private final Function<Integer, ExecutorService> commandStateBuilderExecutorFactory;
    @NonNull
    private final EditorState.Factory editorStateFactory;
    @Nullable
    private EditorState lastNotifiedState;

    public EditorStateNotifier(@NonNull Collection<? extends EditorStateListener> listeners) {
        this(
                listeners,
                Executors::newSingleThreadExecutor,
                Executors::newFixedThreadPool,
                new ThreadPoolExecutor(Command.values().length + 1, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS, new SynchronousQueue<>()),
                new EditorState.Factory()
        );
    }

    @VisibleForTesting
    protected EditorStateNotifier(@NonNull Collection<? extends EditorStateListener> listeners,
                               @NonNull Supplier<ExecutorService> executorFactory,
                               @NonNull Function<Integer, ExecutorService> commandStateBuilderExecutorFactory,
                               @NonNull ExecutorService firstNotifyExecutorService,
                               @NonNull EditorState.Factory editorStateFactory
    ) {
        this.listeners = listeners;
        this.executorFactory = executorFactory;
        this.commandStateBuilderExecutorFactory = commandStateBuilderExecutorFactory;
        this.firstNotifyExecutorService = firstNotifyExecutorService;
        this.editorStateFactory = editorStateFactory;
    }

    @AnyThread
    public Future<Void> forceNotify(@NonNull Context context,
                                    @NonNull EditorStateListener listener,
                                    boolean editorIsEnabled,
                                    @ColorInt int color,
                                    @NonNull Spannable content,
                                    int selectionStart,
                                    int selectionEnd) {

        // FIXME This way a race condition with notify is possible
        return firstNotifyExecutorService.submit(() -> {
            final var state = editorStateFactory.build(context,
                    firstNotifyExecutorService,
                    editorIsEnabled,
                    color,
                    content,
                    selectionStart,
                    selectionEnd);

            listener.onEditorStateChanged(state);
            lastNotifiedState = state;

            return null;
        });
    }

    @AnyThread
    public Future<Void> notify(@NonNull Context context,
                               boolean editorIsEnabled,
                               @ColorInt int color,
                               @NonNull Spannable content,
                               int selectionStart,
                               int selectionEnd) {
        listeners.removeIf(Objects::isNull);
        if (listeners.isEmpty()) {
            return CompletableFuture.completedFuture(null);
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

        return executor.submit(() -> {
            try {
                final var state = editorStateFactory.build(context,
                        commandExecutor,
                        editorIsEnabled,
                        color,
                        content,
                        selectionStart,
                        selectionEnd);

                if (Objects.equals(state, lastNotifiedState)) {
                    return null;
                }

                listeners.forEach(listener -> {
                    if (!executor.isShutdown()) {
                        listener.onEditorStateChanged(state);
                        lastNotifiedState = state;
                    }
                });

            } finally {
                executor.shutdown();
                commandExecutor.shutdown();
            }

            return null;
        });
    }
}
