package it.niedermann.android.markdown.markwon.textwatcher;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import it.niedermann.android.markdown.MarkdownUtil;
import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor;
import it.niedermann.android.markdown.model.SearchSpan;

public class SearchHighlightTextWatcher extends InterceptorTextWatcher {

    private final MarkwonMarkdownEditor editText;
    @Nullable
    private CharSequence searchText;
    @Nullable
    private Integer current;
    @ColorInt
    private int color;

    public SearchHighlightTextWatcher(@NonNull TextWatcher originalWatcher, @NonNull MarkwonMarkdownEditor editText) {
        super(originalWatcher);
        this.editText = editText;
        final var context = editText.getContext();
        final var typedValue = new TypedValue();
        final var theme = context.getTheme();
        theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);
        this.color = typedValue.data;
    }

    public void setSearchText(@Nullable CharSequence searchText, @Nullable Integer current) {
        this.current = current;
        if (TextUtils.isEmpty(searchText)) {
            this.searchText = null;
            final var text = editText.getText();
            if (text != null) {
                MarkdownUtil.removeSpans(text, SearchSpan.class);
            }
        } else {
            this.searchText = searchText;
            afterTextChanged(editText.getText());
        }
    }

    public void setSearchColor(@ColorInt int color) {
        this.color = color;
        afterTextChanged(editText.getText());
    }

    @Override
    public void afterTextChanged(Editable s) {
        originalWatcher.afterTextChanged(s);
        if (searchText != null) {
            MarkdownUtil.removeSpans(s, SearchSpan.class);
            MarkdownUtil.searchAndColor(editText.getContext(), s, searchText, color, current);
        }
    }
}
