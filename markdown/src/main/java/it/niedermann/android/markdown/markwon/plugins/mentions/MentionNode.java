package it.niedermann.android.markdown.markwon.plugins.mentions;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.commonmark.node.CustomNode;

import io.noties.markwon.inlineparser.MarkwonInlineParserContext;

class MentionNode extends CustomNode {

    MentionNode(@NonNull MarkwonInlineParserContext context,
                @NonNull String userId,
                @Nullable String displayName,
                @Nullable Drawable avatar) {
        appendChild(new AvatarNode(context, userId, displayName != null, avatar));
        appendChild(new DisplayNameNode(context, userId, displayName));
    }
}