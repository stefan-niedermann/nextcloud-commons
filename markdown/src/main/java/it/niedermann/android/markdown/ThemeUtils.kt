package it.niedermann.android.markdown

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import com.nextcloud.android.common.ui.theme.MaterialSchemes
import com.nextcloud.android.common.ui.theme.MaterialSchemes.Companion.fromColor
import com.nextcloud.android.common.ui.theme.ViewThemeUtilsBase
import dynamiccolor.MaterialDynamicColors

class ThemeUtils(schemes: MaterialSchemes) : ViewThemeUtilsBase(schemes) {
    private val dynamicColor = MaterialDynamicColors()

    fun getPrimary(context: Context): Int {
        return withScheme(context) { scheme -> dynamicColor.primary().getArgb(scheme) }
    }

    fun getOnPrimary(context: Context): Int {
        return withScheme(context) { scheme -> dynamicColor.onPrimary().getArgb(scheme) }
    }

    fun getSecondary(context: Context): Int {
        return withScheme(context) { scheme -> dynamicColor.secondary().getArgb(scheme) }
    }

    fun getOnSecondary(context: Context): Int {
        return withScheme(context) { scheme -> dynamicColor.onSecondary().getArgb(scheme) }
    }

    fun getOnSurfaceVariant(context: Context): Int {
        return withScheme(context) { scheme -> dynamicColor.onSurfaceVariant().getArgb(scheme) }
    }

    fun tintDrawable(
        context: Context,
        drawable: Drawable
    ): Drawable {
        return withScheme(context) { scheme ->
            drawable.setTintList(ColorStateList.valueOf(dynamicColor.onSurfaceVariant().getArgb(scheme)))
            drawable
        }
    }

    companion object {
        private val cache = mutableMapOf<Int, ThemeUtils>()
        fun of(color: Int): ThemeUtils {
            return cache.computeIfAbsent(color) {
                ThemeUtils(fromColor(color))
            }
        }
    }
}
