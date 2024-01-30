package it.niedermann.android.markdown

import android.content.Context
import com.nextcloud.android.common.ui.theme.MaterialSchemes
import com.nextcloud.android.common.ui.theme.MaterialSchemes.Companion.fromColor
import com.nextcloud.android.common.ui.theme.ViewThemeUtilsBase

class SearchThemeUtils(schemes: MaterialSchemes) : ViewThemeUtilsBase(schemes) {

    fun getPrimary(context: Context): Int {
        return withScheme(context) { scheme -> scheme.primary }
    }

    fun getOnPrimary(context: Context): Int {
        return withScheme(context) { scheme -> scheme.onPrimary }
    }

    fun getSecondary(context: Context): Int {
        return withScheme(context) { scheme -> scheme.secondary }
    }

    fun getOnSecondary(context: Context): Int {
        return withScheme(context) { scheme -> scheme.onSecondary }
    }

    companion object {
        private val cache = mutableMapOf<Int, SearchThemeUtils>()
        fun of(color: Int): SearchThemeUtils {
            return cache.computeIfAbsent(color) {
                SearchThemeUtils(fromColor(color))
            }
        }
    }

}