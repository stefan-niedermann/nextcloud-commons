package it.niedermann.android.markdown.controller;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

public interface MarkdownController extends EditorStateListener {
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    void setCommandReceiver(@Nullable CommandReceiver commandReceiver);
}
