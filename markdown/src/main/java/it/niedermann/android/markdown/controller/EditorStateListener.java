package it.niedermann.android.markdown.controller;

import androidx.annotation.NonNull;

public interface EditorStateListener {

    void onEditorStateChanged(@NonNull EditorState state);
}
