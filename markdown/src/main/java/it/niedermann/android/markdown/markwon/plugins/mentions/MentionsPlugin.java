package it.niedermann.android.markdown.markwon.plugins.mentions;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spanned;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.content.ContextCompat;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import org.commonmark.parser.Parser;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.MarkwonSpansFactory;
import io.noties.markwon.MarkwonVisitor;
import io.noties.markwon.inlineparser.MarkwonInlineParser;
import it.niedermann.android.markdown.MarkdownUtil;
import it.niedermann.android.markdown.R;
import it.niedermann.android.markdown.ThemeUtils;

public class MentionsPlugin extends AbstractMarkwonPlugin {

    /**
     * Map of existing users. Keys are the username, values are the display name
     */
    @NonNull
    private final Map<String, String> userCache = new ConcurrentHashMap<>();
    /**
     * Set of user names which are known to belong to not existing users
     */
    @NonNull
    private final Set<String> noUserCache = ConcurrentHashMap.newKeySet();
    @NonNull
    private final Context context;
    @NonNull
    private final ExecutorService executor;
    @NonNull
    private final DisplayNameUtil displayNameUtil;
    @NonNull
    private final AvatarUtil avatarUtil;
    @NonNull
    private final AtomicReference<SingleSignOnAccount> ssoAccountRef = new AtomicReference<>();
    @NonNull
    private final AtomicInteger avatarSizeRef = new AtomicInteger();

    private MentionsPlugin(@NonNull Context context,
                           @NonNull ExecutorService executor,
                           @Px int textSize,
                           @ColorInt int color) {
        this.context = context.getApplicationContext();
        this.executor = executor;
        this.avatarUtil = new AvatarUtil(noUserCache);
        this.displayNameUtil = new DisplayNameUtil(userCache, noUserCache);
        setTextSize(textSize);
        setColor(color);
    }

    public static MarkwonPlugin create(@NonNull Context context,
                                       @Px int textSize,
                                       @ColorInt int color) {
        return new MentionsPlugin(context, Executors.newCachedThreadPool(), textSize, color);
    }

    @Override
    public void configureParser(@NonNull Parser.Builder builder) {
        try {
            builder.inlineParserFactory(MarkwonInlineParser.factoryBuilder()
                    .addInlineProcessor(new MentionInlineProcessor(ssoAccountRef))
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void configureVisitor(@NonNull MarkwonVisitor.Builder builder) {
        builder.on(AvatarNode.class, new AvatarVisitor(ssoAccountRef, avatarSizeRef));
        builder.on(DisplayNameNode.class, new DisplayNameVisitor());
    }

    @Override
    public void configureSpansFactory(@NonNull MarkwonSpansFactory.Builder builder) {
        builder.setFactory(AvatarNode.class, new AvatarSpanFactory());
        builder.setFactory(DisplayNameNode.class, new DisplayNameSpanFactory());
    }

    @Override
    public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
        super.beforeSetText(textView, markdown);
    }

    @Override
    public void afterSetText(@NonNull TextView textView) {
        super.afterSetText(textView);
        final var ssoAccount = ssoAccountRef.get();
        if (ssoAccount != null) {
            executor.submit(() -> {
                final var spannable = MarkdownUtil.getContentAsSpannable(textView);
                try {
                    final var spannableWithDisplayNames = displayNameUtil.insertActualDisplayNames(textView.getContext(), spannable, ssoAccount);
                    final var spannableWithDisplayNamesAndAvatarPlaceholders = avatarUtil.replacePotentialAvatarsWithPlaceholders(spannableWithDisplayNames);

                    textView.post(() -> {
                        textView.setText(spannableWithDisplayNamesAndAvatarPlaceholders);
                        executor.submit(() -> {
                            try {
                                final var spannableWithDisplayNamesAndActualAvatars = avatarUtil.insertActualAvatars(textView.getContext(), MarkdownUtil.getContentAsSpannable(textView));
                                textView.post(() -> textView.setText(spannableWithDisplayNamesAndActualAvatars));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                    });
                } catch (InterruptedException | NextcloudFilesAppAccountNotFoundException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void setCurrentSingleSignOnAccount(@Nullable SingleSignOnAccount ssoAccount) {
        userCache.clear();
        noUserCache.clear();
        ssoAccountRef.set(ssoAccount);
    }

    public void setColor(@ColorInt int color) {
        final var utils = ThemeUtils.Companion.of(color);
        final var size = avatarSizeRef.get();

        avatarUtil.setAvatarPlaceholder(getTintedDrawable(utils, context, R.drawable.ic_baseline_account_circle_24dp, size));
        avatarUtil.setAvatarBroken(getTintedDrawable(utils, context, R.drawable.ic_baseline_broken_image_24, size));
    }

    private Drawable getTintedDrawable(@NonNull ThemeUtils utils, @NonNull Context context, @DrawableRes int drawableRes, @Px int size) {
        final var avatarBrokenDrawable = Objects.requireNonNull(ContextCompat.getDrawable(context, drawableRes));
        avatarBrokenDrawable.setBounds(0, 0, size, size);
        return utils.tintDrawable(context, avatarBrokenDrawable);
    }

    public void setTextSize(@Px int textSize) {
        avatarSizeRef.set((int) (textSize * 1.5));
    }
}
