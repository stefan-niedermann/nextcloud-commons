package it.niedermann.android.markdown.markwon.plugins.mentions;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.commonmark.node.CustomNode;

import io.noties.markwon.inlineparser.MarkwonInlineParserContext;

class AvatarNode extends CustomNode {
    @NonNull
    public final String userId;
    public final boolean userIsKnown;
    @Nullable
    public final Drawable avatar;

    AvatarNode(@NonNull MarkwonInlineParserContext context,
               @NonNull String userId,
               boolean userIsKnown,
               @Nullable Drawable avatar) {
        this.userId = userId;
        this.userIsKnown = userIsKnown;
        this.avatar = avatar;
        appendChild(context.text("@"));
    }
}