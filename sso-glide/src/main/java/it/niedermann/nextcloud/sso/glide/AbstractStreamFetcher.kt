package it.niedermann.nextcloud.sso.glide

import android.content.Context
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nextcloud.android.sso.aidl.NextcloudRequest
import com.nextcloud.android.sso.api.NextcloudAPI
import com.nextcloud.android.sso.api.NextcloudAPI.ApiConnectedListener
import com.nextcloud.android.sso.exceptions.TokenMismatchException
import com.nextcloud.android.sso.model.SingleSignOnAccount
import it.niedermann.nextcloud.sso.urlhelper.fallbackPreviewSize
import it.niedermann.nextcloud.sso.urlhelper.getAbsoluteUrl
import it.niedermann.nextcloud.sso.urlhelper.getQueryParams
import java.io.InputStream
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 * Fetches an [InputStream] using the Nextcloud SSO library.
 */
abstract class AbstractStreamFetcher<T>(
    private val context: Context,
    private val model: T,
    private val apiFactory: ApiFactory = object : ApiFactory {
        override fun build(
            context: Context,
            ssoAccount: SingleSignOnAccount,
            gson: Gson,
            callback: ApiConnectedListener
        ): NextcloudAPI {
            return NextcloudAPI(context, ssoAccount, gson, callback)
        }
    }
) : DataFetcher<InputStream> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream?>) {
        try {
            val ssoAccount: SingleSignOnAccount = getSingleSignOnAccount(context, model)
            var client = INITIALIZED_APIs[ssoAccount.name]
            val didInitialize = if (client == null) {
                client = apiFactory.build(
                    context,
                    ssoAccount,
                    GsonBuilder().create(),
                    object : ApiConnectedListener {
                        override fun onConnected() {
                            Log.v(TAG, "SSO API successfully initialized")
                        }

                        override fun onError(ex: Exception) {
                            Log.e(TAG, ex.message, ex)
                        }
                    })
                INITIALIZED_APIs[ssoAccount.name] = client
                true
            } else {
                false
            }
            val url = getAbsoluteUrl(ssoAccount, model.toString(), context.fallbackPreviewSize)
            val nextcloudRequest = NextcloudRequest.Builder()
                .setMethod(METHOD_GET)
                .setUrl(url.path.substring(URL(ssoAccount.url).path.length))
                .setParameter(getQueryParams(url))
                .build()
            try {
                val response = client.performNetworkRequestV2(nextcloudRequest)
                callback.onDataReady(response.body)
            } catch (e: TokenMismatchException) {
                Log.w(
                    TAG,
                    "SSO Glide loader failed with ${TokenMismatchException::class.java.simpleName}"
                )
                resetInitializedApi(ssoAccount.name)
                if (didInitialize) {
                    Log.i(
                        TAG,
                        "This API instance failed at the very first call, so we won't try to re-initialize the API this time…"
                    )
                    callback.onLoadFailed(e)
                } else {
                    Log.i(TAG, "This API instance worked before, so we try to re-initialize it…")
                    loadData(priority, callback)
                }
            }
        } catch (e: Exception) {
            callback.onLoadFailed(e)
        }
    }

    /**
     * @return the [SingleSignOnAccount] from whom to load the [model]
     */
    abstract fun getSingleSignOnAccount(context: Context, model: T): SingleSignOnAccount

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
        fun build(
            context: Context,
            ssoAccount: SingleSignOnAccount,
            gson: Gson,
            callback: ApiConnectedListener
        ): NextcloudAPI
    }

    companion object {
        private val TAG = AbstractStreamFetcher::class.java.simpleName
        private const val METHOD_GET = "GET"
        private val INITIALIZED_APIs: MutableMap<String, NextcloudAPI> = ConcurrentHashMap()

        @VisibleForTesting
        fun resetInitializedApis() {
            for ((key, _) in INITIALIZED_APIs) {
                resetInitializedApi(key)
            }
        }

        private fun resetInitializedApi(key: String) {
            INITIALIZED_APIs[key]?.stop()
            INITIALIZED_APIs.remove(key)
        }
    }
}