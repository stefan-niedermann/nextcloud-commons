package it.niedermann.android.markdown.markwon.plugins.mentions;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.noties.markwon.AbstractMarkwonPlugin;

/**
 * @noinspection unused
 */
public class MentionsCache extends AbstractMarkwonPlugin {

    @NonNull
    private static final MentionsCache INSTANCE = new MentionsCache();

    /**
     * {@link Map} of existing users. Keys are the username, values are the display name
     */
    @NonNull
    private final Map<String, AccountCache> accountCaches = new ConcurrentHashMap<>();

    private MentionsCache() {
        // Silence is gold
    }

    @NonNull
    public static MentionsCache getInstance() {
        return INSTANCE;
    }

    private Optional<AccountCache> getCache(@NonNull SingleSignOnAccount ssoAccount) {
        return Optional.ofNullable(accountCaches.get(Objects.requireNonNull(ssoAccount.name)));
    }

    private AccountCache getOrCreateCache(@NonNull SingleSignOnAccount ssoAccount) {
        return accountCaches.computeIfAbsent(Objects.requireNonNull(ssoAccount.name), key -> new AccountCache());
    }

    public void clear() {
        accountCaches.values().forEach(AccountCache::clear);
        accountCaches.clear();
    }

    public void clear(@NonNull SingleSignOnAccount ssoAccount) {
        getCache(ssoAccount).ifPresent(AccountCache::clear);
        accountCaches.remove(Objects.requireNonNull(ssoAccount.name));
    }

    /**
     * @return <code>true</code> if the {@param userId} is known to exist for {@param ssoAccount}.
     */
    public boolean isKnownValidUserId(@NonNull SingleSignOnAccount ssoAccount, @NonNull String userId) {
        return getCache(ssoAccount)
                .map(cache -> cache.isKnownValidUserId(userId))
                .orElse(false);
    }

    /**
     * @return <code>true</code> if the {@param userId} is known to <strong>not</strong> exist for {@param ssoAccount}.
     */
    public boolean isKnownInvalidUserId(@NonNull SingleSignOnAccount ssoAccount, @NonNull String userId) {
        return getCache(ssoAccount)
                .map(cache -> cache.isKnownInvalidUserId(userId))
                .orElse(false);
    }

    public void addKnownInvalidUserId(@NonNull SingleSignOnAccount ssoAccount, @NonNull String userId) {
        getOrCreateCache(ssoAccount)
                .addKnownInvalidUserId(userId);
    }

    @NonNull
    public Optional<String> getDisplayName(@NonNull SingleSignOnAccount ssoAccount, @NonNull String userId) {
        return getCache(ssoAccount)
                .flatMap(cache -> cache.getDisplayName(userId));
    }

    public void setDisplayName(@NonNull SingleSignOnAccount ssoAccount, @NonNull String userId, @NonNull String displayName) {
        getOrCreateCache(ssoAccount)
                .setDisplayName(userId, displayName);
    }

    @NonNull
    public Optional<Drawable> getAvatar(@NonNull SingleSignOnAccount ssoAccount, @NonNull String userId) {
        return getCache(ssoAccount)
                .flatMap(cache -> cache.getAvatar(userId));
    }

    public void setAvatar(@NonNull SingleSignOnAccount ssoAccount, @NonNull String userId, @NonNull Drawable avatar) {
        getOrCreateCache(ssoAccount)
                .setAvatar(userId, avatar);
    }

    private static class AccountCache {
        /**
         * {@link Map} of existing users. Keys are the username, values are the display name
         */
        @NonNull
        private final Map<String, String> displayNameCache = new ConcurrentHashMap<>();

        @NonNull
        private final Map<String, Drawable> avatarCache = new ConcurrentHashMap<>();

        /**
         * {@link Set} of userIds which are known to belong to not existing users
         */
        @NonNull
        private final Set<String> invalidUserCache = ConcurrentHashMap.newKeySet();

        private AccountCache() {
            // Silence is gold
        }

        @NonNull
        public static MentionsCache getInstance() {
            return INSTANCE;
        }

        public void clear() {
            displayNameCache.clear();
            invalidUserCache.clear();
            avatarCache.clear();
        }

        public boolean isKnownValidUserId(@NonNull String userId) {
            return displayNameCache.containsKey(userId) || avatarCache.containsKey(userId);
        }

        public boolean isKnownInvalidUserId(@NonNull String userId) {
            return invalidUserCache.contains(userId);
        }

        public void addKnownInvalidUserId(@NonNull String userId) {
            this.invalidUserCache.add(userId);
        }

        @NonNull
        public Optional<String> getDisplayName(@NonNull String userId) {
            return Optional.ofNullable(displayNameCache.get(userId));
        }

        public void setDisplayName(@NonNull String userId, @NonNull String displayName) {
            this.displayNameCache.putIfAbsent(userId, displayName);
        }

        @NonNull
        public Optional<Drawable> getAvatar(@NonNull String userId) {
            return Optional.ofNullable(avatarCache.get(userId));
        }

        public void setAvatar(@NonNull String userId, @NonNull Drawable avatar) {
            avatarCache.putIfAbsent(userId, avatar);
        }
    }
}
