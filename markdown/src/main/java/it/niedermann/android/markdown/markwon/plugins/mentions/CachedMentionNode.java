package it.niedermann.android.markdown.markwon.plugins.mentions;

import androidx.annotation.NonNull;

import org.commonmark.node.CustomNode;

import io.noties.markwon.inlineparser.MarkwonInlineParserContext;

class CachedMentionNode extends CustomNode {

    CachedMentionNode(@NonNull MarkwonInlineParserContext context,
                      @NonNull String userId,
                      @NonNull String displayName) {
        appendChild(new AvatarPlaceholderNode(context, userId));
        appendChild(context.text(displayName));
    }
}