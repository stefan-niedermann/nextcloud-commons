package it.niedermann.android.markdown.markwon.plugins.mentions;

import android.graphics.drawable.Drawable;

import io.noties.markwon.Prop;

class MentionProps {
    public static final Prop<String> MENTION_USER_ID_PROPS = Prop.of("mention-user-id");
    public static final Prop<Boolean> MENTION_USER_IS_KNOWN_PROPS = Prop.of("mention-user-is-known");
    public static final Prop<String> MENTION_DISPLAY_NAME_PROPS = Prop.of("mention-display-name");
    public static final Prop<String> MENTION_AVATAR_URL_PROPS = Prop.of("mention-avatar-url");
    public static final Prop<Drawable> MENTION_AVATAR_DRAWABLE_PROPS = Prop.of("mention-avatar-drawable");

    private MentionProps() {
    }
}