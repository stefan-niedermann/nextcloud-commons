package it.niedermann.android.markdown.markwon.plugins.mentions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.RenderProps;
import io.noties.markwon.SpanFactory;

class DisplayNameSpanFactory implements SpanFactory {
    @Nullable
    @Override
    public Object getSpans(@NonNull MarkwonConfiguration configuration, @NonNull RenderProps props) {
        return new PotentialDisplayNameSpan(MentionProps.MENTION_DISPLAY_NAME_USER_ID_PROPS.require(props));
    }
}