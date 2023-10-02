package it.niedermann.android.markdown.remoteviews

import java.util.ArrayList

class RemoteViewElement(
    val type: Int,
    val currentLineBlock: String
) {

    companion object {
        const val TYPE_TEXT = 0
        const val TYPE_CHECKBOX_CHECKED = 1
        const val TYPE_CHECKBOX_UNCHECKED = 2
    }

    override fun toString(): String {
        var elements = StringBuilder()

        if(type == TYPE_TEXT) {
            elements.append("TYPE_TEXT").append("\n")
        }
        if(type == TYPE_CHECKBOX_CHECKED) {
            elements.append("TYPE_CHECKBOX_CHECKED").append("\n")
        }
        if(type == TYPE_CHECKBOX_UNCHECKED) {
            elements.append("TYPE_CHECKBOX_UNCHECKED").append("\n")
        }

        elements.append("Content:").append("\n")
        elements.append(currentLineBlock).append("\n")
        return elements.toString()
    }
}