package it.niedermann.android.markdown.markwon.plugins;

import android.content.Context;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

import com.nextcloud.android.sso.model.SingleSignOnAccount;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.MarkwonSpansFactory;
import io.noties.markwon.MarkwonVisitor;
import io.noties.markwon.Prop;
import io.noties.markwon.RenderProps;
import io.noties.markwon.SpanFactory;
import io.noties.markwon.image.AsyncDrawableScheduler;
import io.noties.markwon.image.ImageProps;
import io.noties.markwon.image.ImageSpanFactory;
import io.noties.markwon.inlineparser.InlineProcessor;
import io.noties.markwon.inlineparser.MarkwonInlineParser;
import io.noties.markwon.inlineparser.MarkwonInlineParserContext;
import it.niedermann.android.markdown.MarkdownUtil;
import it.niedermann.nextcloud.ocs.ApiProvider;
import it.niedermann.nextcloud.ocs.OcsAPI;

public class MentionsPlugin extends AbstractMarkwonPlugin {

    private static final String TAG = MentionsPlugin.class.getSimpleName();
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
    private final ExecutorService executor;
    @NonNull
    private final AtomicReference<SingleSignOnAccount> ssoAccountRef = new AtomicReference<>();
    @NonNull
    private final AtomicInteger textSizeRef;

    private MentionsPlugin(@NonNull ExecutorService executor, @Px int textSize) {
        this.executor = executor;
        this.textSizeRef = new AtomicInteger(textSize);
    }

    public static MarkwonPlugin create(@Px int textSize) {
        return new MentionsPlugin(Executors.newCachedThreadPool(), textSize);
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
        builder.on(AvatarNode.class, new AvatarVisitor(this.ssoAccountRef, this.textSizeRef));
        builder.on(DisplayNameNode.class, new DisplayNameVisitor());
    }

    @Override
    public void configureSpansFactory(@NonNull MarkwonSpansFactory.Builder builder) {
        builder.setFactory(AvatarNode.class, new ImageSpanFactory());
        builder.setFactory(DisplayNameNode.class, new DisplayNameSpanFactory());
    }

