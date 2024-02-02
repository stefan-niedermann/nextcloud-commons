package it.niedermann.android.markdown.markwon;

import android.content.Context;

import androidx.annotation.NonNull;

import com.nextcloud.android.common.ui.util.PlatformThemeUtil;

@Deprecated(forRemoval = true)
public class MarkwonMarkdownUtil {

    private MarkwonMarkdownUtil() {
        // Util class
    }

    /**
     * @deprecated Use {@link PlatformThemeUtil#isDarkMode(Context)}
     */
    @Deprecated(forRemoval = true)
    public static boolean isDarkThemeActive(@NonNull Context context) {
        return PlatformThemeUtil.isDarkMode(context);
    }
}
