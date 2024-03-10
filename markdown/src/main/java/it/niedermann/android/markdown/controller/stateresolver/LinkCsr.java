package it.niedermann.android.markdown.controller.stateresolver;

import static it.niedermann.android.markdown.MarkdownUtil.lineStartsWithCheckbox;

import android.content.Context;
import android.text.Spannable;

import androidx.annotation.NonNull;

import it.niedermann.android.markdown.MarkdownUtil;

public class LinkCsr implements CommandStateResolver {
    @Override
    public boolean isEnabled(@NonNull Context context,
                             @NonNull Spannable content,
                             int selectionStart,
                             int selectionEnd) {
        if (MarkdownUtil.isMultilineSelection(content, selectionStart, selectionEnd)) {
            return false;
        }

        if (lineStartsWithCheckbox(MarkdownUtil.getLine(content, selectionStart))) {
            return false;
        }

        return !MarkdownUtil.selectionIsInLink(content, selectionStart, selectionEnd);
    }

    @Override
    public boolean isActive(@NonNull Context context,
                            @NonNull Spannable content,
                            int selectionStart,
                            int selectionEnd) {
        return MarkdownUtil.selectionIsInLink(content, selectionStart, selectionEnd);
    }
}