    @Override
    public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
        AsyncDrawableScheduler.unschedule(textView);
        super.beforeSetText(textView, markdown);
    }

    @Override
    public void afterSetText(@NonNull TextView textView) {
        super.afterSetText(textView);
        AsyncDrawableScheduler.schedule(textView);
        final var ssoAccount = ssoAccountRef.get();
        if (ssoAccount != null) {
            replaceUserNames(textView, ssoAccount);
        }
    }

    public void setCurrentSingleSignOnAccount(@Nullable SingleSignOnAccount ssoAccount) {
        userCache.clear();
        noUserCache.clear();
        ssoAccountRef.set(ssoAccount);
    }

    public void setTextSize(@Px int textSize) {
        textSizeRef.set(textSize);
    }

    static class MentionInlineProcessor extends InlineProcessor {

        @NonNull
        private final AtomicReference<SingleSignOnAccount> ssoAccountRef;

        private MentionInlineProcessor(@NonNull AtomicReference<SingleSignOnAccount> ssoAccountRef) {
            this.ssoAccountRef = ssoAccountRef;
        }

        private static final Pattern REGEX_MENTION = Pattern.compile("\\B@\\w+");

        @Override
        public char specialCharacter() {
            return '@';
        }

        protected Node parse() {
            if (ssoAccountRef.get() == null) {
                Log.i(TAG, "Skipping " + MentionsPlugin.class.getSimpleName() + " processing: Currently no " + SingleSignOnAccount.class.getSimpleName() + " is set.");
                return null;
            }

            final String userId = match(REGEX_MENTION);
            if (userId != null) {
                return new MentionNode(super.context, userId.substring(1));
            }

            return null;
        }
    }

    static class MentionNode extends CustomNode {

        public MentionNode(@NonNull MarkwonInlineParserContext context,
                           @NonNull String userId) {
            appendChild(new AvatarNode(context, userId));
            appendChild(new DisplayNameNode(context, userId));
        }
    }

    static class AvatarNode extends CustomNode {
        public final String userId;

        public AvatarNode(@NonNull MarkwonInlineParserContext context, @NonNull String userId) {
            this.userId = userId;
            appendChild(context.text("@"));
        }
    }

    static class DisplayNameNode extends CustomNode {
        public final String userId;

        public DisplayNameNode(@NonNull MarkwonInlineParserContext context, @NonNull String userId) {
            this.userId = userId;
            appendChild(context.text(userId));
        }
    }

    private record AvatarVisitor(
            @NonNull AtomicReference<SingleSignOnAccount> ssoAccountRef,
            @NonNull AtomicInteger textSizeRef) implements MarkwonVisitor.NodeVisitor<AvatarNode> {

        @Override
        public void visit(@NonNull MarkwonVisitor visitor, @NonNull AvatarNode avatarNode) {
            final var ssoAccount = ssoAccountRef.get();

            if (ssoAccount == null) {
                Log.w(TAG, AvatarVisitor.class.getSimpleName() + " tried to process avatars, but " + SingleSignOnAccount.class.getSimpleName() + " is null");
                return;
            }

            final int start = visitor.length();

            ImageProps.DESTINATION.set(visitor.renderProps(), getAvatarUrl(ssoAccount, avatarNode.userId, (int) (textSizeRef.get() * 1.5)));

            visitor.visitChildren(avatarNode);
            visitor.setSpansForNodeOptional(avatarNode, start);
        }

        private String getAvatarUrl(@NonNull SingleSignOnAccount ssoAccount, @NonNull String userId, @Px int size) {
            return ssoAccount.url + "/index.php/avatar/" + Uri.encode(userId) + "/" + size;
        }
    }

    private static class DisplayNameVisitor implements MarkwonVisitor.NodeVisitor<DisplayNameNode> {
        @Override
        public void visit(@NonNull MarkwonVisitor visitor, @NonNull DisplayNameNode displayNameNode) {
            final int start = visitor.length();
            MentionProps.MENTION_DISPLAY_NAME_PROPS.set(visitor.renderProps(), displayNameNode.userId);
            visitor.visitChildren(displayNameNode);
            visitor.setSpansForNodeOptional(displayNameNode, start);
        }
    }

    private static class DisplayNameSpanFactory implements SpanFactory {
        @Nullable
        @Override
        public Object getSpans(@NonNull MarkwonConfiguration configuration, @NonNull RenderProps props) {
            return new DisplayNameSpan(MentionProps.MENTION_DISPLAY_NAME_PROPS.require(props));
        }
    }

    private record DisplayNameSpan(@NonNull String userId) {
    }

    private abstract static class MentionProps {
        public static final Prop<String> MENTION_DISPLAY_NAME_PROPS = Prop.of("mention-display-name");

        private MentionProps() {
        }
    }

    private void replaceUserNames(@NonNull TextView textView, @NonNull SingleSignOnAccount ssoAccount) {
        final var spannable = MarkdownUtil.getContentAsSpannable(textView);
        final var userNames = findUserNames(spannable);
        fetchDisplayNames(textView.getContext(), ssoAccount, userNames).whenComplete((displayNames, exception) -> {
            if (exception == null) {
                final var spannableStringBuilder = new SpannableStringBuilder(spannable);
                final var displayNameSpans = spannable.getSpans(0, spannable.length(), DisplayNameSpan.class);

                int positionOffset = 0;

                for (int i = 0; i < displayNameSpans.length; i++) {
                    final var currentSpan = displayNameSpans[0];
                    if (displayNames.containsKey(currentSpan.userId)) {
                        final var displayName = Objects.requireNonNull(displayNames.get(currentSpan.userId));
                        final int originalStart = spannable.getSpanStart(currentSpan);
                        final int originalEnd = spannable.getSpanEnd(currentSpan);

                        spannableStringBuilder.replace(originalStart + positionOffset, originalEnd + positionOffset, displayName);
                        positionOffset += displayName.length() - currentSpan.userId.length();
                    }
                }
                textView.setText(spannableStringBuilder);
            } else {
                exception.printStackTrace();
            }
        });
    }

    public Set<String> findUserNames(@NonNull Spannable spannable) {
        return Arrays.stream(spannable.getSpans(0, spannable.length(), DisplayNameSpan.class))
                .map(span -> span.userId)
                .collect(Collectors.toUnmodifiableSet());
    }

    @NonNull
    public CompletableFuture<Map<String, String>> fetchDisplayNames(@NonNull Context context,
                                                                    @NonNull SingleSignOnAccount ssoAccount,
                                                                    @NonNull Set<String> potentialUserNames) {
        if (potentialUserNames.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyMap());
        }

        final var future = new CompletableFuture<Map<String, String>>();

        executor.submit(() -> {
            final var result = new ConcurrentHashMap<String, String>(potentialUserNames.size());
            final var latch = new CountDownLatch(potentialUserNames.size());
            final var apiFactory = new ApiProvider.Factory();
            try (final var apiProvider = apiFactory.createApiProvider(context, ssoAccount, OcsAPI.class, "/ocs/v2.php/cloud/")) {
                for (final var potentialUsername : potentialUserNames) {
                    fetchDisplayName(apiProvider.getApi(), potentialUsername).whenComplete((displayName, exception) -> {
                        if (exception == null) {
                            if (displayName != null) {
                                result.put(potentialUsername, displayName);
                            } else {
                                Log.v(TAG, "Username " + potentialUsername + " does not have a displayName");
                            }
                        } else {
                            Log.w(TAG, "Could not fetch display name for " + potentialUsername + ", " + exception.getMessage());
                        }
                        latch.countDown();
                    });
                }
                latch.await();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    @NonNull
    public CompletableFuture<String> fetchDisplayName(@NonNull OcsAPI ocsApi,
                                                      @NonNull String potentialUsername) {
        if (userCache.containsKey(potentialUsername)) {
            return CompletableFuture.completedFuture(userCache.get(potentialUsername));
        }

        final var future = new CompletableFuture<String>();

        if (noUserCache.contains(potentialUsername)) {
            future.complete(null);
            return future;
        }

        final var call = ocsApi.getUser(potentialUsername);

        executor.submit(() -> {
            try {
                final var response = call.execute();
                if (response.isSuccessful()) {
                    final var body = response.body();
                    if (body == null) {
                        future.completeExceptionally(new RuntimeException("Response body for " + potentialUsername + " was null."));

                    } else {
                        final var displayName = body.ocs.data.displayName;
                        userCache.putIfAbsent(potentialUsername, displayName);
                        future.complete(displayName);
                    }
                } else {
                    if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                        noUserCache.add(potentialUsername);
                    }

                    future.completeExceptionally(new RuntimeException("HTTP " + response.code() + ": " + response.message() + " (" + potentialUsername + ")"));
                }
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }
}
