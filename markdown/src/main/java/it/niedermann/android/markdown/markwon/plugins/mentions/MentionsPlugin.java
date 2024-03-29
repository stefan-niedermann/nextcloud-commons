package it.niedermann.android.markdown.markwon.plugins.mentions;

import static java.util.function.Predicate.not;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spanned;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import org.commonmark.parser.Parser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
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

    @NonNull
    private final MentionsCache cache;
    @NonNull
    private final Collection<ExecutorService> executors = new HashSet<>(2);
    @NonNull
    private final Context context;
    @NonNull
    private final DisplayNameUtil displayNameUtil;
    @NonNull
    private final AvatarUtil avatarUtil;
    @NonNull
    private final AtomicReference<Drawable> avatarPlaceholder = new AtomicReference<>();
    @NonNull
    private final AtomicReference<Drawable> avatarBroken = new AtomicReference<>();
    @NonNull
    private final AtomicReference<SingleSignOnAccount> ssoAccountRef = new AtomicReference<>();
    @NonNull
    private final AtomicInteger avatarSizeRef = new AtomicInteger();

    private MentionsPlugin(@NonNull Context context,
                           @NonNull MentionsCache cache,
                           @ColorInt int color) {
        this.context = context.getApplicationContext();
        this.cache = cache;
        this.avatarUtil = new AvatarUtil(cache, avatarPlaceholder, avatarBroken);
        this.displayNameUtil = new DisplayNameUtil(cache);
        setColor(color);
    }

    public static MarkwonPlugin create(@NonNull Context context,
                                       @ColorInt int color) {
        return new MentionsPlugin(context, MentionsCache.getInstance(), color);
    }

    @Override
    public void configureParser(@NonNull Parser.Builder builder) {
        try {
            builder.inlineParserFactory(MarkwonInlineParser.factoryBuilder()
                    .addInlineProcessor(new MentionInlineProcessor(cache, ssoAccountRef))
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
        builder.setFactory(AvatarNode.class, new AvatarSpanFactory(avatarPlaceholder));
        builder.setFactory(DisplayNameNode.class, new DisplayNameSpanFactory());
    }

    @Override
    public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
        executors.stream()
                .filter(not(ExecutorService::isShutdown))
                .forEach(ExecutorService::shutdownNow);

        executors.removeIf(ExecutorService::isShutdown);

        super.beforeSetText(textView, markdown);
    }

    @Override
    public void afterSetText(@NonNull TextView textView) {
        super.afterSetText(textView);
        final var ssoAccount = ssoAccountRef.get();
        if (ssoAccount != null) {
            final var executor = Executors.newFixedThreadPool(2);
            executors.add(executor);
            executor.submit(() -> {
                final var spannable = MarkdownUtil.getContentAsSpannable(textView);
                try {
                    final var spannableWithDisplayNames = displayNameUtil.insertActualDisplayNames(textView.getContext(), spannable, ssoAccount);
                    if (executor.isShutdown()) return;
                    final var spannableWithDisplayNamesAndAvatarPlaceholders = avatarUtil.replacePotentialAvatarsWithPlaceholders(ssoAccount, spannableWithDisplayNames);
                    if (executor.isShutdown()) return;
                    textView.post(() -> {
                        textView.setText(spannableWithDisplayNamesAndAvatarPlaceholders);
                        if (executor.isShutdown()) return;
                        executor.submit(() -> {
                            try {
                                final var spannableWithDisplayNamesAndActualAvatars = avatarUtil.insertActualAvatars(ssoAccount, textView.getContext(), MarkdownUtil.getContentAsSpannable(textView));
                                if (executor.isShutdown()) return;
                                textView.post(() -> {
                                    if (executor.isShutdown()) return;
                                    textView.setText(spannableWithDisplayNamesAndActualAvatars);
                                    executor.shutdown();
                                });
                            } catch (InterruptedException ignored) {
                                executor.shutdown();
                            }
                        });
                    });
                } catch (InterruptedException e) {
                    executor.shutdown();
                } catch (NextcloudFilesAppAccountNotFoundException e) {
                    e.printStackTrace();
                    executor.shutdown();
                }
            });
        }
    }

    public void setCurrentSingleSignOnAccount(@Nullable SingleSignOnAccount ssoAccount) {
        ssoAccountRef.set(ssoAccount);
    }

    public void setColor(@ColorInt int color) {
        final var utils = ThemeUtils.Companion.of(color);

        avatarPlaceholder.set(getTintedDrawable(utils, context, R.drawable.ic_baseline_account_circle_24dp));
        avatarBroken.set(getTintedDrawable(utils, context, R.drawable.ic_baseline_broken_image_24));
    }

    private Drawable getTintedDrawable(@NonNull ThemeUtils utils, @NonNull Context context, @DrawableRes int drawableRes) {
        final var drawable = ContextCompat.getDrawable(context, drawableRes);
        final var tintedDrawable = Objects.requireNonNull(drawable);
        tintedDrawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        return utils.tintDrawable(context, tintedDrawable);
    }
}
