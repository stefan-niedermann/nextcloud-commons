package it.niedermann.android.markdown.markwon.plugins

import android.R
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.util.Range
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.SpannableBuilder
import io.noties.markwon.ext.tasklist.TaskListSpan
import it.niedermann.android.markdown.markwon.plugins.ToggleableTaskListPlugin.ToggleMarkerSpan
import it.niedermann.android.markdown.markwon.span.InterceptedURLSpan
import it.niedermann.android.markdown.markwon.span.ToggleTaskListSpan
import junit.framework.TestCase
import org.commonmark.node.Node
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.stream.Collectors

@RunWith(RobolectricTestRunner::class)
class ToggleableTaskListPluginTest : TestCase() {
    @Test
    @Throws(IllegalAccessException::class, InvocationTargetException::class, InstantiationException::class, NoSuchMethodException::class)
    fun testAfterRender() {
        val node = mockk<Node>()
        val visitor = mockk<MarkwonVisitor>()
        val markerSpanConstructor = ToggleMarkerSpan::class.java.getDeclaredConstructor(TaskListSpan::class.java)
        markerSpanConstructor.isAccessible = true
        val builder = SpannableBuilder("Lorem Ipsum Dolor \nSit Amet")
        builder.setSpan(markerSpanConstructor.newInstance(mockk<TaskListSpan>()), 0, 6)
        builder.setSpan(URLSpan(""), 6, 11)
        builder.setSpan(markerSpanConstructor.newInstance(mockk<TaskListSpan>()), 11, 19)
        builder.setSpan(InterceptedURLSpan(emptyList(), ""), 19, 22)
        builder.setSpan(markerSpanConstructor.newInstance(mockk<TaskListSpan>()), 22, 27)
        every { visitor.builder() } returns builder
        val plugin = ToggleableTaskListPlugin { _: Int?, _: Boolean? -> }
        plugin.afterRender(node, visitor)

        // We ignore marker spans in this test. They will be removed in another step
        val spans = builder.getSpans(0, builder.length)
            .stream()
            .filter { span: SpannableBuilder.Span -> span.what.javaClass != ToggleMarkerSpan::class.java }
            .sorted(Comparator.comparingInt { o: SpannableBuilder.Span -> o.start })
            .collect(Collectors.toList())
        assertEquals(5, spans.size)
        assertEquals(ToggleTaskListSpan::class.java, spans[0].what.javaClass)
        assertEquals(0, spans[0].start)
        assertEquals(6, spans[0].end)
        assertEquals(URLSpan::class.java, spans[1].what.javaClass)
        assertEquals(6, spans[1].start)
        assertEquals(11, spans[1].end)
        assertEquals(ToggleTaskListSpan::class.java, spans[2].what.javaClass)
        assertEquals(11, spans[2].start)
        assertEquals(19, spans[2].end)
        assertEquals(InterceptedURLSpan::class.java, spans[3].what.javaClass)
        assertEquals(19, spans[3].start)
        assertEquals(22, spans[3].end)
        assertEquals(ToggleTaskListSpan::class.java, spans[4].what.javaClass)
        assertEquals(22, spans[4].start)
        assertEquals(27, spans[4].end)
    }

