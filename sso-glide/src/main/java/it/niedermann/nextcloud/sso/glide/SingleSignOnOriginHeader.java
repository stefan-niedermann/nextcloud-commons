package it.niedermann.nextcloud.sso.glide;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.Headers;
import com.bumptech.glide.load.model.LazyHeaders;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.util.Map;

import static it.niedermann.nextcloud.sso.glide.SingleSignOnStreamFetcher.X_HEADER_SSO_ACCOUNT_NAME;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class SingleSignOnOriginHeader implements Headers {

    private final Headers headers;

    /**
     * Use this as {@link Headers} if you want to do a {@link Glide} request for an {@link SingleSignOnAccount} which is not set by {@link SingleAccountHelper} as current {@link SingleSignOnAccount}.
     * They also add User-Agent header.
     *
     * @param ssoAccountName Account name from which host the request should be fired (needs to match {@link SingleSignOnAccount#name})
     */
    public SingleSignOnOriginHeader(@NonNull String ssoAccountName) {
        this.headers = new LazyHeaders.Builder().addHeader(X_HEADER_SSO_ACCOUNT_NAME, ssoAccountName).build();
    }

    /**
     * Use this as {@link Headers} if you want to do a {@link Glide} request for an {@link SingleSignOnAccount} which is not set by {@link SingleAccountHelper} as current {@link SingleSignOnAccount}.
     * They also add User-Agent header.
     *
     * @param ssoAccountName Account name from which host the request should be fired (needs to match {@link SingleSignOnAccount#name})
     * @param headers        {@link Headers} which should be set additionally
     */
    public SingleSignOnOriginHeader(@NonNull String ssoAccountName, Headers headers) {
        LazyHeaders.Builder builder = new LazyHeaders.Builder();
        for (Map.Entry<String, String> entry : headers.getHeaders().entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        builder.addHeader(X_HEADER_SSO_ACCOUNT_NAME, ssoAccountName).build();
        this.headers = builder.build();
    }

    @Override
    public Map<String, String> getHeaders() {
        return this.headers.getHeaders();
    }
}