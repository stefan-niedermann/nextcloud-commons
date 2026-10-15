package it.niedermann.android.markdown.markwon.textwatcher;

import android.os.Handler;
import android.os.Looper;
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

    /**
     * While a search is active, each text change removes and re-applies <em>all</em>
     * {@link SearchSpan}s over the whole document. Debouncing this work keeps typing responsive in
     * long notes (see https://github.com/nextcloud/notes-android/issues/2489 and
     * https://github.com/nextcloud/notes-android/issues/1729).
     */
    private static final long REHIGHLIGHT_DEBOUNCE_MS = 250;

    private final MarkwonMarkdownEditor editText;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable rehighlightRunnable = this::rehighlight;
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
            handler.removeCallbacks(rehighlightRunnable);
            final var text = editText.getText();
            if (text != null) {
                MarkdownUtil.removeSpans(text, SearchSpan.class);
            }
        } else {
            this.searchText = searchText;
            // Apply immediately because this is a direct reaction to a user action. Only the
            // search highlighting is updated - the previous implementation additionally triggered
            // a full Markdown re-render by delegating to the original watcher although the text
            // did not change at all.
            rehighlight();
        }
    }

    public void setSearchColor(@ColorInt int color) {
        this.color = color;
        if (searchText != null) {
            rehighlight();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        originalWatcher.afterTextChanged(s);
        if (searchText != null) {
            handler.removeCallbacks(rehighlightRunnable);
            handler.postDelayed(rehighlightRunnable, REHIGHLIGHT_DEBOUNCE_MS);
        }
    }

    private void rehighlight() {
        handler.removeCallbacks(rehighlightRunnable);
        final var text = editText.getText();
        if (text == null) {
            return;
        }
        MarkdownUtil.removeSpans(text, SearchSpan.class);
        if (searchText != null) {
            MarkdownUtil.searchAndColor(editText.getContext(), text, searchText, color, current);
        }
    }
}
