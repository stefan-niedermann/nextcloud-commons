package it.niedermann.android.markdown.controller;

import androidx.annotation.NonNull;

public interface CommandReceiver {
    void executeCommand(@NonNull Command command) throws UnsupportedOperationException;
}