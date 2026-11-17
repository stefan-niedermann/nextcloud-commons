package it.niedermann.android.markdown.markwon.textwatcher

import android.text.SpanWatcher
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VisibleRangeRenderTextWatcherTest : TestCase() {

    private class CountingWatcher : SpanWatcher {
        var changed = 0
        override fun onSpanAdded(text: Spannable, what: Any, start: Int, end: Int) {}
        override fun onSpanRemoved(text: Spannable, what: Any, start: Int, end: Int) {}
        override fun onSpanChanged(text: Spannable, what: Any, ostart: Int, oend: Int, nstart: Int, nend: Int) {
            changed++
        }
    }

    @Test
    fun `should skip unchanged spans`() {
        val text = SpannableStringBuilder("foo bar")
        val span = StyleSpan(0)
        text.setSpan(span, 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val watcher = CountingWatcher()
        text.setSpan(watcher, 0, text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        VisibleRangeRenderTextWatcher.filtered(text, 100).setSpan(span, 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        assertEquals(0, watcher.changed)

        VisibleRangeRenderTextWatcher.filtered(text, 100).setSpan(span, 4, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        assertEquals(1, watcher.changed)
        assertEquals(4, text.getSpanStart(span))
    }

    @Test
    fun `should only apply spans up to the limit`() {
        val text = SpannableStringBuilder("foo bar baz")
        val filtered = VisibleRangeRenderTextWatcher.filtered(text, 4)
        val inside = StyleSpan(0)
        val outside = StyleSpan(0)

        filtered.setSpan(inside, 4, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        filtered.setSpan(outside, 8, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        assertEquals(4, text.getSpanStart(inside))
        assertEquals(-1, text.getSpanStart(outside))

        text.setSpan(outside, 8, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        filtered.setSpan(outside, 8, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        assertEquals(-1, text.getSpanStart(outside))
    }

    @Test
    fun `should pass other calls to the text`() {
        val text = SpannableStringBuilder("foo")
        val span = StyleSpan(0)
        text.setSpan(span, 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val filtered = VisibleRangeRenderTextWatcher.filtered(text, 100)
        assertEquals(3, filtered.length)
        filtered.removeSpan(span)
        assertEquals(-1, text.getSpanStart(span))
    }
}
