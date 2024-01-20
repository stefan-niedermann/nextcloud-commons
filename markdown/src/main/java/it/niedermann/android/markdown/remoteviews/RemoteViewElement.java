package it.niedermann.android.markdown.remoteviews;

import androidx.annotation.NonNull;

public record RemoteViewElement(
        @NonNull Type type,
        @NonNull String currentLineBlock,
        int blockStartsInLine,
        int blockEndsInLine
) {

    @NonNull
    @Override
    public String toString() {
        return type.name() + " (" + type.id + ") " + "\n" +
                "Content:" + "\n" +
                currentLineBlock + "\n" +
                "Started from " + blockStartsInLine + " to " + blockStartsInLine;
    }

    public enum Type {
        TEXT(0),
        CHECKBOX_CHECKED(1),
        CHECKBOX_UNCHECKED(2);

        private final int id;

        Type(final int id) {
            this.id = id;
        }
    }
}
