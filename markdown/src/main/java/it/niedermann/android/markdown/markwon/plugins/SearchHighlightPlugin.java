package it.niedermann.android.markdown.markwon.plugins;

import static it.niedermann.android.markdown.MarkdownUtil.getContentAsSpannable;

import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.nextcloud.android.common.ui.theme.MaterialSchemes;
import com.nextcloud.android.common.ui.util.PlatformThemeUtil;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonPlugin;
import it.niedermann.android.markdown.MarkdownUtil;
import it.niedermann.android.markdown.R;
import it.niedermann.android.markdown.SearchThemeUtils;
import it.niedermann.android.markdown.model.SearchSpan;

public class SearchHighlightPlugin extends AbstractMarkwonPlugin {

    @Nullable
    private CharSequence searchText = null;
    private Integer current;
    @ColorInt
    private int color;
    @ColorInt
    private final int highlightColor;
    private final boolean darkTheme;
    @Nullable
    private SearchThemeUtils util;

    public SearchHighlightPlugin(@NonNull Context context) {
        this.color = ContextCompat.getColor(context, R.color.search_color);
        this.highlightColor = ContextCompat.getColor(context, R.color.bg_highlighted);
        this.darkTheme = PlatformThemeUtil.isDarkMode(context);
    }

    public static MarkwonPlugin create(@NonNull Context context) {
        return new SearchHighlightPlugin(context);
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

    public void setSearchColor(@ColorInt int color, @NonNull TextView textView) {
        this.color = color;
        this.util = new SearchThemeUtils(MaterialSchemes.Companion.fromColor(color));
    }

    @Override
    public void afterSetText(@NonNull TextView textView) {
        super.afterSetText(textView);
        if (this.searchText != null) {
            final var spannable = getContentAsSpannable(textView);
            MarkdownUtil.searchAndColor(textView.getContext(), spannable, searchText, current, color);
            if (util != null) {
//                util.highlightText(textView, textView.getText().toString(), searchText.toString());
            }
        }
    }
}
