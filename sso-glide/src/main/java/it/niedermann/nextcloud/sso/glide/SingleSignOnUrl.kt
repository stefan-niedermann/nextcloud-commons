package it.niedermann.nextcloud.sso.glide

import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.Headers
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.nextcloud.android.sso.model.SingleSignOnAccount
import java.net.URL

/**
 * Use this as kind of [GlideUrl] if you want to do a [Glide] request from a [SingleSignOnAccount] which is not set by [SingleAccountHelper.commitCurrentAccount].
 */
class SingleSignOnUrl : GlideUrl {

    val ssoAccountName: String

    constructor(ssoAccount: SingleSignOnAccount, url: String) : this(ssoAccount.name, url)
    constructor(ssoAccount: SingleSignOnAccount, url: URL) : this(ssoAccount.name, url)
    constructor(ssoAccount: SingleSignOnAccount, uri: Uri) : this(ssoAccount.name, uri.toString())
    constructor(ssoAccount: SingleSignOnAccount, url: String, headers: Headers) : this(ssoAccount.name, url, headers)
    constructor(ssoAccount: SingleSignOnAccount, url: URL, headers: Headers) : this(ssoAccount.name, url, headers)
    constructor(ssoAccount: SingleSignOnAccount, uri: Uri, headers: Headers) : this(ssoAccount.name, uri.toString(), headers)

    constructor(ssoAccountName: String, url: String) : super(url) {
        this.ssoAccountName = ssoAccountName
    }

    constructor(ssoAccountName: String, url: URL) : super(url) {
        this.ssoAccountName = ssoAccountName
    }

    constructor(ssoAccountName: String, uri: Uri) : super(uri.toString()) {
        this.ssoAccountName = ssoAccountName
    }

    constructor(ssoAccountName: String, url: String, headers: Headers) : super(url, headers) {
        this.ssoAccountName = ssoAccountName
    }

    constructor(ssoAccountName: String, url: URL, headers: Headers) : super(url, headers) {
        this.ssoAccountName = ssoAccountName
    }

    constructor(ssoAccountName: String, uri: Uri, headers: Headers) : super(uri.toString(), headers) {
        this.ssoAccountName = ssoAccountName
    }
}