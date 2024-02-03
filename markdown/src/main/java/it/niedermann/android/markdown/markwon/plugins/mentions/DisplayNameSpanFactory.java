package it.niedermann.android.markdown.markwon.plugins.mentions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Optional;

import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.RenderProps;
import io.noties.markwon.SpanFactory;

class DisplayNameSpanFactory implements SpanFactory {
    @Nullable
    @Override
    public Object getSpans(@NonNull MarkwonConfiguration configuration, @NonNull RenderProps props) {
        final var userId = MentionProps.MENTION_USER_ID_PROPS.require(props);
        final var displayName = Optional.ofNullable(MentionProps.MENTION_DISPLAY_NAME_PROPS.get(props));

        if (displayName.isPresent()) {
            return null;
        } else {
            return new PotentialDisplayNameSpan(userId);
        }
    }

    public record PotentialDisplayNameSpan(@NonNull String userId) {
    }
}