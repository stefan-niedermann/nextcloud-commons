package it.niedermann.nextcloud.sso.glide

import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.LibraryGlideModule
import java.io.InputStream

/**
 * Registers SingleSignOn related classes via Glide's annotation processor.
 *
 * For Applications that depend on this library and include an [LibraryGlideModule] and Glide's
 * annotation processor, this class will be automatically included.
 */
@GlideModule
class SingleSignOnLibraryGlideModule : LibraryGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        Log.i(SingleSignOnLibraryGlideModule::class.java.simpleName, "Adding Glide support for Nextcloud SingleSignOn")
        registry.prepend(SingleSignOnUrl::class.java, InputStream::class.java, SingleSignOnUrlLoader.Factory(context))
    }
}