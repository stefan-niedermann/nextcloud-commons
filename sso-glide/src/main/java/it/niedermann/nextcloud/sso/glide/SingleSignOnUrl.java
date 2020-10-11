package it.niedermann.nextcloud.sso.glide;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.net.URL;

/**
 * Use this as kind of {@link GlideUrl} if you want to do a {@link Glide} request from a {@link SingleSignOnAccount} which is not set by {@link SingleAccountHelper#setCurrentAccount(Context, String)}.
 */
public class SingleSignOnUrl extends GlideUrl {

    public SingleSignOnUrl(@NonNull SingleSignOnAccount ssoAccount, @NonNull String url) {
        this(ssoAccount.name, url);
    }

    public SingleSignOnUrl(@NonNull SingleSignOnAccount ssoAccount, @NonNull URL url) {
        this(ssoAccount.name, url);
    }

    public SingleSignOnUrl(@NonNull SingleSignOnAccount ssoAccount, @NonNull String url, @NonNull Headers headers) {
        this(ssoAccount.name, url, headers);
    }

    public SingleSignOnUrl(@NonNull SingleSignOnAccount ssoAccount, @NonNull URL url, @NonNull Headers headers) {
        this(ssoAccount.name, url, headers);
    }

    public SingleSignOnUrl(@NonNull String ssoAccountName, @NonNull String url) {
        super(url, new SingleSignOnOriginHeader(ssoAccountName));
    }

    public SingleSignOnUrl(@NonNull String ssoAccountName, @NonNull URL url) {
        super(url, new SingleSignOnOriginHeader(ssoAccountName));
    }

    public SingleSignOnUrl(@NonNull String ssoAccountName, @NonNull String url, @NonNull Headers headers) {
        super(url, new SingleSignOnOriginHeader(ssoAccountName, headers));
    }

    public SingleSignOnUrl(@NonNull String ssoAccountName, @NonNull URL url, @NonNull Headers headers) {
        super(url, new SingleSignOnOriginHeader(ssoAccountName, headers));
    }
}
