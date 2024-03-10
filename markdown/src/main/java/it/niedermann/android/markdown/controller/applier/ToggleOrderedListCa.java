package it.niedermann.android.markdown.controller.applier;

import static it.niedermann.android.markdown.MarkdownUtil.getOrderedListNumber;

import android.content.Context;
import android.text.Editable;

import androidx.annotation.NonNull;

import it.niedermann.android.markdown.MarkdownUtil;

public class ToggleOrderedListCa implements CommandApplier {

    @Override
    public @NonNull CommandApplierResult applyCommand(@NonNull Context context,
                                                      @NonNull Editable content,
                                                      int selectionStart,
                                                      int selectionEnd) {
        final var startOfLine = MarkdownUtil.getStartOfLine(content, selectionStart);
        final var endOfLine = MarkdownUtil.getEndOfLine(content, selectionEnd);
        final var orderedListNumber = getOrderedListNumber(content.subSequence(startOfLine, endOfLine).toString());

        final int newSelection;

        if (orderedListNumber.isPresent()) {
            final var lengthOfStringToRemove = String.valueOf(orderedListNumber.get()).length() + ". ".length();
            content.replace(startOfLine, lengthOfStringToRemove, "");
            newSelection = selectionEnd - lengthOfStringToRemove;
        } else {
            int startingNumber = 1;

            if (startOfLine > 0) {
                final var startOfPreviousLine = MarkdownUtil.getStartOfLine(content, startOfLine - 1);
                startingNumber = MarkdownUtil.getOrderedListNumber(content.subSequence(startOfPreviousLine, startOfLine).toString()).orElse(1);
            }

            final var prefix = startingNumber + ". ";
            content.insert(startOfLine, prefix);
            newSelection = selectionStart + prefix.length();
        }

        return new CommandApplierResult(content, newSelection);
    }
}
