package it.niedermann.android.markdown.markwon.plugins.mentions;

import io.noties.markwon.Prop;

class MentionProps {
    public static final Prop<String> MENTION_AVATAR_URL_PROPS = Prop.of("mention-avatar-user-id");
    public static final Prop<String> MENTION_AVATAR_USER_ID_PROPS = Prop.of("mention-avatar-url");
    public static final Prop<String> MENTION_DISPLAY_NAME_USER_ID_PROPS = Prop.of("mention-display-name-user-id");

    private MentionProps() {
    }
}