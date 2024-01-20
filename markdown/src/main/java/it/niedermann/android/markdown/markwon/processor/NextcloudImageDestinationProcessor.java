package it.niedermann.android.markdown.markwon.processor;

import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import io.noties.markwon.image.destination.ImageDestinationProcessor;

public class NextcloudImageDestinationProcessor extends ImageDestinationProcessor {

    private String prefix = "";

    public NextcloudImageDestinationProcessor() {
    }

    public void setPrefix(@NonNull String prefix) {
        this.prefix = prefix;
    }

    @NonNull
    @Override
    public String process(@NonNull String destination) {
        final var uri = Uri.parse(destination);
        final String scheme = uri.getScheme();
        if (TextUtils.isEmpty(scheme)) {
            return prefix + destination;
        }
        return destination;
    }
}