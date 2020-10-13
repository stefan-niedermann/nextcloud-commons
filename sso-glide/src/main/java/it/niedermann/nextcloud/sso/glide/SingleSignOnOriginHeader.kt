package it.niedermann.nextcloud.sso.glide

import androidx.annotation.RestrictTo
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.Headers
import com.bumptech.glide.load.model.LazyHeaders
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.nextcloud.android.sso.model.SingleSignOnAccount

@RestrictTo(RestrictTo.Scope.LIBRARY)
class SingleSignOnOriginHeader : Headers {
    private val headers: Headers

    /**
     * Use this as [Headers] if you want to do a [Glide] request for an [SingleSignOnAccount] which is not set by [SingleAccountHelper] as current [SingleSignOnAccount].
     * They also add User-Agent header.
     *
     * @param ssoAccountName Account name from which host the request should be fired (needs to match [SingleSignOnAccount.name])
     */
    constructor(ssoAccountName: String) {
        headers = LazyHeaders.Builder().addHeader(SingleSignOnStreamFetcher.X_HEADER_SSO_ACCOUNT_NAME, ssoAccountName).build()
    }

    /**
     * Use this as [Headers] if you want to do a [Glide] request for an [SingleSignOnAccount] which is not set by [SingleAccountHelper] as current [SingleSignOnAccount].
     * They also add User-Agent header.
     *
     * @param ssoAccountName Account name from which host the request should be fired (needs to match [SingleSignOnAccount.name])
     * @param headers        [Headers] which should be set additionally
     */
    constructor(ssoAccountName: String, headers: Headers) {
        val builder = LazyHeaders.Builder()
        for ((key, value) in headers.headers) {
            builder.addHeader(key!!, value!!)
        }
        builder.addHeader(SingleSignOnStreamFetcher.X_HEADER_SSO_ACCOUNT_NAME, ssoAccountName).build()
        this.headers = builder.build()
    }

    override fun getHeaders(): Map<String, String> {
        return headers.headers
    }
}