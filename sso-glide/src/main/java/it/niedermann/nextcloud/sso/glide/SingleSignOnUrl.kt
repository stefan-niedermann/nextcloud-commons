package it.niedermann.nextcloud.sso.glide

import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.Headers
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.nextcloud.android.sso.model.SingleSignOnAccount
import java.net.MalformedURLException
import java.net.URL

/**
 * Use this as kind of [GlideUrl] if you want to do a [Glide] request from a [SingleSignOnAccount] which is not set by [SingleAccountHelper.setCurrentAccount]. Supports also relative paths (starting with `/`).
 */
class SingleSignOnUrl : GlideUrl {

    val ssoAccount: SingleSignOnAccount;

    constructor(ssoAccount: SingleSignOnAccount, url: URL) : this(ssoAccount, url, Headers.DEFAULT)

    constructor(ssoAccount: SingleSignOnAccount, url: String) : this(ssoAccount, url, Headers.DEFAULT)

    constructor(ssoAccount: SingleSignOnAccount, url: URL, headers: Headers) : super(
            if (url.toString().startsWith(ssoAccount.url)) {
                url
            } else {
                throw IllegalArgumentException("Given ${SingleSignOnAccount::class.java.simpleName} does not match the URL (${ssoAccount.url} vs. ${url}). Use correct ${SingleSignOnAccount::class.java.simpleName} or default ${GlideUrl::class.java.simpleName}")
            }, headers
    ) {
        this.ssoAccount = ssoAccount
    }

    constructor(ssoAccount: SingleSignOnAccount, url: String, headers: Headers) : super(
            try {
                URL(url)
                if (url.startsWith(ssoAccount.url)) {
                    url
                } else {
                    throw IllegalArgumentException("Given ${SingleSignOnAccount::class.java.simpleName} does not match the URL (${ssoAccount.url} vs. ${url}). Use correct ${SingleSignOnAccount::class.java.simpleName} or default ${GlideUrl::class.java.simpleName}")
                }
            } catch (e: MalformedURLException) {
                if (url.startsWith(ssoAccount.url)) url else ssoAccount.url + url
            }, headers
    ) {
        this.ssoAccount = ssoAccount;
    }
}