package it.niedermann.android.markdown.markwon.plugins.mentions;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;

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
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import it.niedermann.nextcloud.ocs.ApiProvider;
import it.niedermann.nextcloud.ocs.OcsAPI;

public class DisplayNameUtil {

    private static final String TAG = DisplayNameUtil.class.getSimpleName();
    @NonNull
    private final Map<String, String> userCache;
    @NonNull
    private final Set<String> noUserCache;

    public DisplayNameUtil(@NonNull Map<String, String> userCache,
                           @NonNull Set<String> noUserCache) {
        this.userCache = userCache;
        this.noUserCache = noUserCache;
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

        final var cachedUsers = userCache
                .entrySet()
                .stream()
                .filter(entry -> potentialUserNames.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        final var usernamesToCheck = potentialUserNames.stream()
                .filter(potentialUserName -> !userCache.containsKey(potentialUserName))
                .filter(potentialUserName -> !noUserCache.contains(potentialUserName))
                .collect(Collectors.toUnmodifiableSet());

        final var result = new ConcurrentHashMap<String, String>(cachedUsers.size() + usernamesToCheck.size());
        result.putAll(cachedUsers);

        final var latch = new CountDownLatch(usernamesToCheck.size());
        final var apiFactory = new ApiProvider.Factory();

        try (final var apiProvider = apiFactory.createApiProvider(context, ssoAccount, OcsAPI.class, "/ocs/v2.php/cloud/")) {
            final var executor = Executors.newFixedThreadPool(usernamesToCheck.size());

            for (final var potentialUsername : usernamesToCheck) {
                executor.submit(() -> {
                    try {
                        final String displayName = fetchDisplayName(context, apiProvider.getApi(), potentialUsername);

                        if (displayName != null) {
                            userCache.putIfAbsent(potentialUsername, displayName);
                            result.put(potentialUsername, displayName);
                        } else {
                            Log.v(TAG, "Username " + potentialUsername + " does not have a displayName");
                        }
                    } catch (NextcloudHttpRequestFailedException e) {
                        noUserCache.add(potentialUsername);
                    } catch (IOException exception) {
                        Log.w(TAG, "Could not fetch display name for " + potentialUsername + ", " + exception.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            executor.shutdown();
            latch.await();
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
                return body.ocs.data.displayName;
            }

        } else {
            final var exception = new RuntimeException("HTTP " + response.code() + ": " + response.message() + " (" + potentialUsername + ")");

            if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new NextcloudHttpRequestFailedException(context, HttpURLConnection.HTTP_NOT_FOUND, exception);
            }

            throw exception;
        }
    }
}
