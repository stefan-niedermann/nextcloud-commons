package it.niedermann.android.markdown.model;

import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

public class NewSearchSpan extends MetricAffectingSpan {

    @ColorInt
    private final int colorBackground;
    @ColorInt
    private final int colorOnBackground;

    public NewSearchSpan(@ColorInt int colorBackground, @ColorInt int colorOnBackground) {
        this.colorBackground = colorBackground;
        this.colorOnBackground = colorOnBackground;
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.bgColor = colorBackground;
        tp.setColor(colorOnBackground);
        tp.setFakeBoldText(true);
    }

    @Override
    public void updateMeasureState(@NonNull TextPaint tp) {
        tp.setFakeBoldText(true);
    }
}