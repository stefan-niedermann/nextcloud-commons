package it.niedermann.android.markdown

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import com.nextcloud.android.common.ui.theme.MaterialSchemes
import com.nextcloud.android.common.ui.theme.MaterialSchemes.Companion.fromColor
import com.nextcloud.android.common.ui.theme.ViewThemeUtilsBase

class ThemeUtils(schemes: MaterialSchemes) : ViewThemeUtilsBase(schemes) {

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

    fun getOnSurfaceVariant(context: Context): Int {
        return withScheme(context) { scheme -> scheme.onSurfaceVariant }
    }

    fun tintDrawable(
        context: Context,
        drawable: Drawable
    ): Drawable {
        return withScheme(context) { scheme ->
            drawable.setTintList(ColorStateList.valueOf(scheme.onSurfaceVariant))
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