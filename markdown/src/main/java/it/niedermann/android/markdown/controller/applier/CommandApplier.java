package it.niedermann.android.markdown.controller.applier;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;

import androidx.annotation.NonNull;

public interface CommandApplier {
    @NonNull
    CommandApplierResult applyCommand(@NonNull Context context,
                                      @NonNull Editable content,
                                      int selectionStart,
                                      int selectionEnd);

    record CommandApplierResult(@NonNull Spannable content, int selection) {
    }
}
