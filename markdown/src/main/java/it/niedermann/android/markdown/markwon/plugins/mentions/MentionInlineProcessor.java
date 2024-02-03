package it.niedermann.android.markdown.markwon.plugins.mentions;

import android.text.TextUtils;

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
    @NonNull
    private final MentionsCache cache;

    MentionInlineProcessor(@NonNull MentionsCache cache,
                           @NonNull AtomicReference<SingleSignOnAccount> ssoAccountRef) {
        this.ssoAccountRef = ssoAccountRef;
        this.cache = cache;
    }

    @Override
    public char specialCharacter() {
        return '@';
    }

    protected Node parse() {
        if (ssoAccountRef.get() == null) {
            return null;
        }

        final String mention = match(REGEX_MENTION);

        if (TextUtils.isEmpty(mention) || mention.length() < 2) {
            return null;
        }

        final String userId = mention.substring(1);
        final var ssoAccount = ssoAccountRef.get();

        if (cache.isKnownInvalidUserId(ssoAccount, userId)) {
            return null;
        }

        return new MentionNode(super.context,
                userId,
                cache.getDisplayName(ssoAccount, userId).orElse(null),
                cache.getAvatar(ssoAccount, userId).orElse(null));
    }
}