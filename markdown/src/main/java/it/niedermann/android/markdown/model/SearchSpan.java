package it.niedermann.android.markdown.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

import androidx.annotation.ColorInt;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import it.niedermann.android.markdown.ThemeUtils;

/**
 * Only changes colors, so adding or removing it does not reflow the text layout.
 */
public class SearchSpan extends CharacterStyle implements UpdateAppearance {

    @ColorInt
    private final int colorBackground;
    @ColorInt
    private final int colorOnBackground;

    public SearchSpan(@ColorInt int colorBackground, @ColorInt int colorOnBackground) {
        this.colorBackground = colorBackground;
        this.colorOnBackground = colorOnBackground;
    }

    @Deprecated(forRemoval = true)
    public SearchSpan(@ColorInt int color, @ColorInt int ignoredColor, boolean current, boolean ignoredDarkTheme) {
        try {
            @SuppressLint("PrivateApi") final var context = (Context) Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication")
                    .invoke(null, (Object[]) null);

            Objects.requireNonNull(context);

            final var util = ThemeUtils.Companion.of(color);

            if (current) {
                this.colorBackground = util.getPrimary(context);
                this.colorOnBackground = util.getOnPrimary(context);
            } else {
                this.colorBackground = util.getSecondary(context);
                this.colorOnBackground = util.getOnSecondary(context);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.bgColor = colorBackground;
        tp.setColor(colorOnBackground);
    }
}