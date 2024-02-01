package it.niedermann.android.markdown.markwon.plugins.mentions;

import androidx.annotation.NonNull;

import com.nextcloud.android.sso.model.SingleSignOnAccount;

import org.commonmark.node.Node;

import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import io.noties.markwon.inlineparser.InlineProcessor;

class MentionInlineProcessor extends InlineProcessor {

    private static final Pattern REGEX_MENTION = Pattern.compile("\\B@\\w+");
    @NonNull
    private final AtomicReference<SingleSignOnAccount> ssoAccountRef;

    MentionInlineProcessor(@NonNull AtomicReference<SingleSignOnAccount> ssoAccountRef) {
        this.ssoAccountRef = ssoAccountRef;
    }

    @Override
    public char specialCharacter() {
        return '@';
    }

    protected Node parse() {
        if (ssoAccountRef.get() == null) {
            return null;
        }

        final String userId = match(REGEX_MENTION);
        if (userId != null) {
            return new MentionNode(super.context, userId.substring(1));
        }

        return null;
    }
}