package it.niedermann.android.markdown.controller.stateresolver;

import android.content.Context;
import android.text.Spannable;

import androidx.annotation.NonNull;

public interface CommandStateResolver {

    boolean isEnabled(@NonNull Context context,
                      @NonNull Spannable content,
                      int selectionStart,
                      int selectionEnd);

    boolean isActive(@NonNull Context context,
                     @NonNull Spannable content,
                     int selectionStart,
                     int selectionEnd);
}
