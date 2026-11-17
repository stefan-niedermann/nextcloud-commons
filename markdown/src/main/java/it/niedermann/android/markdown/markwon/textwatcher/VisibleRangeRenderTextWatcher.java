package it.niedermann.android.markdown.markwon.textwatcher;

import android.graphics.Rect;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.noties.markwon.editor.MarkwonEditor;
import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor;

/**
 * Renders Markdown in the background like {@link io.noties.markwon.editor.MarkwonEditorTextWatcher#withPreRender},
 * but applies spans only down to one screen below the visible area. Android re-lays out text for every span that
 * changes or moves, so applying all spans of a long note makes typing lag
 * (see https://github.com/nextcloud/notes-android/issues/2489). More spans get applied when scrolling down.
 */
public class VisibleRangeRenderTextWatcher implements TextWatcher {

    private static final String TAG = VisibleRangeRenderTextWatcher.class.getSimpleName();

    private final MarkwonEditor editor;
    private final MarkwonMarkdownEditor editText;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ViewTreeObserver.OnScrollChangedListener scrollListener = this::onScrollChanged;
    private final Rect visibleRect = new Rect();
    private int generation = 0;
    private int appliedUntil = 0;
    @Nullable
    private Future<?> future;
    @Nullable
    private MarkwonEditor.PreRenderResult lastResult;
    private boolean selfChange = false;

    public VisibleRangeRenderTextWatcher(@NonNull MarkwonEditor editor, @NonNull MarkwonMarkdownEditor editText) {
        this.editor = editor;
        this.editText = editText;
        editText.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                v.getViewTreeObserver().addOnScrollChangedListener(scrollListener);
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
                v.getViewTreeObserver().removeOnScrollChangedListener(scrollListener);
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Nothing to do
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Nothing to do
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (selfChange) {
            return;
        }
        final int key = ++generation;
        lastResult = null;
        if (future != null) {
            future.cancel(true);
        }
        final var copy = new SpannableStringBuilder(s);
        future = executor.submit(() -> {
            try {
                editor.preRender(copy, result -> editText.post(() -> {
                    if (key == generation) {
                        lastResult = result;
                        dispatch(result);
                    }
                }));
            } catch (Throwable t) {
                Log.e(TAG, "Could not render Markdown", t);
            }
        });
    }

    private void onScrollChanged() {
        if (lastResult != null && visibleEnd() > appliedUntil) {
            editText.keepScrollPositionUntilEdit();
            dispatch(lastResult);
        }
    }

    private void dispatch(@NonNull MarkwonEditor.PreRenderResult result) {
        final var text = editText.getText();
        if (text == null) {
            return;
        }
        appliedUntil = visibleEnd();
        selfChange = true;
        try {
            result.dispatchTo(filtered(text, appliedUntil));
        } finally {
            selfChange = false;
        }
    }

    /**
     * @return text offset one screen below the visible area, or the text length if the editor is not laid out yet
     */
    private int visibleEnd() {
        final var layout = editText.getLayout();
        if (layout == null || !editText.getLocalVisibleRect(visibleRect)) {
            return editText.length();
        }
        final int y = visibleRect.bottom + visibleRect.height() - editText.getTotalPaddingTop();
        return layout.getLineEnd(layout.getLineForVertical(y));
    }

    /**
     * @return {@link Editable} which passes span changes to {@param text}, but skips spans that did not change
     * and removes spans which start after {@param limit}
     */
    @VisibleForTesting
    static Editable filtered(@NonNull Editable text, int limit) {
        return (Editable) Proxy.newProxyInstance(Editable.class.getClassLoader(), new Class[]{Editable.class}, (proxy, method, args) -> {
            if ("setSpan".equals(method.getName())) {
                final Object span = args[0];
                final int start = (int) args[1];
                if (start > limit) {
                    text.removeSpan(span);
                    return null;
                }
                if (text.getSpanStart(span) == start && text.getSpanEnd(span) == (int) args[2] && text.getSpanFlags(span) == (int) args[3]) {
                    return null;
                }
            }
            try {
                return method.invoke(text, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
    }
}
