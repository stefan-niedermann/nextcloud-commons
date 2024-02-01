package it.niedermann.android.markdown.markwon.plugins.mentions;

import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;

class AvatarPlaceholderSpan extends ImageSpan {
    @NonNull
    final String userId;
    @NonNull
    final String url;

    AvatarPlaceholderSpan(@NonNull Drawable drawable, @NonNull PotentialAvatarSpan potentialAvatarSpan) {
        super(drawable);
        this.userId = potentialAvatarSpan.userId();
        this.url = potentialAvatarSpan.url();
    }
}