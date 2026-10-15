package it.niedermann.android.markdown.controller;

import android.content.Context;
import android.text.Spannable;

import androidx.annotation.AnyThread;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

public class EditorStateNotifier {

    @NonNull
    private final Collection<? extends EditorStateListener> listeners;
    @NonNull
    private final ExecutorService firstNotifyExecutorService;
    /**
     * Single threaded, long living executor which serializes {@link EditorState} builds.
     */
    @NonNull
    private final ExecutorService notifyExecutor;
    /**
     * Long living executor which resolves the states of all {@link Command}s in parallel.
     */
    @NonNull
    private final ExecutorService commandExecutor;
    @NonNull
    private final EditorState.Factory editorStateFactory;
    /**
     * Holds only the most recent enqueued notify request. Requests which arrive while a build is
     * running get coalesced, so that only the newest one will be processed afterwards.
     */
    @NonNull
    private final AtomicReference<NotifyRequest> pendingRequest = new AtomicReference<>();
    @NonNull
    private final AtomicBoolean workerActive = new AtomicBoolean(false);
    @Nullable
    private volatile EditorState lastNotifiedState;

    public EditorStateNotifier(@NonNull Collection<? extends EditorStateListener> listeners) {
        this(
                listeners,
                Executors::newSingleThreadExecutor,
                Executors::newFixedThreadPool,
                new CommandExecutor(),
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
        // The factories get applied exactly once. Reusing the resulting executors avoids expensive
        // thread (pool) creation and destruction for each notify() call, which happens on every
        // single keystroke via onSelectionChanged
        // (see https://github.com/nextcloud/notes-android/issues/2489).
        this.notifyExecutor = executorFactory.get();
        this.commandExecutor = commandStateBuilderExecutorFactory.apply(Command.values().length);
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

        pendingRequest.set(new NotifyRequest(context, editorIsEnabled, color, content, selectionStart, selectionEnd));

        if (workerActive.compareAndSet(false, true)) {
            return notifyExecutor.submit(this::processPendingRequests);
        }

        // A worker is already active and will pick up the coalesced request.
        return CompletableFuture.completedFuture(null);
    }

    private Void processPendingRequests() {
        try {
            NotifyRequest request;
            while ((request = pendingRequest.getAndSet(null)) != null) {
                final EditorState state;
                try {
                    state = editorStateFactory.build(request.context(),
                            commandExecutor,
                            request.editorIsEnabled(),
                            request.color(),
                            request.content(),
                            request.selectionStart(),
                            request.selectionEnd());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }

                if (Objects.equals(state, lastNotifiedState)) {
                    continue;
                }

                for (final var listener : listeners) {
                    listener.onEditorStateChanged(state);
                    lastNotifiedState = state;
                }
            }
        } finally {
            workerActive.set(false);
            // Handles the race where a new request arrived after the last getAndSet(null) returned
            // null, but before workerActive has been reset.
            if (pendingRequest.get() != null && workerActive.compareAndSet(false, true)) {
                notifyExecutor.submit(this::processPendingRequests);
            }
        }
        return null;
    }

    private record NotifyRequest(@NonNull Context context,
                                 boolean editorIsEnabled,
                                 @ColorInt int color,
                                 @NonNull Spannable content,
                                 int selectionStart,
                                 int selectionEnd) {
    }

    /**
     * {@link ThreadPoolExecutor} optimized to resolve {@link Command} related states in parallel,
     * allowing all threads to time out while the editor is idle.
     */
    private static class CommandExecutor extends ThreadPoolExecutor {

        public CommandExecutor() {
            super(Command.values().length + 1, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS, new SynchronousQueue<>());
            allowCoreThreadTimeOut(true);
        }
    }
}
