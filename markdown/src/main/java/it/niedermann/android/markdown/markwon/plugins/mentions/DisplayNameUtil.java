package it.niedermann.android.markdown.markwon.plugins.mentions;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import it.niedermann.android.markdown.markwon.plugins.mentions.DisplayNameSpanFactory.PotentialDisplayNameSpan;
import it.niedermann.nextcloud.ocs.ApiProvider;
import it.niedermann.nextcloud.ocs.OcsAPI;

public class DisplayNameUtil {

    private static final String TAG = DisplayNameUtil.class.getSimpleName();
    private static final String API_URL_OCS = "/ocs/v2.php/cloud/";
    @NonNull
    private final MentionsCache cache;
    @NonNull
    private final ApiProvider.Factory apiFactory;
    @NonNull
    private final ExecutorServiceFactory executorFactory;

    public DisplayNameUtil(@NonNull MentionsCache cache) {
        this(cache,
                new ApiProvider.Factory(),
                taskCount -> Executors.newFixedThreadPool(Math.min(taskCount, 50)));
    }

    private DisplayNameUtil(@NonNull MentionsCache cache,
                            @NonNull ApiProvider.Factory apiFactory,
                            @NonNull ExecutorServiceFactory executorFactory) {
        this.cache = cache;
        this.apiFactory = apiFactory;
        this.executorFactory = executorFactory;
    }

    @WorkerThread
    @NonNull
    public Spannable insertActualDisplayNames(@NonNull Context context,
                                              @NonNull Spannable spannable,
                                              @NonNull SingleSignOnAccount ssoAccount) throws InterruptedException, NextcloudFilesAppAccountNotFoundException {
        final var potentialMentions = spannable.getSpans(0, spannable.length(), PotentialDisplayNameSpan.class);
        final var potentialUserNames = Arrays.stream(potentialMentions)
                .map(PotentialDisplayNameSpan::userId)
                .collect(Collectors.toUnmodifiableSet());

        final var displayNames = fetchDisplayNames(context, ssoAccount, potentialUserNames);

        final var spannableStringBuilder = new SpannableStringBuilder(spannable);
        final var displayNameSpans = Arrays.stream(potentialMentions)
                .filter(displayNameSpan -> displayNames.containsKey(displayNameSpan.userId()))
                .toArray(PotentialDisplayNameSpan[]::new);

        for (final var span : displayNameSpans) {
            final var displayName = " " + Objects.requireNonNull(displayNames.get(span.userId()));
            final int start = spannableStringBuilder.getSpanStart(span);
            final int end = spannableStringBuilder.getSpanEnd(span);

            spannableStringBuilder.replace(start, end, displayName);
        }
        return spannableStringBuilder;
    }

    @WorkerThread
    @NonNull
    public Map<String, String> fetchDisplayNames(@NonNull Context context,
                                                 @NonNull SingleSignOnAccount ssoAccount,
                                                 @NonNull Set<String> potentialUserNames) throws NextcloudFilesAppAccountNotFoundException, InterruptedException {
        if (potentialUserNames.isEmpty()) {
            return Collections.emptyMap();
        }

        final var validDisplayNames = potentialUserNames.stream()
                .map(potentialUserName -> new Pair<>(potentialUserName, cache.getDisplayName(ssoAccount, potentialUserName)))
                .filter(pair -> pair.second.isPresent())
                .map(pair -> new Pair<>(pair.first, pair.second.get()))
                .collect(Collectors.toUnmodifiableMap(pair -> pair.first, pair -> pair.second));

        final var usernamesToCheck = potentialUserNames.stream()
                .filter(potentialUserName -> !cache.isKnownValidUserId(ssoAccount, potentialUserName))
                .filter(potentialUserName -> !cache.isKnownInvalidUserId(ssoAccount, potentialUserName))
                .collect(Collectors.toUnmodifiableSet());

        if (usernamesToCheck.isEmpty()) {
            return validDisplayNames;
        }

        final var result = new ConcurrentHashMap<String, String>(validDisplayNames.size() + usernamesToCheck.size());
        result.putAll(validDisplayNames);

        final var latch = new CountDownLatch(usernamesToCheck.size());
        final var executor = this.executorFactory.createExecutor(usernamesToCheck.size());

        try (final var apiProvider = apiFactory.createApiProvider(context, ssoAccount, OcsAPI.class, API_URL_OCS)) {
            for (final var potentialUsername : usernamesToCheck) {
                executor.submit(() -> {
                    try {
                        final String displayName = fetchDisplayName(context, apiProvider.getApi(), potentialUsername);

                        if (displayName != null) {
                            cache.setDisplayName(ssoAccount, potentialUsername, displayName);
                            result.put(potentialUsername, displayName);
                        } else {
                            Log.v(TAG, "Username " + potentialUsername + " does not have a displayName");
                        }
                    } catch (NextcloudHttpRequestFailedException e) {
                        cache.addKnownInvalidUserId(ssoAccount, potentialUsername);
                    } catch (IOException exception) {
                        Log.w(TAG, "Could not fetch display name for " + potentialUsername + ", " + exception.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();

        } finally {
            executor.shutdown();
        }

        return result;
    }

    @WorkerThread
    private String fetchDisplayName(@NonNull Context context,
                                    @NonNull OcsAPI ocsApi,
                                    @NonNull String potentialUsername) throws IOException, NextcloudHttpRequestFailedException {
        final var call = ocsApi.getUser(potentialUsername);
        final var response = call.execute();

        if (response.isSuccessful()) {
            final var body = response.body();

            if (body == null) {
                throw new RuntimeException("Response body for " + potentialUsername + " was null.");
            } else {
                return body.ocs().data().displayName();
            }

        } else {
            final var exception = new RuntimeException("HTTP " + response.code() + ": " + response.message() + " (" + potentialUsername + ")");

            if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new NextcloudHttpRequestFailedException(context, HttpURLConnection.HTTP_NOT_FOUND, exception);
            }

            throw exception;
        }
    }

    interface ExecutorServiceFactory {
        @NonNull
        ExecutorService createExecutor(int taskCount);
    }
}
