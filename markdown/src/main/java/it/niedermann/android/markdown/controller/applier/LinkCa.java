package it.niedermann.android.markdown.controller.applier;

import android.content.Context;
import android.text.Editable;

import androidx.annotation.NonNull;

import it.niedermann.android.markdown.MarkdownUtil;
import it.niedermann.android.util.ClipboardUtil;

public class LinkCa implements CommandApplier {

    @Override
    public @NonNull CommandApplierResult applyCommand(@NonNull Context context,
                                                      @NonNull Editable content,
                                                      int selectionStart,
                                                      int selectionEnd) {
        final int newSelection = MarkdownUtil.insertLink(content, selectionStart, selectionEnd, ClipboardUtil.getClipboardURLorNull(context));
        return new CommandApplierResult(content, newSelection);
    }
}
