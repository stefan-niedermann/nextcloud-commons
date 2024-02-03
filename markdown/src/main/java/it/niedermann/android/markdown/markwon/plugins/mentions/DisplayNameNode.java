package it.niedermann.android.markdown.markwon.plugins.mentions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.commonmark.node.CustomNode;

import io.noties.markwon.inlineparser.MarkwonInlineParserContext;

class DisplayNameNode extends CustomNode {
    @NonNull
    public final String userId;
    @Nullable
    public final String displayName;

    DisplayNameNode(@NonNull MarkwonInlineParserContext context,
                    @NonNull String userId,
                    @Nullable String displayName) {
        this.userId = userId;
        this.displayName = displayName;

        if (displayName == null) {
            appendChild(context.text(userId));
        } else {
            appendChild(context.text(" " + displayName));
        }
    }
}