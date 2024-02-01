package it.niedermann.android.markdown.markwon.plugins.mentions;

import androidx.annotation.NonNull;

import org.commonmark.node.CustomNode;

import io.noties.markwon.inlineparser.MarkwonInlineParserContext;

class MentionNode extends CustomNode {

    MentionNode(@NonNull MarkwonInlineParserContext context,
                       @NonNull String userId) {
        appendChild(new AvatarNode(context, userId));
        appendChild(new DisplayNameNode(context, userId));
    }
}