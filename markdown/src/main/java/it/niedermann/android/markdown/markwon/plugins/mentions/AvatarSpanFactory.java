package it.niedermann.android.markdown.markwon.plugins.mentions;

import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.RenderProps;
import io.noties.markwon.SpanFactory;

class AvatarSpanFactory implements SpanFactory {

    @NonNull
    private final AtomicReference<Drawable> avatarPlaceholder;

    public AvatarSpanFactory(@NonNull AtomicReference<Drawable> avatarPlaceholder) {
        this.avatarPlaceholder = avatarPlaceholder;
    }

    @Nullable
    @Override
    public Object getSpans(@NonNull MarkwonConfiguration configuration,
                           @NonNull RenderProps props) {
        final var userId = MentionProps.MENTION_USER_ID_PROPS.require(props);
        final var userIsKnown = MentionProps.MENTION_USER_IS_KNOWN_PROPS.require(props);
        final var url = MentionProps.MENTION_AVATAR_URL_PROPS.require(props);
        final var avatar = Optional.ofNullable(MentionProps.MENTION_AVATAR_DRAWABLE_PROPS.get(props));

        if (avatar.isPresent()) {
            return new ImageSpan(avatar.get());
        } else {
            return userIsKnown
                    ? new AvatarPlaceholderSpan(avatarPlaceholder.get(), userId, url)
                    : new PotentialAvatarSpan(userId, url);
        }
    }

    static class AvatarPlaceholderSpan extends ImageSpan {
        @NonNull
        final String userId;
        @NonNull
        final String url;

        AvatarPlaceholderSpan(@NonNull Drawable drawable, @NonNull String userId, @NonNull String url) {
            super(drawable);
            this.userId = userId;
            this.url = url;
        }
    }

    record PotentialAvatarSpan(@NonNull String userId, @NonNull String url) {
    }
}