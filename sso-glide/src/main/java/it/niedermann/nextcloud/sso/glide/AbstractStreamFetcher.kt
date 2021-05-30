package it.niedermann.nextcloud.sso.glide

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.GlideUrl
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nextcloud.android.sso.aidl.NextcloudRequest
import com.nextcloud.android.sso.api.NextcloudAPI
import com.nextcloud.android.sso.api.NextcloudAPI.ApiConnectedListener
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException
import com.nextcloud.android.sso.exceptions.TokenMismatchException
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.nextcloud.android.sso.model.SingleSignOnAccount
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Fetches an [InputStream] using the Nextcloud SSO library.
 */
abstract class AbstractStreamFetcher<T>(private val context: Context,
                                        private val model: T,
                                        private val apiFactory: ApiFactory = object : ApiFactory {
                                            override fun build(context: Context, ssoAccount: SingleSignOnAccount, gson: Gson, callback: ApiConnectedListener): NextcloudAPI {
                                                return NextcloudAPI(context, ssoAccount, gson, callback)
                                            }
                                        }) : DataFetcher<InputStream> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream?>) {
        var client: NextcloudAPI?
        try {
            val ssoAccount: SingleSignOnAccount = getSingleSignOnAccount(context, model)
            client = INITIALIZED_APIs[ssoAccount.name]
            var didInitialize = false
            if (client == null) {
                client = apiFactory.build(context, ssoAccount, GsonBuilder().create(), object : ApiConnectedListener {
                    override fun onConnected() {
                        Log.v(TAG, "SSO API successfully initialized")
                    }

                    override fun onError(ex: Exception) {
                        Log.e(TAG, ex.message, ex)
                    }
                })
                INITIALIZED_APIs[ssoAccount.name] = client
                didInitialize = true
            }
            try {
                val url = getAbsoluteUrl(ssoAccount, model.toString())
                val nextcloudRequest = NextcloudRequest.Builder()
                        .setMethod(METHOD_GET)
                        .setUrl(url.path.substring(URL(ssoAccount.url).path.length))
                        .setParameter(getQueryParams(url))
                        .build()
                val response = client.performNetworkRequestV2(nextcloudRequest)
                callback.onDataReady(response.body)
            } catch (e: TokenMismatchException) {
                if (!didInitialize) {
                    Log.w(TAG, "SSO Glide loader failed with " + TokenMismatchException::class.java.simpleName + ", trying to re-initialize…")
                    client.stop()
                    INITIALIZED_APIs.remove(ssoAccount.name)
                    loadData(priority, callback)
                } else {
                    e.printStackTrace()
                    callback.onLoadFailed(e)
                }
            } catch (e: Exception) {
                callback.onLoadFailed(e)
            }
        } catch (e: NextcloudFilesAppAccountNotFoundException) {
            e.printStackTrace()
        } catch (e: NoCurrentAccountSelectedException) {
            e.printStackTrace()
        }
    }

    abstract fun getSingleSignOnAccount(context: Context, model: T): SingleSignOnAccount

    private fun getAbsoluteUrl(ssoAccount: SingleSignOnAccount, model: String): URL {
        return try {
            // Absolute URL
            val url = URL(model)
            // Verify it starts with the given ssoAccount
            if (model.startsWith(ssoAccount.url)) {
                url
            } else {
                throw IllegalArgumentException("Given ${SingleSignOnAccount::class.java.simpleName} does not match the URL (${ssoAccount.url} vs. ${model}). Pass correct ${SingleSignOnAccount::class.java.simpleName} or use default ${GlideUrl::class.java.simpleName} (or a plain ${String::class.java.simpleName}) to try fetching with the current ${SingleSignOnAccount::class.java.simpleName} stored in ${SingleAccountHelper::class.java.simpleName}.")
            }
        } catch (e: MalformedURLException) {
            // This might be a relative URL, prepend the URL of the ssoAccount
            if (model.startsWith("/")) {
                URL(ssoAccount.url + model)
            } else {
                throw IllegalArgumentException("URL must be absolute (starting with protocol and host or with a slash character).")
            }
        }
    }

    private fun getQueryParams(url: URL): Map<String?, String?> {
        if (TextUtils.isEmpty(url.query)) {
            return emptyMap<String?, String>()
        }
        val queryParams: MutableMap<String?, String?> = HashMap()
        for (param in url.query.split("&").toTypedArray()) {
            if ("c" == param) {
                Log.w(TAG, "Stripped query parameter \"c\". This is usually used as CSRF protection and must not be sent by the client because the SSO authenticates itself.")
            } else {
                val idx = param.indexOf("=")
                val key = if (idx > 0) param.substring(0, idx) else param
                val value = if (idx > 0 && param.length > idx + 1) param.substring(idx + 1) else null
                queryParams[key] = value
            }
        }
        return queryParams
    }

    override fun cleanup() {
        // Nothing to do here…
    }

    override fun cancel() {
        // Nothing to do here…
    }

    override fun getDataClass(): Class<InputStream> {
        return InputStream::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.REMOTE
    }

    interface ApiFactory {
        fun build(context: Context, ssoAccount: SingleSignOnAccount, gson: Gson, callback: ApiConnectedListener): NextcloudAPI
    }

    companion object {
        private val TAG = AbstractStreamFetcher::class.java.simpleName
        private const val METHOD_GET = "GET"
        private val INITIALIZED_APIs: MutableMap<String, NextcloudAPI> = ConcurrentHashMap()

        @VisibleForTesting
        fun resetInitializedApis() {
            for((key, _) in INITIALIZED_APIs) {
                INITIALIZED_APIs[key]?.stop()
                INITIALIZED_APIs.remove(key)
            }
        }
    }
}