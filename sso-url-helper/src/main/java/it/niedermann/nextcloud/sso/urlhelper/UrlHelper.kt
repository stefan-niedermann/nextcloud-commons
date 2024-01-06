@file:JvmName("UrlHelper")

package it.niedermann.nextcloud.sso.urlhelper

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.nextcloud.android.sso.QueryParam
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.nextcloud.android.sso.model.SingleSignOnAccount
import java.net.MalformedURLException
import java.net.URL
import java.util.LinkedList
import java.util.Optional

class FallbackPreviewSize(val width: Int, val height: Int)
val Context.fallbackPreviewSize get() = FallbackPreviewSize(
    width = resources.displayMetrics.widthPixels,
    height = resources.displayMetrics.heightPixels,
)

/**
 * Generates an [URL] object from the given [model]
 * @throws [IllegalArgumentException] in case the [model] can not be processed or the [ssoAccount] is not valid for the [model]
 */
fun getAbsoluteUrl(ssoAccount: SingleSignOnAccount, model: String, fallbackPreviewSize: FallbackPreviewSize): URL {
    return try {
        // Check whether this is a complete URL, will throw an MalformedURLException if not
        val url = URL(model)
        // Verify it starts with the given ssoAccount
        if (model.startsWith(ssoAccount.url)) {
            rewriteSpecialURLs(ssoAccount, url, fallbackPreviewSize).orElse(url)
        } else {
            throw IllegalArgumentException("Given ${SingleSignOnAccount::class.java.simpleName} does not match the URL (${ssoAccount.url} vs. ${model}). Pass correct ${SingleSignOnAccount::class.java.simpleName} or use correct URL (or a plain ${String::class.java.simpleName}) to try fetching with the current ${SingleSignOnAccount::class.java.simpleName} stored in ${SingleAccountHelper::class.java.simpleName}.")
        }
    } catch (e: MalformedURLException) {
        if (model.startsWith("/")) {
            // This might be an absolute path instead of an URL, so prepend the URL of the ssoAccount, but be aware of accounts which are located in a sub directory!
            val url = URL(ssoAccount.url.substring(0, ssoAccount.url.length - URL(ssoAccount.url).path.length) + model)
            rewriteSpecialURLs(ssoAccount, url, fallbackPreviewSize).orElse(url)
        } else {
            throw IllegalArgumentException("URL must be absolute (starting with protocol and host or with a slash character).")
        }
    }
}

/**
 * @return a rewritten [URL] if something special has been detected, like Share IDs, File IDs or avatars. [Optional.empty] otherwise.
 */
private fun rewriteSpecialURLs(ssoAccount: SingleSignOnAccount, url: URL, fallbackPreviewSize: FallbackPreviewSize): Optional<URL> {
    // Exclude potential sub directory from url path (if Nextcloud instance is hosted at https://example.com/nextcloud)
    val pathStartingFromNextcloudRoot = if (url.query == null) {
        url.toString().substring(ssoAccount.url.length)
    } else {
        val urlStringWithoutLeadingAccount = url.toString().substring(ssoAccount.url.length)
        urlStringWithoutLeadingAccount.substring(0, urlStringWithoutLeadingAccount.length - url.query.length - 1)
    }

    val fileId = REGEX_FILE_ID.find(pathStartingFromNextcloudRoot)?.groupValues?.get(2)
    if (fileId != null) {
        return if (url.query == null) {
            Optional.of(URL("${ssoAccount.url}/index.php/core/preview?fileId=${fileId}&x=${fallbackPreviewSize.width}&y=${fallbackPreviewSize.height}&a=true"))
        } else {
            Optional.of(URL("${ssoAccount.url}/index.php/core/preview?fileId=${fileId}&x=${fallbackPreviewSize.width}&y=${fallbackPreviewSize.height}&a=true&${url.query}"))
        }
    }

    val shareId = REGEX_SHARE_ID.find(pathStartingFromNextcloudRoot)?.groupValues?.get(2)
    if (shareId != null) {
        return if (url.query == null) {
            Optional.of(URL("${ssoAccount.url}/index.php/s/${shareId}/download"))
        } else {
            Optional.of(URL("${ssoAccount.url}/index.php/s/${shareId}/download?${url.query}"))
        }
    }

    val avatarGroupValues = REGEX_AVATAR.find(pathStartingFromNextcloudRoot)?.groupValues
    val avatarUserId = if (avatarGroupValues?.get(2) == null) {
        ssoAccount.name
    } else {
        avatarGroupValues[2]
    }
    val avatarSize = avatarGroupValues?.get(3)
    if (avatarSize != null) {
        return if (url.query == null) {
            Optional.of(URL("${ssoAccount.url}/index.php/avatar/${avatarUserId}/${avatarSize}"))
        } else {
            Optional.of(URL("${ssoAccount.url}/index.php/avatar/${avatarUserId}/${avatarSize}?${url.query}"))
        }
    }

    if (!pathStartingFromNextcloudRoot.startsWith("/index.php") && !pathStartingFromNextcloudRoot.startsWith("/remote.php")) {
        val webDAV = REGEX_WEBDAV.find(pathStartingFromNextcloudRoot)?.groupValues?.get(2)
        if (webDAV != null) {
            return if (url.query == null) {
                Optional.of(URL("${ssoAccount.url}/remote.php/webdav/${webDAV}"))
            } else {
                Optional.of(URL("${ssoAccount.url}/remote.php/webdav/${webDAV}?${url.query}"))
            }
        }
    }

    // This leads to /index.php/apps/... or somewhere else. We should not manipulate this.
    return Optional.empty()
}

fun getQueryParams(url: URL): Collection<QueryParam> {
    if (TextUtils.isEmpty(url.query)) {
        return emptyList()
    }
    val queryParams: MutableList<QueryParam> = LinkedList()
    for (param in url.query.split("&").toTypedArray()) {
        if (param == "c") {
            Log.w(TAG, "Stripped query parameter \"c\". This is usually used as CSRF protection and must not be sent by the client because the SSO authenticates itself.")
        } else {
            val idx = param.indexOf("=")
            val key = if (idx > 0) param.substring(0, idx) else param
            val value =
                if (idx > 0 && param.length > idx + 1) param.substring(idx + 1) else null
            if (!TextUtils.isEmpty(key)) {
                queryParams.add(QueryParam(key, value))
            }
        }
    }
    return queryParams
}

private const val TAG = "UrlHelper"
private val REGEX_FILE_ID = Regex("^(/index\\.php)?/f/(\\d+)(/)?$")
private val REGEX_SHARE_ID = Regex("^(/index\\.php)?/s/(\\w+)(/|/download|/download/)?$")
private val REGEX_AVATAR = Regex("^(/index\\.php)?/avatar/([\\w-]+)/(\\d+)(/)?$")
private val REGEX_WEBDAV = Regex("^(/webdav)?/(.*)$")
