package it.niedermann.nextcloud.sso.glide

import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.Headers
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.nextcloud.android.sso.model.SingleSignOnAccount
import java.net.URL

/**
 * Use this as kind of [GlideUrl] if you want to do a [Glide] request from a [SingleSignOnAccount] which is not set by [SingleAccountHelper.setCurrentAccount].
 */
class SingleSignOnUrl : GlideUrl {
    constructor(ssoAccount: SingleSignOnAccount, url: String) : this(ssoAccount.name, url)
    constructor(ssoAccount: SingleSignOnAccount, url: URL) : this(ssoAccount.name, url)
    constructor(ssoAccount: SingleSignOnAccount, url: String, headers: Headers) : this(ssoAccount.name, url, headers)
    constructor(ssoAccount: SingleSignOnAccount, url: URL, headers: Headers) : this(ssoAccount.name, url, headers)
    constructor(ssoAccountName: String, url: String) : super(url, SingleSignOnOriginHeader(ssoAccountName))
    constructor(ssoAccountName: String, url: URL) : super(url, SingleSignOnOriginHeader(ssoAccountName))
    constructor(ssoAccountName: String, url: String, headers: Headers) : super(url, SingleSignOnOriginHeader(ssoAccountName, headers))
    constructor(ssoAccountName: String, url: URL, headers: Headers) : super(url, SingleSignOnOriginHeader(ssoAccountName, headers))
}