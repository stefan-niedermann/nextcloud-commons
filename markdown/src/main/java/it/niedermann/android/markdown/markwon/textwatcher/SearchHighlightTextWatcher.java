package it.niedermann.android.markdown.markwon.textwatcher;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.nextcloud.android.common.ui.theme.MaterialSchemes;
import com.nextcloud.android.common.ui.util.PlatformThemeUtil;

import it.niedermann.android.markdown.MarkdownUtil;
import it.niedermann.android.markdown.R;
import it.niedermann.android.markdown.SearchThemeUtils;
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
    @ColorInt
    private final int highlightColor;
    private final boolean darkTheme;
    @Nullable
    private SearchThemeUtils util;

    public SearchHighlightTextWatcher(@NonNull TextWatcher originalWatcher, @NonNull MarkwonMarkdownEditor editText) {
        super(originalWatcher);
        this.editText = editText;
        final var context = editText.getContext();
        this.color = ContextCompat.getColor(context, R.color.search_color);
        this.highlightColor = ContextCompat.getColor(context, R.color.bg_highlighted);
        this.darkTheme = PlatformThemeUtil.isDarkMode(context);
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
        this.util = new SearchThemeUtils(MaterialSchemes.Companion.fromColor(color));
        afterTextChanged(editText.getText());
    }

    @Override
    public void afterTextChanged(Editable s) {
        originalWatcher.afterTextChanged(s);
        if (searchText != null) {
            MarkdownUtil.removeSpans(s, SearchSpan.class);
//            MarkdownUtil.searchAndColor(s, searchText, current, color, highlightColor, darkTheme);
            if (util != null) {
//                util.highlightText(editText, editText.getText().toString(), searchText.toString());
            }
        }
    }
}
