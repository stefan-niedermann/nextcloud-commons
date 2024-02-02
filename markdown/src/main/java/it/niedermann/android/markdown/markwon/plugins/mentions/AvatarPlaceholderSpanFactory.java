package it.niedermann.android.markdown.markwon.plugins.mentions;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.atomic.AtomicReference;

import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.RenderProps;
import io.noties.markwon.SpanFactory;

class AvatarPlaceholderSpanFactory implements SpanFactory {

    @NonNull
    private final AtomicReference<Drawable> avatarPlaceholder;

    public AvatarPlaceholderSpanFactory(@NonNull AtomicReference<Drawable> avatarPlaceholder) {
        this.avatarPlaceholder = avatarPlaceholder;
    }

    @Nullable
    @Override
    public Object getSpans(@NonNull MarkwonConfiguration configuration,
                           @NonNull RenderProps props) {
        return new AvatarPlaceholderSpan(avatarPlaceholder.get(),
                MentionProps.MENTION_AVATAR_USER_ID_PROPS.require(props),
                MentionProps.MENTION_AVATAR_URL_PROPS.require(props));
    }
}