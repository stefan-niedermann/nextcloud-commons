package it.niedermann.android.markdown.markwon.handler;

import android.text.Editable;
import android.text.Spanned;

import androidx.annotation.NonNull;

import io.noties.markwon.Markwon;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.core.spans.HeadingSpan;
import io.noties.markwon.editor.EditHandler;
import io.noties.markwon.editor.PersistedSpans;

public class HeadingEditHandler implements EditHandler<HeadingSpan> {

    private MarkwonTheme theme;

    @Override
    public void init(@NonNull Markwon markwon) {
        this.theme = markwon.configuration().theme();
    }

    @Override
    public void configurePersistedSpans(@NonNull PersistedSpans.Builder builder) {
        builder.persistSpan(Heading1Span.class, () -> new Heading1Span(theme));
        builder.persistSpan(Heading2Span.class, () -> new Heading2Span(theme));
        builder.persistSpan(Heading3Span.class, () -> new Heading3Span(theme));
        builder.persistSpan(Heading4Span.class, () -> new Heading4Span(theme));
        builder.persistSpan(Heading5Span.class, () -> new Heading5Span(theme));
        builder.persistSpan(Heading6Span.class, () -> new Heading6Span(theme));
    }

    @Override
    public void handleMarkdownSpan(
            @NonNull PersistedSpans persistedSpans,
            @NonNull Editable editable,
            @NonNull String input,
            @NonNull HeadingSpan span,
            int spanStart,
            int spanTextLength) {
        final HeadingSpan newSpan;

        switch (span.getLevel()) {
            case 1 -> newSpan = persistedSpans.get(Heading1Span.class);
            case 2 -> newSpan = persistedSpans.get(Heading2Span.class);
            case 3 -> newSpan = persistedSpans.get(Heading3Span.class);
            case 4 -> newSpan = persistedSpans.get(Heading4Span.class);
            case 5 -> newSpan = persistedSpans.get(Heading5Span.class);
            case 6 -> newSpan = persistedSpans.get(Heading6Span.class);
            default -> {
                return;
            }
        }
        final int newStart = getNewSpanStart(input, spanStart);
        final int newEnd = findEnd(input, newStart, newSpan.getLevel());

        editable.setSpan(
                newSpan,
                newStart,
                newEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }

    private int findEnd(String input, int searchFrom, int spanLevel) {
        int end = searchFrom + spanLevel;
        final int strLength = input.length();
        while (end < strLength - 1) {
            end++;
            if (input.charAt(end) == '\n') {
                break;
            }
        }
        return end + 1;
    }

    private int getNewSpanStart(String input, int spanStart) {
        int start = spanStart;

        while (start >= 0 && input.charAt(start) != '\n') {
            start--;
        }
        start += 1;

        return start;
    }


    @NonNull
    @Override
    public Class<HeadingSpan> markdownSpanType() {
        return HeadingSpan.class;
    }

    private static class Heading1Span extends HeadingSpan {
        public Heading1Span(@NonNull MarkwonTheme theme) {
            super(theme, 1);
        }
    }

    private static class Heading2Span extends HeadingSpan {
        public Heading2Span(@NonNull MarkwonTheme theme) {
            super(theme, 2);
        }
    }

    private static class Heading3Span extends HeadingSpan {
        public Heading3Span(@NonNull MarkwonTheme theme) {
            super(theme, 3);
        }
    }

    private static class Heading4Span extends HeadingSpan {
        public Heading4Span(@NonNull MarkwonTheme theme) {
            super(theme, 4);
        }
    }

    private static class Heading5Span extends HeadingSpan {
        public Heading5Span(@NonNull MarkwonTheme theme) {
            super(theme, 5);
        }
    }

    private static class Heading6Span extends HeadingSpan {
        public Heading6Span(@NonNull MarkwonTheme theme) {
            super(theme, 6);
        }
    }
}