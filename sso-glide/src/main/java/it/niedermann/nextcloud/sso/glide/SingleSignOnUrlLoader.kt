package it.niedermann.nextcloud.sso.glide

import android.content.Context
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException
import com.nextcloud.android.sso.helper.SingleAccountHelper
import java.io.InputStream

/**
 * A simple model loader for fetching media over http/https using OkHttp.
 */
class SingleSignOnUrlLoader(private val context: Context) : ModelLoader<GlideUrl, InputStream> {
    override fun handles(url: GlideUrl): Boolean {
        return if (url is SingleSignOnUrl) {
            // It has explicitly been requested, so yeah, we assume that we support this.
            true
        } else try {
            // We are still not sure whether we can handle this, because it might be from another user account.
            // Though we should try it because it is likely.

            // if it does not start with it but is a relative path, like /foo/bar, we should assume that the resource is on this Nextcloud instance
            url.toStringUrl()?.startsWith("/")!! ||
                    // Or if the url is absolute and points to the url of the currently selected SingleSignOnAccount
                    url.toStringUrl()?.startsWith(SingleAccountHelper.getCurrentSingleSignOnAccount(context).url)!!
        } catch (e: NextcloudFilesAppAccountNotFoundException) {
            false
        } catch (e: NoCurrentAccountSelectedException) {
            false
        }
    }

    override fun buildLoadData(url: GlideUrl, width: Int, height: Int, options: Options): LoadData<InputStream> {
        return LoadData(url, SingleSignOnStreamFetcher(context, url))
    }

    /**
     * The default factory for [SingleSignOnUrlLoader]s.
     */
    class Factory(context: Context) : ModelLoaderFactory<GlideUrl, InputStream> {
        private val loader: SingleSignOnUrlLoader = SingleSignOnUrlLoader(context)
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<GlideUrl, InputStream> {
            return loader
        }

        override fun teardown() {
            // Do nothing, this instance doesn't own the client.
        }

    }
}