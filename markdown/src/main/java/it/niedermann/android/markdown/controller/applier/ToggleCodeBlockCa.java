package it.niedermann.android.markdown.controller.applier;

import android.content.Context;
import android.text.Editable;

import androidx.annotation.NonNull;

import it.niedermann.android.markdown.MarkdownUtil;

public class ToggleCodeBlockCa implements CommandApplier {

    @Override
    public @NonNull CommandApplierResult applyCommand(@NonNull Context context,
                                                      @NonNull Editable content,
                                                      int selectionStart,
                                                      int selectionEnd) {
        final var startOfLine = MarkdownUtil.getStartOfLine(content, selectionStart);
        final var endOfLine = MarkdownUtil.getEndOfLine(content, selectionEnd);

        return new CommandApplierResult(content, startOfLine);
    }
}
