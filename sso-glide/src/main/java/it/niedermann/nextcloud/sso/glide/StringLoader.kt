package it.niedermann.nextcloud.sso.glide

import android.content.Context
import android.util.Log
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException
import com.nextcloud.android.sso.helper.SingleAccountHelper
import java.io.InputStream

/**
 * A simple model loader for fetching media over http/https using OkHttp.
 */
class StringLoader(private val context: Context) : ModelLoader<String, InputStream> {
    private val TAG = StringLoader::class.java.simpleName

    override fun handles(url: String): Boolean {
        Log.i(TAG, "[handles] ------------------------------")
        Log.i(TAG, "[handles] ${url}")
        return try {
            // We are still not sure whether we can handle this, because it might be from another user account.
            // Though we should try it because it is likely.

            Log.i(TAG, "[handles] starts with / → ${url.startsWith("/")}")
            Log.i(TAG, "[handles] starts with ${SingleAccountHelper.getCurrentSingleSignOnAccount(context).url} → ${url.startsWith(SingleAccountHelper.getCurrentSingleSignOnAccount(context).url)}")
            // if it does not start with it but is a relative path, like /foo/bar, we should assume that the resource is on this Nextcloud instance
            url.startsWith("/") ||
                    // Or if the url is absolute and points to the url of the currently selected SingleSignOnAccount
                    url.startsWith(SingleAccountHelper.getCurrentSingleSignOnAccount(context).url)
        } catch (e: NextcloudFilesAppAccountNotFoundException) {
            Log.i(TAG, "[handles] NextcloudFilesAppAccountNotFoundException → true")
            false
        } catch (e: NoCurrentAccountSelectedException) {
            Log.i(TAG, "[handles] NoCurrentAccountSelectedException → true")
            false
        }
    }

    override fun buildLoadData(url: String, width: Int, height: Int, options: Options): LoadData<InputStream> {
        return LoadData(ObjectKey(url), StringStreamFetcher(context, url))
    }

    /**
     * The default factory for [StringLoader]s.
     */
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