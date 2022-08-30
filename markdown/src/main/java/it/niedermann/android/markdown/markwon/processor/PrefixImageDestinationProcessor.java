package it.niedermann.android.markdown.markwon.processor;

import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import io.noties.markwon.image.destination.ImageDestinationProcessor;

/**
 * Will append the given {@link #prefix} <strong>only for not fully qualified URLs</strong>.
 * URLs which start with a valid {@link Uri#getScheme()} will stay untouched.
 */
public class PrefixImageDestinationProcessor extends ImageDestinationProcessor {

    private String prefix = "";

    public PrefixImageDestinationProcessor() {
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @NonNull
    @Override
    public String process(@NonNull String destination) {
        final var uri = Uri.parse(destination);
        final var scheme = uri.getScheme();
        if (TextUtils.isEmpty(scheme)) {
            return prefix + destination;
        }
        return destination;
    }
}