package it.niedermann.nextcloud.sso.glide

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.GlideUrl
import com.google.gson.GsonBuilder
import com.nextcloud.android.sso.AccountImporter
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

/**
 * Fetches an [InputStream] using the Nextcloud SSO library.
 */
class SingleSignOnStreamFetcher(private val context: Context, private val url: GlideUrl) : DataFetcher<InputStream> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream?>) {
        var client: NextcloudAPI?
        try {
            val ssoAccount: SingleSignOnAccount = if (url.headers.containsKey(X_HEADER_SSO_ACCOUNT_NAME)) {
                AccountImporter.getSingleSignOnAccount(context, url.headers[X_HEADER_SSO_ACCOUNT_NAME])
            } else {
                SingleAccountHelper.getCurrentSingleSignOnAccount(context)
            }
            client = INITIALIZED_APIs[ssoAccount.name]
            var didInitialize = false
            if (client == null) {
                client = NextcloudAPI(context, ssoAccount, GsonBuilder().create(), object : ApiConnectedListener {
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
            val requestBuilder: NextcloudRequest.Builder
            try {
                val urlObject = url.toURL();
                requestBuilder = NextcloudRequest.Builder()
                        .setMethod(METHOD_GET)
                        .setUrl(urlObject.path.substring(URL(ssoAccount.url).path.length))
                val header: MutableMap<String, List<String>> = HashMap()
                for ((key, value) in url.headers) {
                    if (X_HEADER_SSO_ACCOUNT_NAME != key) {
                        header[key] = listOf(value)
                    }
                }
                requestBuilder.setHeader(header)
                requestBuilder.setParameter(getQueryParams(urlObject))
                val nextcloudRequest = requestBuilder.build()
                Log.v(TAG, nextcloudRequest.toString())
                val response = client.performNetworkRequestV2(nextcloudRequest)
                callback.onDataReady(response.body)
            } catch (e: MalformedURLException) {
                callback.onLoadFailed(e)
            } catch (e: TokenMismatchException) {
                if (!didInitialize) {
                    Log.w(TAG, "SSO Glide loader failed with " + TokenMismatchException::class.java.simpleName + ", trying to re-initialize...")
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

    override fun cleanup() {
        // Nothing to do here...
    }

    override fun cancel() {
        // Nothing to do here...
    }

    override fun getDataClass(): Class<InputStream> {
        return InputStream::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.REMOTE
    }

    fun getQueryParams(url: URL): Map<String?, String?>? {
        if (TextUtils.isEmpty(url.query)) {
            return emptyMap<String?, String>()
        }
        val queryParams: MutableMap<String?, String?> = HashMap()
        for (param in url.query.split("&").toTypedArray()) {
            if ("c" == param) {
                Log.w(TAG, "stripped query parameter \"c\". This is usually used as CSRF protection and must not be sent by the client because the SSO authenticates itself.");
            } else {
                val idx = param.indexOf("=")
                val key = if (idx > 0) param.substring(0, idx) else param
                val value = if (idx > 0 && param.length > idx + 1) param.substring(idx + 1) else null
                queryParams[key] = value
            }
        }
        return queryParams
    }

    companion object {
        /**
         * Use this header and set the [SingleSignOnAccount] name property as value
         * Format of the value needs to be
         */
        const val X_HEADER_SSO_ACCOUNT_NAME = "X-SSO-Account-Name"
        private val TAG = SingleSignOnStreamFetcher::class.java.simpleName
        private const val METHOD_GET = "GET"
        private val INITIALIZED_APIs: MutableMap<String, NextcloudAPI> = HashMap()
    }
}