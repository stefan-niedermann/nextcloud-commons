package it.niedermann.android.markdown.markwon.format;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.SparseIntArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import java.util.Map;

import it.niedermann.android.markdown.MarkdownController;
import it.niedermann.android.markdown.R;
import it.niedermann.android.markdown.controller.Command;
import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor;

@Deprecated(forRemoval = true)
public class ContextBasedRangeFormattingCallback extends AbstractFormattingCallback implements ActionMode.Callback, MarkdownController {

    public ContextBasedRangeFormattingCallback(@NonNull MarkwonMarkdownEditor ignored) {
        super(R.menu.context_based_range_formatting, Map.of(
                R.id.bold, Command.TOGGLE_BOLD,
                R.id.italic, Command.TOGGLE_ITALIC,
                R.id.link, Command.INSERT_LINK,
                R.id.checkbox, Command.TOGGLE_CHECKBOX_LIST
        ));
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        super.onCreateActionMode(mode, menu);

        final var styleFormatMap = new SparseIntArray();
        styleFormatMap.append(R.id.bold, Typeface.BOLD);
        styleFormatMap.append(R.id.italic, Typeface.ITALIC);

        MenuItem item;
        CharSequence title;
        SpannableString spannableString;

        for (int i = 0; i < styleFormatMap.size(); i++) {
            item = menu.findItem(styleFormatMap.keyAt(i));
            title = item.getTitle();
            spannableString = new SpannableString(title);
            spannableString.setSpan(new StyleSpan(styleFormatMap.valueAt(i)), 0, title == null ? 0 : title.length(), 0);
            item.setTitle(spannableString);
        }

        return true;
    }
}
