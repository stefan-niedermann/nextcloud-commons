package it.niedermann.nextcloud.sso.glide

import android.content.Context
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.nextcloud.android.sso.AccountImporter
import com.nextcloud.android.sso.model.SingleSignOnAccount
import java.io.InputStream

/**
 * Responsible for fetching resources of explicit a given [SingleSignOnAccount] via [SingleSignOnUrl]
 * regardless the currently active [SingleSignOnAccount].
 */
class SingleSignOnUrlLoader(private val context: Context) : ModelLoader<SingleSignOnUrl, InputStream> {

    override fun handles(url: SingleSignOnUrl): Boolean {
        return true;
    }

    override fun buildLoadData(url: SingleSignOnUrl, width: Int, height: Int, options: Options): LoadData<InputStream> {
        return LoadData(url, object : AbstractStreamFetcher<SingleSignOnUrl>(context, url) {
            override fun getSingleSignOnAccount(context: Context, model: SingleSignOnUrl): SingleSignOnAccount {
                return AccountImporter.getSingleSignOnAccount(context, model.ssoAccountName)
            }
        })
    }

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