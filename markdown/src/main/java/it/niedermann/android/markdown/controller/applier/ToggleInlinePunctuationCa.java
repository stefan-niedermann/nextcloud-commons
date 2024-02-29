package it.niedermann.android.markdown.controller.applier;

import android.content.Context;
import android.text.Editable;

import androidx.annotation.NonNull;

import it.niedermann.android.markdown.MarkdownUtil;

public class ToggleInlinePunctuationCa implements CommandApplier {

    private final String punctuation;

    public ToggleInlinePunctuationCa(@NonNull String punctuation) {
        this.punctuation = punctuation;
    }

    @Override
    public @NonNull CommandApplierResult applyCommand(@NonNull Context context,
                                                      @NonNull Editable content,
                                                      int selectionStart,
                                                      int selectionEnd) {
        final int newSelection = MarkdownUtil.togglePunctuation(content, selectionStart, selectionEnd, punctuation);
        return new CommandApplierResult(content, newSelection);
    }
}
