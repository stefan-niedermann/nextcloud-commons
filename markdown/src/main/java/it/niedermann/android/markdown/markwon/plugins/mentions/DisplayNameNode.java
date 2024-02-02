package it.niedermann.android.markdown.markwon.plugins.mentions;

import androidx.annotation.NonNull;

import org.commonmark.node.CustomNode;

import io.noties.markwon.inlineparser.MarkwonInlineParserContext;

class DisplayNameNode extends CustomNode {
    final String userId;

    DisplayNameNode(@NonNull MarkwonInlineParserContext context, @NonNull String userId) {
        this.userId = userId;
        appendChild(context.text(userId));
    }
}