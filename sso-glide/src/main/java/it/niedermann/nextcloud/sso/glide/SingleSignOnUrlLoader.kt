package it.niedermann.nextcloud.sso.glide

import android.content.Context
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import java.io.InputStream

/**
 * A simple model loader for fetching media over http/https using OkHttp.
 */
class SingleSignOnUrlLoader(private val context: Context) : ModelLoader<GlideUrl, InputStream> {
    override fun handles(url: GlideUrl): Boolean {
        return true
    }

    override fun buildLoadData(url: GlideUrl, width: Int, height: Int, options: Options): LoadData<InputStream>? {
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