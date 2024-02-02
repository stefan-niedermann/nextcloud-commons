package it.niedermann.android.markdown.markwon.span;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.function.BiConsumer;

import io.noties.markwon.ext.tasklist.TaskListSpan;

public class ToggleTaskListSpan extends ClickableSpan {

    private static final String TAG = ToggleTaskListSpan.class.getSimpleName();

    private final BiConsumer<Integer, Boolean> toggleListener;
    private final TaskListSpan span;
    private final int position;

    public ToggleTaskListSpan(@NonNull BiConsumer<Integer, Boolean> toggleListener, @NonNull TaskListSpan span, int position) {
        this.toggleListener = toggleListener;
        this.span = span;
        this.position = position;
    }

    @Override
    public void onClick(@NonNull View widget) {
        if (widget.isEnabled()) {
            widget.invalidate();
            span.setDone(!span.isDone());
            widget.invalidate();
            toggleListener.accept(position, span.isDone());
        } else {
            Log.w(TAG, "Prevented toggling checkbox because the view is disabled");
        }
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        // NoOp to remove underline text decoration
    }
}