package it.niedermann.android.markdown.markwon.plugins.mentions;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    private final Map<MentionedUser, String> displayNameCache = new ConcurrentHashMap<>();

    @NonNull
    private final Map<MentionedUser, Drawable> avatarCache = new ConcurrentHashMap<>();

    /**
     * {@link Set} of userIds which are known to belong to not existing users
     */
    @NonNull
    private final Set<MentionedUser> invalidUserCache = ConcurrentHashMap.newKeySet();

    private MentionsCache() {
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

    public void clear(@NonNull SingleSignOnAccount ssoAccount) {
        for (final var key : displayNameCache.keySet()) {
            if (key.ssoAccountName.equals(ssoAccount.name)) {
                displayNameCache.remove(key);
            }
        }

        invalidUserCache.removeIf(user -> ssoAccount.name.equals(user.ssoAccountName));

        for (final var key : avatarCache.keySet()) {
            if (key.ssoAccountName.equals(ssoAccount.name)) {
                displayNameCache.remove(key);
            }
        }
    }

    /**
     * @return <code>true</code> if the {@param userId} is known to not exist for {@param ssoAccount}.
     */
    public boolean isKnownValidUserId(@NonNull SingleSignOnAccount ssoAccount, @NonNull String userId) {
        final var key = new MentionedUser(ssoAccount, userId);
        return displayNameCache.containsKey(key) || avatarCache.containsKey(key);
    }

    /**
     * @return <code>true</code> if the {@param userId} is known to not exist for {@param ssoAccount}.
     */
    public boolean isKnownInvalidUserId(@NonNull SingleSignOnAccount ssoAccount, @NonNull String userId) {
        return invalidUserCache.contains(new MentionedUser(ssoAccount, userId));
    }

    public void addKnownInvalidUserId(@NonNull SingleSignOnAccount ssoAccount, @NonNull String userId) {
        this.invalidUserCache.add(new MentionedUser(ssoAccount, userId));
    }

    /**
     * @return a {@link Map} with userId as keys and display names as values. Contains only entries which are searched for by {@param needles}
     * @see #getDisplayName(SingleSignOnAccount, String)
     */
    @NonNull
    public Map<String, String> getDisplayNames(@NonNull SingleSignOnAccount ssoAccount) {
        return displayNameCache
                .entrySet()
                .stream()
                .filter(cachedEntry -> cachedEntry.getKey().ssoAccountName.equals(ssoAccount.name))
                .collect(Collectors.toMap(cacheKeyStringEntry -> cacheKeyStringEntry.getKey().userId, Map.Entry::getValue));
    }

    @NonNull
    public Optional<String> getDisplayName(@NonNull SingleSignOnAccount ssoAccount, @NonNull String userId) {
        return Optional.ofNullable(displayNameCache.get(new MentionedUser(ssoAccount, userId)));
    }

    public void setDisplayName(@NonNull SingleSignOnAccount ssoAccount, @NonNull String userId, @NonNull String displayName) {
        this.displayNameCache.putIfAbsent(new MentionedUser(ssoAccount, userId), displayName);
    }

    @NonNull
    public Optional<Drawable> getAvatar(@NonNull SingleSignOnAccount ssoAccount, @NonNull String userId) {
        return Optional.ofNullable(avatarCache.get(new MentionedUser(ssoAccount, userId)));
    }

    public void setAvatar(@NonNull SingleSignOnAccount ssoAccount, @NonNull String userId, @NonNull Drawable avatar) {
        avatarCache.putIfAbsent(new MentionedUser(ssoAccount, userId), avatar);
    }

    private static class MentionedUser {
        @NonNull
        final String ssoAccountName;
        @NonNull
        final String userId;

        public MentionedUser(@NonNull SingleSignOnAccount ssoAccount, @NonNull String userId) {
            this.ssoAccountName = Objects.requireNonNull(ssoAccount.name);
            this.userId = userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MentionedUser mentionedUser = (MentionedUser) o;
            return Objects.equals(ssoAccountName, mentionedUser.ssoAccountName) && Objects.equals(userId, mentionedUser.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ssoAccountName, userId);
        }
    }
}
