package it.niedermann.android.markdown.markwon.plugins;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import org.commonmark.node.Node;
import org.commonmark.node.Visitor;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.InlineParserContext;
import org.commonmark.parser.Parser;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.MarkwonSpansFactory;
import io.noties.markwon.MarkwonVisitor;
import it.niedermann.android.markdown.MarkdownUtil;
import it.niedermann.android.markdown.R;
import it.niedermann.nextcloud.ocs.ApiProvider;
import it.niedermann.nextcloud.ocs.OcsAPI;

@Deprecated(forRemoval = true)
public class NextcloudMentionsPlugin extends AbstractMarkwonPlugin {

    private static final String TAG = NextcloudMentionsPlugin.class.getSimpleName();
    private static final Pattern REGEX_MENTION = Pattern.compile("\\B@(\\w+)");
    private final ApiProvider.Factory apiFactory;
    private final ExecutorService executor;
    private final Map<String, String> userCache = new ConcurrentHashMap<>();
    @NonNull
    private final Context context;
    @Nullable
    private SingleSignOnAccount ssoAccount;

    private NextcloudMentionsPlugin(@NonNull Context context,
                                    @NonNull ApiProvider.Factory apiFactory,
                                    @NonNull ExecutorService executor) {
        this.context = context.getApplicationContext();
        this.apiFactory = apiFactory;
        this.executor = executor;
    }

    public static MarkwonPlugin create(@NonNull Context context) {
        return new NextcloudMentionsPlugin(
                context,
                new ApiProvider.Factory(),
                Executors.newCachedThreadPool());
    }

    public void setCurrentSingleSignOnAccount(@Nullable SingleSignOnAccount ssoAccount, @NonNull TextView textView) {
        this.userCache.clear();
        this.ssoAccount = ssoAccount;
        afterSetText(textView);
    }

    @Override
    public void afterSetText(@NonNull TextView textView) {
        super.afterSetText(textView);
        final var spannable = MarkdownUtil.getContentAsSpannable(textView);
        if (ssoAccount != null) {
//            setupMentions(ssoAccount, textView);
        }
    }

