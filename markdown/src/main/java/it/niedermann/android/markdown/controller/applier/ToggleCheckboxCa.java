package it.niedermann.android.markdown.controller.applier;

import android.content.Context;
import android.text.Editable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import it.niedermann.android.markdown.MarkdownUtil;
import it.niedermann.android.markdown.model.EListType;

public class ToggleCheckboxCa implements CommandApplier {

    @Override
    public @NonNull CommandApplierResult applyCommand(@NonNull Context context,
                                                      @NonNull Editable content,
                                                      int selectionStart,
                                                      int selectionEnd) {
        final var startOfLine = MarkdownUtil.getStartOfLine(content, selectionStart);
        final var endOfLine = MarkdownUtil.getEndOfLine(content, selectionEnd);

        @Nullable
        EListType listType = null;
        for (final var type : EListType.values()) {
            if (MarkdownUtil.lineStartsWithCheckbox(content.subSequence(startOfLine, endOfLine).toString(), type)) {
                listType = type;
                break;
            }
        }

        final int newSelection;

        if (listType == null) {
            @Nullable
            EListType previousLineListType = EListType.DASH;
            if (startOfLine > 0) {
                final var startOfPreviousLine = MarkdownUtil.getStartOfLine(content, startOfLine - 1);
                for (final var type : EListType.values()) {
                    if (MarkdownUtil.lineStartsWithCheckbox(content.subSequence(startOfPreviousLine, startOfLine).toString(), type)) {
                        previousLineListType = type;
                        break;
                    }
                }
            }

            content.insert(MarkdownUtil.getStartOfLine(content, selectionStart), previousLineListType.checkboxUncheckedWithTrailingSpace);
            newSelection = selectionEnd + previousLineListType.checkboxUncheckedWithTrailingSpace.length();
        } else {
            if (startOfLine + listType.checkboxUncheckedWithTrailingSpace.length() > endOfLine) {
                content.replace(startOfLine, listType.checkboxUnchecked.length(), "");
                newSelection = selectionEnd - listType.checkboxUnchecked.length();
            } else {
                content.replace(startOfLine, listType.checkboxUncheckedWithTrailingSpace.length(), "");
                newSelection = selectionEnd - listType.checkboxUncheckedWithTrailingSpace.length();
            }
        }

        return new CommandApplierResult(content, newSelection);
    }
}
