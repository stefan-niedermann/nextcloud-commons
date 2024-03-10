package it.niedermann.android.markdown.controller.applier;

import android.content.Context;
import android.text.Editable;

import androidx.annotation.NonNull;

import it.niedermann.android.markdown.MarkdownUtil;
import it.niedermann.android.markdown.model.EListType;

public class ToggleBlockQuoteCa implements CommandApplier {

    @Override
    public @NonNull CommandApplierResult applyCommand(@NonNull Context context,
                                                      @NonNull Editable content,
                                                      int selectionStart,
                                                      int selectionEnd) {
        final var startOfLine = MarkdownUtil.getStartOfLine(content, selectionStart);
        final var endOfLine = MarkdownUtil.getEndOfLine(content, selectionEnd);
        final var optionalListType = MarkdownUtil.lineStartsWithList(content.subSequence(startOfLine, endOfLine).toString());

        final int newSelection;

        if (optionalListType.isPresent()) {
            content.replace(startOfLine, optionalListType.get().listSymbolWithTrailingSpace.length(), "");
            newSelection = selectionStart + optionalListType.get().listSymbolWithTrailingSpace.length();
        } else {
            final EListType listType;
            if (startOfLine == 0) {
                listType = EListType.DASH;
            } else {
                final var startOfPreviousLine = MarkdownUtil.getStartOfLine(content, startOfLine - 1);
                listType = MarkdownUtil.lineStartsWithList(content.subSequence(startOfPreviousLine, startOfLine).toString()).orElse(EListType.DASH);
            }

            content.insert(startOfLine, listType.listSymbolWithTrailingSpace);
            newSelection = selectionStart + listType.listSymbolWithTrailingSpace.length();
        }

        return new CommandApplierResult(content, newSelection);
    }
}
