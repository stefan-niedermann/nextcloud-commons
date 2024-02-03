package it.niedermann.android.markdown.markwon.plugins.mentions;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.nextcloud.android.sso.model.SingleSignOnAccount;

import org.commonmark.node.Node;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import io.noties.markwon.inlineparser.InlineProcessor;

class MentionInlineProcessor extends InlineProcessor {

    private static final Pattern REGEX_MENTION = Pattern.compile("\\B@\\w+");
    @NonNull
    private final AtomicReference<SingleSignOnAccount> ssoAccountRef;
    @NonNull
    private final Map<String, String> userCache;
    @NonNull
    private final Set<String> noUserCache;
    @NonNull
    private final Map<String, Drawable> avatarCache;

    MentionInlineProcessor(@NonNull Map<String, String> userCache,
                           @NonNull Set<String> noUserCache,
                           @NonNull Map<String, Drawable> avatarCache,
                           @NonNull AtomicReference<SingleSignOnAccount> ssoAccountRef) {
        this.ssoAccountRef = ssoAccountRef;
        this.userCache = userCache;
        this.noUserCache = noUserCache;
        this.avatarCache = avatarCache;
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

        if (TextUtils.isEmpty(mention) || mention.length() < 2 || noUserCache.contains(mention)) {
            return null;
        }

        final String userId = mention.substring(1);

        return new MentionNode(super.context, userId, userCache.get(userId), avatarCache.get(userId));
    }
}