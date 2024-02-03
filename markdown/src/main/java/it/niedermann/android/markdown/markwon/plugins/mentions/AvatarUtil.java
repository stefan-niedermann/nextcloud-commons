package it.niedermann.android.markdown.markwon.plugins.mentions;

import static it.niedermann.android.markdown.markwon.plugins.mentions.AvatarSpanFactory.AvatarPlaceholderSpan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.HttpException;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import it.niedermann.android.markdown.markwon.plugins.mentions.AvatarSpanFactory.PotentialAvatarSpan;

@WorkerThread
public class AvatarUtil {

    @NonNull
    private final Set<String> noUserCache;
    @NonNull
    private final Map<String, Drawable> avatarCache;
    @NonNull
    private final AtomicReference<Drawable> avatarPlaceholder;
    @NonNull
    private final AtomicReference<Drawable> avatarBroken;

    AvatarUtil(@NonNull Set<String> noUserCache,
               @NonNull Map<String, Drawable> avatarCache,
               @NonNull AtomicReference<Drawable> avatarPlaceholder,
               @NonNull AtomicReference<Drawable> avatarBroken) {
        this.noUserCache = noUserCache;
        this.avatarCache = avatarCache;
        this.avatarPlaceholder = avatarPlaceholder;
        this.avatarBroken = avatarBroken;
    }

    @WorkerThread
    public Spannable replacePotentialAvatarsWithPlaceholders(@NonNull Spannable foo) throws InterruptedException {
        final var spannable = new SpannableStringBuilder(foo);
        final var avatarSpans = Arrays.stream(spannable.getSpans(0, spannable.length(), PotentialAvatarSpan.class))
                .filter(span -> !noUserCache.contains(span.userId()))
                .toArray(PotentialAvatarSpan[]::new);

        for (final var span : avatarSpans) {
            final var spanStart = spannable.getSpanStart(span);
            final var spanEnd = spannable.getSpanEnd(span);
            spannable.setSpan(new AvatarPlaceholderSpan(avatarPlaceholder.get(), span.userId(), span.url()), spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannable;
    }

    @WorkerThread
    public Spannable insertActualAvatars(@NonNull Context context, @NonNull Spannable sourceSpannable) throws InterruptedException {
        final var spannable = new SpannableStringBuilder(sourceSpannable);
        final var avatarSpans = Arrays.stream(spannable.getSpans(0, spannable.length(), AvatarPlaceholderSpan.class))
                .filter(span -> !noUserCache.contains(span.userId))
                .toArray(AvatarPlaceholderSpan[]::new);

        final var latch = new CountDownLatch(avatarSpans.length);
        for (final var span : avatarSpans) {
            final var spanStart = spannable.getSpanStart(span);
            final var spanEnd = spannable.getSpanEnd(span);

            Glide.with(context)
                    .asBitmap()
                    .load(span.url)
                    .apply(RequestOptions.circleCropTransform())
                    .listener(new RequestListener<>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            latch.countDown();

                            if (e != null) {
                                final var causes = e.getRootCauses();
                                for (final var cause : causes) {
                                    if (cause instanceof HttpException httpException) {
                                        if (httpException.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                                            // Actually should never happen because the noUserCache should hold this user name from trying to fetch its display names
                                            noUserCache.add(span.userId);
                                            return false;
                                        }
                                    }
                                }
                                e.printStackTrace();
                            }

                            spannable.setSpan(new ImageSpan(avatarBroken.get()), spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            spannable.removeSpan(span);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            final var imageSpan = new ImageSpan(context, resource);
                            avatarCache.putIfAbsent(span.userId, imageSpan.getDrawable());
                            spannable.setSpan(imageSpan, spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            spannable.removeSpan(span);
                            latch.countDown();
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // silence is gold
                        }
                    });
        }
        latch.await();
        return spannable;
    }
}
