package it.niedermann.android.markdown.markwon.plugins;

import static it.niedermann.android.markdown.MarkdownUtil.getContentAsSpannable;

import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonPlugin;
import it.niedermann.android.markdown.MarkdownUtil;
import it.niedermann.android.markdown.model.SearchSpan;

public class SearchHighlightPlugin extends AbstractMarkwonPlugin {

    @Nullable
    private CharSequence searchText = null;
    @Nullable
    private Integer current = null;
    @ColorInt
    private int color;

    public SearchHighlightPlugin(@ColorInt int color) {
        this.color = color;
    }

    public static MarkwonPlugin create(@ColorInt int color) {
        return new SearchHighlightPlugin(color);
    }

    public void setSearchText(@Nullable CharSequence searchText, @Nullable Integer current, @NonNull TextView textView) {
        this.current = current;
        MarkdownUtil.removeSpans(getContentAsSpannable(textView), SearchSpan.class);
        if (TextUtils.isEmpty(searchText)) {
            this.searchText = null;
        } else {
            this.searchText = searchText;
            afterSetText(textView);
        }
    }

    public void setColor(@ColorInt int color) {
        this.color = color;
    }

    @Override
    public void afterSetText(@NonNull TextView textView) {
        super.afterSetText(textView);
        if (this.searchText != null) {
            final var spannable = getContentAsSpannable(textView);
            MarkdownUtil.searchAndColor(textView.getContext(), spannable, searchText, color, current);
        }
    }
}
