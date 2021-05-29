package it.niedermann.nextcloud.sso.glide

import android.content.Context
import android.util.Log
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.nextcloud.android.sso.AccountImporter
import com.nextcloud.android.sso.model.SingleSignOnAccount
import java.io.InputStream

/**
 * A simple model loader for fetching media over http/https using OkHttp.
 */
class SingleSignOnUrlLoader(private val context: Context) : ModelLoader<SingleSignOnUrl, InputStream> {
    private val TAG = SingleSignOnUrlLoader::class.java.simpleName

    override fun handles(url: SingleSignOnUrl): Boolean {
        Log.i(TAG, "[handles] ------------------------------")
        Log.i(TAG, "[handles] ${url.toStringUrl()}")
        Log.i(TAG, "[handles] is SingleSignOnUrl → true")
        return true;
    }

    override fun buildLoadData(url: SingleSignOnUrl, width: Int, height: Int, options: Options): LoadData<InputStream> {
        return LoadData(url, object : AbstractStreamFetcher<SingleSignOnUrl>(context, url) {
            override fun getSingleSignOnAccount(context: Context, model: SingleSignOnUrl): SingleSignOnAccount {
                return AccountImporter.getSingleSignOnAccount(context, model.ssoAccountName)
            }
        })
    }

    /**
     * The default factory for [SingleSignOnUrlLoader]s.
     */
    class Factory(context: Context) : ModelLoaderFactory<SingleSignOnUrl, InputStream> {
        private val loader: SingleSignOnUrlLoader = SingleSignOnUrlLoader(context)
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<SingleSignOnUrl, InputStream> {
            return loader
        }

        override fun teardown() {
            // Do nothing, this instance doesn't own the client.
        }

    }
}