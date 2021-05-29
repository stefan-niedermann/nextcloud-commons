package it.niedermann.nextcloud.sso.glide

import android.content.Context
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.nextcloud.android.sso.model.SingleSignOnAccount
import java.io.InputStream

/**
 * Responsible for trying to fetch resources without an explicit [SingleSignOnAccount].
 * It will use the currently set [SingleSignOnAccount] of [SingleAccountHelper] if possible and also handle relative URLs.
 */
class StringLoader(private val context: Context) : ModelLoader<String, InputStream> {

    override fun handles(url: String): Boolean {
        return try {
            // We support this url if it starts with the current SingleSignOn accounts url
            url.startsWith(SingleAccountHelper.getCurrentSingleSignOnAccount(context).url) ||
                    // We also try to handle relative paths, assuming the requested resource is located at the current SingleSignOn account
                    url.startsWith("/")
        } catch (e: NextcloudFilesAppAccountNotFoundException) {
            false
        } catch (e: NoCurrentAccountSelectedException) {
            false
        }
    }

    override fun buildLoadData(url: String, width: Int, height: Int, options: Options): LoadData<InputStream> {
        return LoadData(ObjectKey(url), object : AbstractStreamFetcher<String>(context, url) {
            override fun getSingleSignOnAccount(context: Context, model: String): SingleSignOnAccount {
                return SingleAccountHelper.getCurrentSingleSignOnAccount(context)
            }
        })
    }

    class Factory(context: Context) : ModelLoaderFactory<String, InputStream> {
        private val loader: StringLoader = StringLoader(context)

        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<String, InputStream> {
            return loader
        }

        override fun teardown() {
            // Do nothing, this instance doesn't own the client.
        }
    }
}