    /**
     * Replaces all mentions in the textView with an avatar and the display name
     *
     * @param ssoAccount {@link SingleSignOnAccount} where the users of those mentions belong to
     * @param target     target {@link TextView}
     */
    public void setupMentions(@NonNull SingleSignOnAccount ssoAccount, @NonNull TextView target) {
        final var potentialMentions = findPotentialMentions(target.getText());
        final var mentionsFuture = fetchDisplayNames(context, ssoAccount, potentialMentions);
        mentionsFuture.whenComplete((mentions, exception) -> {
            final var context = target.getContext();

            // Step 1
            // Add avatar icons and display names
            final var messageBuilder = replaceAtMentionsWithImagePlaceholderAndDisplayName(context, mentions, target.getText());

            // Step 2
            // Replace avatar icons with real avatars
            final var list = messageBuilder.getSpans(0, messageBuilder.length(), MentionSpan.class);
            for (final var span : list) {
                final int spanStart = messageBuilder.getSpanStart(span);
                final int spanEnd = messageBuilder.getSpanEnd(span);
                Glide.with(context)
                        .asBitmap()
                        .placeholder(R.drawable.ic_person_grey600_24dp)
                        .error(R.drawable.ic_baseline_broken_image_24)
                        .load(ssoAccount.url + "/index.php/avatar/" + messageBuilder.subSequence(spanStart + 1, spanEnd) + "/" + span.getDrawable().getIntrinsicHeight())
                        .apply(RequestOptions.circleCropTransform())
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                messageBuilder.removeSpan(span);
                                messageBuilder.setSpan(new MentionSpan(context, resource), spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                // silence is gold
                            }
                        });
            }
            target.setText(messageBuilder);

            target.invalidate();
        });
    }

    /**
     * @param content to be searched for potential mentions
     * @return a {@link Set} of potential mentions without leading <code>@</code> character
     */
    @NonNull
    public static Set<String> findPotentialMentions(@Nullable CharSequence content) {
        if (content == null) {
            return Collections.emptySet();
        }

        final var result = new HashSet<String>();
        final var matcher = REGEX_MENTION.matcher(content);

        while (matcher.find()) {
            if (matcher.groupCount() > 0) {
                result.add(matcher.group(1));
            }
        }

        return result;
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
            try (final var apiProvider = apiFactory.createApiProvider(context, ssoAccount, OcsAPI.class, "/ocs/v2.php/cloud/")) {
                for (final var potentialUsername : potentialUserNames) {
                    fetchDisplayName(apiProvider.getApi(), potentialUsername).whenComplete((displayName, exception) -> {
                        if (exception == null) {
                            userCache.putIfAbsent(potentialUsername, displayName);
                            result.put(potentialUsername, displayName);
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
                                                      @NonNull String potentialUserName) {
        if (userCache.containsKey(potentialUserName)) {
            return CompletableFuture.completedFuture(userCache.get(potentialUserName));
        }

        final var future = new CompletableFuture<String>();

        executor.submit(() -> {
            final var call = ocsApi.getUser(potentialUserName);

            try {
                final var response = call.execute();

                if (response.isSuccessful()) {
                    final var body = response.body();

                    if (body == null) {
                        future.completeExceptionally(new RuntimeException("Response body for " + potentialUserName + " was null."));

                    } else {
                        future.complete(body.ocs.data.displayName);
                    }

                } else {
                    future.completeExceptionally(new RuntimeException("Response was not successful for " + potentialUserName));
                }

            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    private SpannableStringBuilder replaceAtMentionsWithImagePlaceholderAndDisplayName(@NonNull Context context,
                                                                                       @NonNull Map<String, String> mentions,
                                                                                       @NonNull CharSequence text) {
        final var messageBuilder = new SpannableStringBuilder(text);
        for (String userId : mentions.keySet()) {
            final String mentionId = "@" + userId;
            final String mentionDisplayName = " " + mentions.get(userId);
            int index = messageBuilder.toString().lastIndexOf(mentionId);
            while (index >= 0) {
                messageBuilder.setSpan(new MentionSpan(context, R.drawable.ic_person_grey600_24dp), index, index + mentionId.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                messageBuilder.insert(index + mentionId.length(), mentionDisplayName);
                index = messageBuilder.toString().substring(0, index).lastIndexOf(mentionId);
            }
        }
        return messageBuilder;
    }

    @Override
    public void configureParser(@NonNull Parser.Builder builder) {
        builder.inlineParserFactory(MentionInlineParser::new);
    }

    @Override
    public void configureVisitor(@NonNull MarkwonVisitor.Builder builder) {
        builder.on(Mention.class, (visitor, mention) -> {
            final int length = visitor.length();
            visitor.visitChildren(mention);
            visitor.setSpansForNodeOptional(mention, length);
        });
    }

    @Override
    public void configureSpansFactory(@NonNull MarkwonSpansFactory.Builder builder) {
        builder.setFactory(Mention.class, (configuration, props) -> {
            return new MentionSpan(context, R.drawable.ic_person_grey600_24dp);
        });
    }

    @Override
    public void beforeRender(@NonNull Node node) {
        super.beforeRender(node);
    }

    @Override
    public void afterRender(@NonNull Node node, @NonNull MarkwonVisitor visitor) {
        super.afterRender(node, visitor);
    }

    public static class MentionSpan extends ImageSpan {
        private MentionSpan(@NonNull Context context, int resourceId) {
            super(context, resourceId);
        }

        private MentionSpan(@NonNull Context context, @NonNull Bitmap bitmap) {
            super(context, bitmap);
        }
    }

    private static class MentionInlineParser implements InlineParser {

        public MentionInlineParser(InlineParserContext inlineParserContext) {

        }

        @Override
        public void parse(String input, Node node) {
            final var potentialMentions = findPotentialMentions(input);
            final var newNode = new Mention();
            node.appendChild(newNode);
        }
    }

    private static class Mention extends Node {

        @Override
        public void accept(Visitor visitor) {

        }
    }
}
