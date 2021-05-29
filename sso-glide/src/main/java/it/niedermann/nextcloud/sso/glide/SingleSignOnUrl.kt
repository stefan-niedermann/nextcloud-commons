package it.niedermann.nextcloud.sso.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.Headers
import com.nextcloud.android.sso.AccountImporter
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.nextcloud.android.sso.model.SingleSignOnAccount
import java.net.MalformedURLException
import java.net.URL

/**
 * Use this as kind of [GlideUrl] if you want to do a [Glide] request from a [SingleSignOnAccount] which is not set by [SingleAccountHelper.setCurrentAccount]. Supports also relative paths (starting with `/`).
 */
class SingleSignOnUrl : GlideUrl {

    val ssoAccount: SingleSignOnAccount;

    constructor(context: Context, ssoAccountName: String, url: URL) : this(AccountImporter.getSingleSignOnAccount(context, ssoAccountName), url, Headers.DEFAULT)
    constructor(context: Context, ssoAccountName: String, url: String) : this(AccountImporter.getSingleSignOnAccount(context, ssoAccountName), url, Headers.DEFAULT)
    constructor(context: Context, ssoAccountName: String, url: URL, headers: Headers) : this(AccountImporter.getSingleSignOnAccount(context, ssoAccountName), url, headers)
    constructor(context: Context, ssoAccountName: String, url: String, headers: Headers) : this(AccountImporter.getSingleSignOnAccount(context, ssoAccountName), url, headers)
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