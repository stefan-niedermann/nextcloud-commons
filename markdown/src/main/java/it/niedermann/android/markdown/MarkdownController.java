package it.niedermann.android.markdown;

import androidx.annotation.Nullable;

import it.niedermann.android.markdown.controller.EditorStateListener;
import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor;

public interface MarkdownController extends EditorStateListener {
    void setEditor(@Nullable MarkwonMarkdownEditor editor);
}
