package it.niedermann.android.markdown.controller;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import java.util.Map;

public record EditorState(
        @NonNull Map<Command, CommandState> commands,
        @ColorInt int color
) {
}