    @Test
    @Throws(IllegalAccessException::class, InvocationTargetException::class, InstantiationException::class, NoSuchMethodException::class)
    fun testAfterSetText() {
        val markerSpanConstructor = ToggleMarkerSpan::class.java.getDeclaredConstructor(TaskListSpan::class.java)
        markerSpanConstructor.isAccessible = true
        val editable = SpannableStringBuilder("Lorem Ipsum Dolor \nSit Amet")
        editable.setSpan(markerSpanConstructor.newInstance(mockk<TaskListSpan>()), 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        editable.setSpan(URLSpan(""), 6, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        editable.setSpan(markerSpanConstructor.newInstance(mockk<TaskListSpan>()), 11, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        editable.setSpan(InterceptedURLSpan(emptyList(), ""), 19, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        editable.setSpan(markerSpanConstructor.newInstance(mockk<TaskListSpan>()), 22, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val textView = TextView(ApplicationProvider.getApplicationContext())
        textView.text = editable
        assertEquals(3, (textView.text as Spanned).getSpans(0, textView.text.length, ToggleMarkerSpan::class.java).size)
        val plugin = ToggleableTaskListPlugin { i: Int?, b: Boolean? -> }
        plugin.afterSetText(textView)
        assertEquals(0, (textView.text as Spanned).getSpans(0, textView.text.length, ToggleMarkerSpan::class.java).size)
    }

    @Test
    @Throws(NoSuchMethodException::class, InvocationTargetException::class, IllegalAccessException::class)
    fun testGetSortedSpans() {
        val method = ToggleableTaskListPlugin::class.java.getDeclaredMethod("getSortedSpans", SpannableBuilder::class.java, Class::class.java, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        method.isAccessible = true
        val firstClickableSpan = URLSpan("")
        val secondClickableSpan = InterceptedURLSpan(emptyList(), "")
        val unclickableSpan = ForegroundColorSpan(R.color.white)
        val spannable = SpannableBuilder("Lorem Ipsum Dolor \nSit Amet")
        spannable.setSpan(firstClickableSpan, 6, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(secondClickableSpan, 19, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(unclickableSpan, 3, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        var clickableSpans: List<SpannableBuilder.Span>
        clickableSpans = method.invoke(null, spannable, ClickableSpan::class.java, 0, 0) as List<SpannableBuilder.Span>
        assertEquals(0, clickableSpans.size)
        clickableSpans = method.invoke(null, spannable, ClickableSpan::class.java, spannable.length - 1, spannable.length - 1) as List<SpannableBuilder.Span>
        assertEquals(0, clickableSpans.size)
        clickableSpans = method.invoke(null, spannable, ClickableSpan::class.java, 0, 5) as List<SpannableBuilder.Span>
        assertEquals(0, clickableSpans.size)
        clickableSpans = method.invoke(null, spannable, ClickableSpan::class.java, 0, spannable.length) as List<SpannableBuilder.Span>
        assertEquals(2, clickableSpans.size)
        assertEquals(firstClickableSpan, clickableSpans[0].what)
        assertEquals(secondClickableSpan, clickableSpans[1].what)
        clickableSpans = method.invoke(null, spannable, ClickableSpan::class.java, 0, 17) as List<SpannableBuilder.Span>
        assertEquals(1, clickableSpans.size)
        assertEquals(firstClickableSpan, clickableSpans[0].what)
        clickableSpans = method.invoke(null, spannable, ClickableSpan::class.java, 12, 22) as List<SpannableBuilder.Span>
        assertEquals(1, clickableSpans.size)
        assertEquals(secondClickableSpan, clickableSpans[0].what)
        clickableSpans = method.invoke(null, spannable, ClickableSpan::class.java, 9, 20) as List<SpannableBuilder.Span>
        assertEquals(2, clickableSpans.size)
        assertEquals(firstClickableSpan, clickableSpans[0].what)
        assertEquals(secondClickableSpan, clickableSpans[1].what)
    }

    @Test
    @Throws(NoSuchMethodException::class, InvocationTargetException::class, IllegalAccessException::class)
    fun testFindFreeRanges() {
        val method = ToggleableTaskListPlugin::class.java.getDeclaredMethod("findFreeRanges", SpannableBuilder::class.java, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        method.isAccessible = true
        val firstClickableSpan = URLSpan("")
        val secondClickableSpan = InterceptedURLSpan(emptyList(), "")
        var spannable = SpannableBuilder("Lorem Ipsum Dolor \nSit Amet")
        spannable.setSpan(firstClickableSpan, 6, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(secondClickableSpan, 19, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        var freeRanges: List<Range<Int>>
        freeRanges = method.invoke(null, spannable, 0, 0) as List<Range<Int>>
        assertEquals(0, freeRanges.size)
        freeRanges = method.invoke(null, spannable, spannable.length - 1, spannable.length - 1) as List<Range<Int>>
        assertEquals(0, freeRanges.size)
        freeRanges = method.invoke(null, spannable, 0, 6) as List<Range<Int>>
        assertEquals(1, freeRanges.size)
        assertEquals(0, freeRanges[0].lower as Int)
        assertEquals(6, freeRanges[0].upper as Int)
        freeRanges = method.invoke(null, spannable, 0, 6) as List<Range<Int>>
        assertEquals(1, freeRanges.size)
        assertEquals(0, freeRanges[0].lower as Int)
        assertEquals(6, freeRanges[0].upper as Int)
        freeRanges = method.invoke(null, spannable, 3, 15) as List<Range<Int>>
        assertEquals(2, freeRanges.size)
        assertEquals(3, freeRanges[0].lower as Int)
        assertEquals(6, freeRanges[0].upper as Int)
        assertEquals(11, freeRanges[1].lower as Int)
        assertEquals(15, freeRanges[1].upper as Int)
        freeRanges = method.invoke(null, spannable, 0, spannable.length) as List<Range<Int>>
        assertEquals(3, freeRanges.size)
        assertEquals(0, freeRanges[0].lower as Int)
        assertEquals(6, freeRanges[0].upper as Int)
        assertEquals(11, freeRanges[1].lower as Int)
        assertEquals(19, freeRanges[1].upper as Int)
        assertEquals(22, freeRanges[2].lower as Int)
        assertEquals(27, freeRanges[2].upper as Int)

        // https://github.com/stefan-niedermann/nextcloud-notes/issues/1326
        spannable = SpannableBuilder("Test Crash\n\nJust text with link https://github.com")
        spannable.setSpan(firstClickableSpan, 32, 50, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(secondClickableSpan, 32, 50, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        freeRanges = method.invoke(null, spannable, 12, 50) as List<Range<Int>>
        assertEquals(1, freeRanges.size)
    }
}