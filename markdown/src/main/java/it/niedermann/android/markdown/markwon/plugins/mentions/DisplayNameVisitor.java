package it.niedermann.android.markdown.markwon.plugins.mentions;

import androidx.annotation.NonNull;

import io.noties.markwon.MarkwonVisitor;

class DisplayNameVisitor implements MarkwonVisitor.NodeVisitor<DisplayNameNode> {
    @Override
    public void visit(@NonNull MarkwonVisitor visitor, @NonNull DisplayNameNode displayNameNode) {
        final int start = visitor.length();
        MentionProps.MENTION_USER_ID_PROPS.set(visitor.renderProps(), displayNameNode.userId);
        MentionProps.MENTION_DISPLAY_NAME_PROPS.set(visitor.renderProps(), displayNameNode.displayName);
        visitor.visitChildren(displayNameNode);
        visitor.setSpansForNodeOptional(displayNameNode, start);
    }
}