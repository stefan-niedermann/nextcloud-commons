package it.niedermann.android.markdown.markwon.processor;

import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import io.noties.markwon.image.destination.ImageDestinationProcessor;

public class NextcloudImageDestinationProcessor extends ImageDestinationProcessor {


    private String targetURL = "";

    public NextcloudImageDestinationProcessor() {
    }

    public void setPrefix(String targetURLprefix) {
        targetURL = targetURLprefix;
    }

    @NonNull
    @Override
    public String process(@NonNull String destination) {
        final Uri uri = Uri.parse(destination);
        final String scheme = uri.getScheme();
        if (TextUtils.isEmpty(scheme)) {
            return targetURL + destination;
        }
        return destination;
    }
}