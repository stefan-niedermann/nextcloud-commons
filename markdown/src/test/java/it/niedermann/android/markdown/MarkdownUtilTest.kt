package it.niedermann.android.markdown

import android.graphics.Color
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import it.niedermann.android.markdown.model.EListType
import it.niedermann.android.markdown.model.SearchSpan
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.lang.reflect.InvocationTargetException

@Suppress("LocalVariableName")
@RunWith(RobolectricTestRunner::class)
class MarkdownUtilTest : TestCase() {
    @Test
    fun getStartOfLine() {
        //language=md
        val test = StringBuilder(
            """
              # Test-Note
              
              - [ ] this is a test note
              - [x] test
              [test](https://example.com)
              
              
              
              """.trimIndent() // line start 78
        )
        test.indices.forEach { i ->
            val startOfLine = MarkdownUtil.getStartOfLine(test, i)
            when {
                i <= 11 -> assertEquals(0, startOfLine)
                i <= 12 -> assertEquals(12, startOfLine)
                i <= 38 -> assertEquals(13, startOfLine)
                i <= 49 -> assertEquals(39, startOfLine)
                i <= 77 -> assertEquals(50, startOfLine)
                i <= 78 -> assertEquals(78, startOfLine)
                i <= 79 -> assertEquals(79, startOfLine)
            }
        }
    }

    @Test
    fun getEndOfLine() {
        //language=md
        val test = """
            # Test-Note
            
            - [ ] this is a test note
            - [x] test
            [test](https://example.com)
            
            
            
            """.trimIndent() // line 78 - 79
        test.indices.forEach { i ->
            val endOfLine = MarkdownUtil.getEndOfLine(test, i)
            when {
                i <= 11 -> assertEquals(11, endOfLine)
                i <= 12 -> assertEquals(12, endOfLine)
                i <= 38 -> assertEquals(38, endOfLine)
                i <= 49 -> assertEquals(49, endOfLine)
                i <= 77 -> assertEquals(77, endOfLine)
                i <= 78 -> assertEquals(78, endOfLine)
                i <= 79 -> assertEquals(79, endOfLine)
            }
        }
        assertEquals(3, MarkdownUtil.getEndOfLine(" - ", 2))
    }

    @Test
    fun getMarkdownLink() {
        assertEquals("[Foo](https://bar)", MarkdownUtil.getMarkdownLink("Foo", "https://bar"))
    }

    @Test
    fun lineStartsWithCheckbox() {
        linkedMapOf(
            Pair("  - [ ] a", true),
            Pair("  - [x] a", true),
            Pair("  - [X] a", true),
            Pair("  * [ ] a", true),
            Pair("  * [x] a", true),
            Pair("  * [X] a", true),
            Pair("  + [ ] a", true),
            Pair("  + [x] a", true),
            Pair("  + [X] a", true),
            Pair("- [ ] a", true),
            Pair("- [x] a", true),
            Pair("- [X] a", true),
            Pair("* [ ] a", true),
            Pair("* [x] a", true),
            Pair("* [X] a", true),
            Pair("+ [ ] a", true),
            Pair("+ [x] a", true),
            Pair("+ [X] a", true),
            Pair("  - [ ] ", true),
            Pair("  - [x] ", true),
            Pair("  - [X] ", true),
            Pair("  * [ ] ", true),
            Pair("  * [x] ", true),
            Pair("  * [X] ", true),
            Pair("  + [ ] ", true),
            Pair("  + [x] ", true),
            Pair("  + [X] ", true),
            Pair("  - [ ]", true),
            Pair("  - [x]", true),
            Pair("  - [X]", true),
            Pair("  * [ ]", true),
            Pair("  * [x]", true),
            Pair("  * [X]", true),
            Pair("  + [ ]", true),
            Pair("  + [x]", true),
            Pair("  + [X]", true),
            Pair("- [ ] ", true),
            Pair("- [x] ", true),
            Pair("- [X] ", true),
            Pair("* [ ] ", true),
            Pair("* [x] ", true),
            Pair("* [X] ", true),
            Pair("+ [ ] ", true),
            Pair("+ [x] ", true),
            Pair("+ [X] ", true),
            Pair("- [ ]", true),
            Pair("- [x]", true),
            Pair("- [X]", true),
            Pair("* [ ]", true),
            Pair("* [x]", true),
            Pair("* [X]", true),
            Pair("+ [ ]", true),
            Pair("+ [x]", true),
            Pair("+ [X]", true),
            Pair("-[ ] ", false),
            Pair("-[x] ", false),
            Pair("-[X] ", false),
            Pair("*[ ] ", false),
            Pair("*[x] ", false),
            Pair("*[X] ", false),
            Pair("+[ ] ", false),
            Pair("+[x] ", false),
            Pair("+[X] ", false),
            Pair("-[ ]", false),
            Pair("-[x]", false),
            Pair("-[X]", false),
            Pair("*[ ]", false),
            Pair("*[x]", false),
            Pair("*[X]", false),
            Pair("+[ ]", false),
            Pair("+[x]", false),
            Pair("+[X]", false),
            Pair("- [] ", false),
            Pair("* [] ", false),
            Pair("+ [] ", false),
            Pair("- []", false),
            Pair("* []", false),
            Pair("+ []", false),
            Pair("-[] ", false),
            Pair("*[] ", false),
            Pair("+[] ", false),
            Pair("-[]", false),
            Pair("*[]", false),
            Pair("+[]", false)
        ).forEach { (key: String, value: Boolean) -> assertEquals(value, MarkdownUtil.lineStartsWithCheckbox(key)) }
    }

    @Test
    fun togglePunctuation() {
        var builder: Editable

        // Add italic
        builder = SpannableStringBuilder("Lorem ipsum dolor sit amet.")
        assertEquals(12, MarkdownUtil.togglePunctuation(builder, 6, 11, "*"))
        assertEquals("Lorem *ipsum* dolor sit amet.", builder.toString())

        // Remove italic
        builder = SpannableStringBuilder("Lorem *ipsum* dolor sit amet.")
        assertEquals(11, MarkdownUtil.togglePunctuation(builder, 7, 12, "*"))
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString())

        // Add bold
        builder = SpannableStringBuilder("Lorem ipsum dolor sit amet.")
        assertEquals(13, MarkdownUtil.togglePunctuation(builder, 6, 11, "**"))
        assertEquals("Lorem **ipsum** dolor sit amet.", builder.toString())

        // Remove bold
        builder = SpannableStringBuilder("Lorem **ipsum** dolor sit amet.")
        assertEquals(11, MarkdownUtil.togglePunctuation(builder, 8, 13, "**"))
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString())

        // Add strike
        builder = SpannableStringBuilder("Lorem ipsum dolor sit amet.")
        assertEquals(13, MarkdownUtil.togglePunctuation(builder, 6, 11, "~~"))
        assertEquals("Lorem ~~ipsum~~ dolor sit amet.", builder.toString())

        // Remove strike
        builder = SpannableStringBuilder("Lorem ~~ipsum~~ dolor sit amet.")
        assertEquals(11, MarkdownUtil.togglePunctuation(builder, 8, 13, "~~"))
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString())

        // Add italic at first position
        builder = SpannableStringBuilder("Lorem ipsum dolor sit amet.")
        assertEquals(6, MarkdownUtil.togglePunctuation(builder, 0, 5, "*"))
        assertEquals("*Lorem* ipsum dolor sit amet.", builder.toString())

        // Remove italic from first position
        builder = SpannableStringBuilder("*Lorem* ipsum dolor sit amet.")
        assertEquals(5, MarkdownUtil.togglePunctuation(builder, 1, 6, "*"))
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString())

        // Add italic at last position
        builder = SpannableStringBuilder("Lorem ipsum dolor sit amet.")
        assertEquals(28, MarkdownUtil.togglePunctuation(builder, 22, 27, "*"))
        assertEquals("Lorem ipsum dolor sit *amet.*", builder.toString())

        // Remove italic from last position
        builder = SpannableStringBuilder("Lorem ipsum dolor sit *amet.*")
        assertEquals(27, MarkdownUtil.togglePunctuation(builder, 23, 28, "*"))
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString())

        // Text is not directly surrounded by punctuation but contains it

        // Do nothing when the same punctuation is contained only one time
        builder = SpannableStringBuilder("Lorem *ipsum dolor sit amet.")
        assertEquals(28, MarkdownUtil.togglePunctuation(builder, 0, 28, "*"))
        assertEquals("Lorem *ipsum dolor sit amet.", builder.toString())

        // Do nothing when the same punctuation is contained only one time
        builder = SpannableStringBuilder("Lorem **ipsum dolor sit amet.")
        assertEquals(29, MarkdownUtil.togglePunctuation(builder, 0, 29, "**"))
        assertEquals("Lorem **ipsum dolor sit amet.", builder.toString())

        // Remove containing punctuation
        builder = SpannableStringBuilder("Lorem *ipsum* dolor sit amet.")
        assertEquals(11, MarkdownUtil.togglePunctuation(builder, 6, 13, "*"))
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString())

        // Remove containing punctuation
        builder = SpannableStringBuilder("Lorem *ipsum* dolor sit amet.")
        assertEquals(27, MarkdownUtil.togglePunctuation(builder, 0, 29, "*"))
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString())

        // Remove multiple containing punctuations
        builder = SpannableStringBuilder("Lorem *ipsum* dolor *sit* amet.")
        assertEquals(27, MarkdownUtil.togglePunctuation(builder, 0, 31, "*"))
        assertEquals("Lorem ipsum dolor sit amet.", builder.toString())

        // Toggle italic on bold text
        builder = SpannableStringBuilder("Lorem **ipsum** dolor sit amet.")
        assertEquals(14, MarkdownUtil.togglePunctuation(builder, 8, 13, "*"))
        assertEquals("Lorem ***ipsum*** dolor sit amet.", builder.toString())

        // Toggle italic on bold text
        builder = SpannableStringBuilder("Lorem **ipsum** dolor sit amet.")
        assertEquals(16, MarkdownUtil.togglePunctuation(builder, 6, 15, "*"))
        assertEquals("Lorem ***ipsum*** dolor sit amet.", builder.toString())

        // Toggle bold on italic text
        builder = SpannableStringBuilder("Lorem *ipsum* dolor sit amet.")
        assertEquals(14, MarkdownUtil.togglePunctuation(builder, 7, 12, "**"))
        assertEquals("Lorem ***ipsum*** dolor sit amet.", builder.toString())

        // Toggle bold to italic
        builder = SpannableStringBuilder("Lorem **ipsum** dolor sit amet.")
        assertEquals(32, MarkdownUtil.togglePunctuation(builder, 0, 31, "*"))
        assertEquals("*Lorem **ipsum** dolor sit amet.*", builder.toString())

        // Toggle italic and bold to bold
        builder = SpannableStringBuilder("Lorem ***ipsum*** dolor sit amet.")
        assertEquals(13, MarkdownUtil.togglePunctuation(builder, 0, 14, "*"))
        assertEquals("Lorem **ipsum** dolor sit amet.", builder.toString())

        // toggle italic around multiple existing bolds
        builder = SpannableStringBuilder("Lorem **ipsum** dolor **sit** amet.")
        assertEquals(35, MarkdownUtil.togglePunctuation(builder, 0, 34, "*"))
        assertEquals("*Lorem **ipsum** dolor **sit** amet*.", builder.toString())

        // Toggle italic and bold to italic
        builder = SpannableStringBuilder("Lorem ***ipsum*** dolor sit amet.")
        assertEquals(12, MarkdownUtil.togglePunctuation(builder, 9, 14, "**"))
        assertEquals("Lorem *ipsum* dolor sit amet.", builder.toString())

        // Toggle multiple italic and bold to bold
        builder = SpannableStringBuilder("Lorem ***ipsum*** dolor ***sit*** amet.")
        assertEquals(34, MarkdownUtil.togglePunctuation(builder, 0, 38, "*"))
        assertEquals("Lorem **ipsum** dolor **sit** amet.", builder.toString())

        // Toggle multiple italic and bold to italic
        builder = SpannableStringBuilder("Lorem ***ipsum*** dolor ***sit*** amet.")
        assertEquals(30, MarkdownUtil.togglePunctuation(builder, 0, 38, "**"))
        assertEquals("Lorem *ipsum* dolor *sit* amet.", builder.toString())

        // Toggle italic on an empty text
        builder = SpannableStringBuilder("")
        assertEquals(1, MarkdownUtil.togglePunctuation(builder, 0, 0, "*"))
        assertEquals("**", builder.toString())

        // Toggle italic on a blank selection
        builder = SpannableStringBuilder(" ")
        assertEquals(2, MarkdownUtil.togglePunctuation(builder, 0, 1, "*"))
        assertEquals("* *", builder.toString())

        // Toggle italic on a partial blank selection
        builder = SpannableStringBuilder("   ")
        assertEquals(3, MarkdownUtil.togglePunctuation(builder, 1, 2, "*"))
        assertEquals(" * * ", builder.toString())

        // Toggle bold on an empty text
        builder = SpannableStringBuilder("")
        assertEquals(2, MarkdownUtil.togglePunctuation(builder, 0, 0, "**"))
        assertEquals("****", builder.toString())

        // Toggle bold on a blank selection
        builder = SpannableStringBuilder(" ")
        assertEquals(3, MarkdownUtil.togglePunctuation(builder, 0, 1, "**"))
        assertEquals("** **", builder.toString())

        // Toggle bold on a partial blank selection
        builder = SpannableStringBuilder("   ")
        assertEquals(4, MarkdownUtil.togglePunctuation(builder, 1, 2, "**"))
        assertEquals(" ** ** ", builder.toString())

        // Toggle italic right after bold
        builder = SpannableStringBuilder("**Bold**Italic")
        assertEquals(15, MarkdownUtil.togglePunctuation(builder, 8, 14, "*"))
        assertEquals("**Bold***Italic*", builder.toString())

        // Toggle italic for last of many bolds in one line
        builder = SpannableStringBuilder("Lorem **Ipsum** **Dolor**")
        assertEquals(24, MarkdownUtil.togglePunctuation(builder, 18, 23, "*"))
        assertEquals("Lorem **Ipsum** ***Dolor***", builder.toString())
        builder = SpannableStringBuilder("Lorem **Ipsum** **Dolor**")
        assertEquals(14, MarkdownUtil.togglePunctuation(builder, 8, 13, "*"))
        assertEquals("Lorem ***Ipsum*** **Dolor**", builder.toString())
        builder = SpannableStringBuilder("Lorem **Ipsum** **Dolor**")
        assertEquals(16, MarkdownUtil.togglePunctuation(builder, 6, 15, "*"))
        assertEquals("Lorem ***Ipsum*** **Dolor**", builder.toString())

        // Toggle italic for last bold + italic in a row of multiple marked elements
        builder = SpannableStringBuilder("Lorem **Ipsum** ***Dolor***")
        assertEquals(23, MarkdownUtil.togglePunctuation(builder, 19, 24, "*"))
        assertEquals("Lorem **Ipsum** **Dolor**", builder.toString())
        builder = SpannableStringBuilder("Lorem ***Ipsum*** **Dolor**")
        assertEquals(13, MarkdownUtil.togglePunctuation(builder, 9, 14, "*"))
        assertEquals("Lorem **Ipsum** **Dolor**", builder.toString())
        builder = SpannableStringBuilder("Lorem ***Ipsum*** **Dolor**")
        assertEquals(15, MarkdownUtil.togglePunctuation(builder, 6, 17, "*"))
        assertEquals("Lorem **Ipsum** **Dolor**", builder.toString())
        builder = SpannableStringBuilder("Lorem ***Ipsum*** **Dolor**")
        assertEquals(15, MarkdownUtil.togglePunctuation(builder, 7, 16, "*"))
        assertEquals("Lorem **Ipsum** **Dolor**", builder.toString())
        builder = SpannableStringBuilder("Lorem ***Ipsum*** **Dolor**")
        assertEquals(15, MarkdownUtil.togglePunctuation(builder, 7, 17, "*"))
        assertEquals("Lorem **Ipsum** **Dolor**", builder.toString())
        builder = SpannableStringBuilder("Lorem ***Ipsum*** **Dolor**")
        assertEquals(15, MarkdownUtil.togglePunctuation(builder, 8, 16, "*"))
        assertEquals("Lorem **Ipsum** **Dolor**", builder.toString())

        // Multiline
        builder = SpannableStringBuilder("Lorem ***Ipsum***\n **Dolor**")
        assertEquals(29, MarkdownUtil.togglePunctuation(builder, 0, 28, "*"))
        assertEquals("*Lorem ***Ipsum***\n **Dolor***", builder.toString())
        builder = SpannableStringBuilder("**Bold**\nItalic")
        assertEquals(16, MarkdownUtil.togglePunctuation(builder, 9, 15, "*"))
        assertEquals("**Bold**\n*Italic*", builder.toString())
        builder = SpannableStringBuilder("Bold\n*Italic*")
        assertEquals(6, MarkdownUtil.togglePunctuation(builder, 0, 4, "**"))
        assertEquals("**Bold**\n*Italic*", builder.toString())
        builder = SpannableStringBuilder("*Italic*\nBold")
        assertEquals(15, MarkdownUtil.togglePunctuation(builder, 9, 13, "**"))
        assertEquals("*Italic*\n**Bold**", builder.toString())
        builder = SpannableStringBuilder("Italic\n**Bold**")
        assertEquals(7, MarkdownUtil.togglePunctuation(builder, 0, 6, "*"))
        assertEquals("*Italic*\n**Bold**", builder.toString())
    }

    @Test
    fun insertLink() {
        var builder: Editable

        // Add link without clipboardUrl to normal text
        builder = SpannableStringBuilder("Lorem ipsum dolor sit amet.")
        assertEquals(14, MarkdownUtil.insertLink(builder, 6, 11, null))
        assertEquals("Lorem [ipsum]() dolor sit amet.", builder.toString())

        // Add link without clipboardUrl to url
        builder = SpannableStringBuilder("Lorem https://example.com dolor sit amet.")
        assertEquals(7, MarkdownUtil.insertLink(builder, 6, 25, null))
        assertEquals("Lorem [](https://example.com) dolor sit amet.", builder.toString())

        // Add link without clipboardUrl to empty selection before space character
        builder = SpannableStringBuilder("Lorem ipsum dolor sit amet.")
        assertEquals(13, MarkdownUtil.insertLink(builder, 11, 11, null))
        assertEquals("Lorem ipsum []() dolor sit amet.", builder.toString())

        // Add link without clipboardUrl to empty selection after space character
        builder = SpannableStringBuilder("Lorem ipsum dolor sit amet.")
        assertEquals(13, MarkdownUtil.insertLink(builder, 12, 12, null))
        assertEquals("Lorem ipsum []() dolor sit amet.", builder.toString())

        // Add link without clipboardUrl to empty selection in word
        builder = SpannableStringBuilder("Lorem ipsum dolor sit amet.")
        assertEquals(20, MarkdownUtil.insertLink(builder, 14, 14, null))
        assertEquals("Lorem ipsum [dolor]() sit amet.", builder.toString())

        // Add link with clipboardUrl to normal text
        builder = SpannableStringBuilder("Lorem ipsum dolor sit amet.")
        assertEquals(33, MarkdownUtil.insertLink(builder, 6, 11, "https://example.com"))
        assertEquals("Lorem [ipsum](https://example.com) dolor sit amet.", builder.toString())

        // Add link with clipboardUrl to url
        builder = SpannableStringBuilder("Lorem https://example.com dolor sit amet.")
        assertEquals(46, MarkdownUtil.insertLink(builder, 6, 25, "https://example.de"))
        assertEquals("Lorem [https://example.com](https://example.de) dolor sit amet.", builder.toString())

        // Add link with clipboardUrl to empty selection before space character
        builder = SpannableStringBuilder("Lorem ipsum dolor sit amet.")
        assertEquals(13, MarkdownUtil.insertLink(builder, 11, 11, "https://example.de"))
        assertEquals("Lorem ipsum [](https://example.de) dolor sit amet.", builder.toString())

        // Add link with clipboardUrl to empty selection after space character
        builder = SpannableStringBuilder("Lorem ipsum dolor sit amet.")
        assertEquals(13, MarkdownUtil.insertLink(builder, 12, 12, "https://example.de"))
        assertEquals("Lorem ipsum [](https://example.de) dolor sit amet.", builder.toString())

        // Add link with clipboardUrl to empty selection in word
        builder = SpannableStringBuilder("Lorem ipsum dolor sit amet.")
        assertEquals(38, MarkdownUtil.insertLink(builder, 14, 14, "https://example.de"))
        assertEquals("Lorem ipsum [dolor](https://example.de) sit amet.", builder.toString())

        // Add link without clipboardUrl to empty selection on empty text
        builder = SpannableStringBuilder("")
        assertEquals(1, MarkdownUtil.insertLink(builder, 0, 0, null))
        assertEquals("[]()", builder.toString())

        // Add link without clipboardUrl to empty selection on only space text
        builder = SpannableStringBuilder(" ")
        assertEquals(1, MarkdownUtil.insertLink(builder, 0, 0, null))
        assertEquals("[]() ", builder.toString())

        // Add link without clipboardUrl to empty selection on only space text
        builder = SpannableStringBuilder(" ")
        assertEquals(4, MarkdownUtil.insertLink(builder, 0, 1, null))
        assertEquals("[ ]()", builder.toString())

        // Add link without clipboardUrl to empty selection on only space text
        builder = SpannableStringBuilder(" ")
        assertEquals(2, MarkdownUtil.insertLink(builder, 1, 1, null))
        assertEquals(" []()", builder.toString())

        // Add link without clipboardUrl to empty selection on only spaces
        builder = SpannableStringBuilder("  ")
        assertEquals(2, MarkdownUtil.insertLink(builder, 1, 1, null))
        assertEquals(" []() ", builder.toString())

        // Add link without clipboardUrl to empty selection in word with trailing and leading spaces
        builder = SpannableStringBuilder("  Lorem  ")
        assertEquals(10, MarkdownUtil.insertLink(builder, 5, 5, null))
        assertEquals("  [Lorem]()  ", builder.toString())

        // Add link with clipboardUrl to empty selection on empty text
        builder = SpannableStringBuilder("")
        assertEquals(1, MarkdownUtil.insertLink(builder, 0, 0, "https://www.example.com"))
        assertEquals("[](https://www.example.com)", builder.toString())

        // Add link with clipboardUrl to empty selection on only space text
        builder = SpannableStringBuilder(" ")
        assertEquals(1, MarkdownUtil.insertLink(builder, 0, 0, "https://www.example.com"))
        assertEquals("[](https://www.example.com) ", builder.toString())

        // Add link with clipboardUrl to empty selection on only space text
        builder = SpannableStringBuilder(" ")
        assertEquals(27, MarkdownUtil.insertLink(builder, 0, 1, "https://www.example.com"))
        assertEquals("[ ](https://www.example.com)", builder.toString())

        // Add link with clipboardUrl to empty selection on only space text
        builder = SpannableStringBuilder(" ")
        assertEquals(2, MarkdownUtil.insertLink(builder, 1, 1, "https://www.example.com"))
        assertEquals(" [](https://www.example.com)", builder.toString())

        // Add link with clipboardUrl to empty selection on one character
        builder = SpannableStringBuilder("a")
        assertEquals(1, MarkdownUtil.insertLink(builder, 0, 0, "https://www.example.com"))
        assertEquals("[](https://www.example.com)a", builder.toString())

        // Add link with clipboardUrl to empty selection on one character
        builder = SpannableStringBuilder("a")
        assertEquals(27, MarkdownUtil.insertLink(builder, 0, 1, "https://www.example.com"))
        assertEquals("[a](https://www.example.com)", builder.toString())

        // Add link with clipboardUrl to empty selection on one character
        builder = SpannableStringBuilder("a")
        assertEquals(2, MarkdownUtil.insertLink(builder, 1, 1, "https://www.example.com"))
        assertEquals("a[](https://www.example.com)", builder.toString())

        // Add link with clipboardUrl to empty selection on only spaces
        builder = SpannableStringBuilder("  ")
        assertEquals(2, MarkdownUtil.insertLink(builder, 1, 1, "https://www.example.com"))
        assertEquals(" [](https://www.example.com) ", builder.toString())

        // Add link with clipboardUrl to empty selection in word with trailing and leading spaces
        builder = SpannableStringBuilder("  Lorem  ")
        assertEquals(33, MarkdownUtil.insertLink(builder, 5, 5, "https://www.example.com"))
        assertEquals("  [Lorem](https://www.example.com)  ", builder.toString())

        // Add link with clipboardUrl to selection in word with trailing and leading spaces
        builder = SpannableStringBuilder("  Lorem  ")
        assertEquals(33, MarkdownUtil.insertLink(builder, 2, 7, "https://www.example.com"))
        assertEquals("  [Lorem](https://www.example.com)  ", builder.toString())
    }

    @Test
    fun selectionIsInLink() {
        try {
            val method = MarkdownUtil::class.java.getDeclaredMethod("selectionIsInLink", CharSequence::class.java, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
            method.isAccessible = true
            assertTrue((method.invoke(null, "Lorem [ipsum](https://example.com) dolor sit amet.", 7, 12) as Boolean))
            assertTrue((method.invoke(null, "Lorem [ipsum](https://example.com) dolor sit amet.", 6, 34) as Boolean))
            assertTrue((method.invoke(null, "Lorem [ipsum](https://example.com) dolor sit amet.", 14, 33) as Boolean))
            assertTrue((method.invoke(null, "Lorem [ipsum](https://example.com) dolor sit amet.", 12, 14) as Boolean))
            assertTrue((method.invoke(null, "Lorem [ipsum](https://example.com) dolor sit amet.", 0, 7) as Boolean))
            assertTrue((method.invoke(null, "Lorem [ipsum](https://example.com) dolor sit amet.", 33, 34) as Boolean))
            assertTrue((method.invoke(null, "Lorem [](https://example.com) dolor sit amet.", 6, 28) as Boolean))
            assertTrue((method.invoke(null, "Lorem [](https://example.com) dolor sit amet.", 7, 28) as Boolean))
            assertTrue((method.invoke(null, "Lorem [](https://example.com) dolor sit amet.", 8, 28) as Boolean))
            assertTrue((method.invoke(null, "Lorem [](https://example.com) dolor sit amet.", 9, 28) as Boolean))
            assertTrue((method.invoke(null, "Lorem [](https://example.com) dolor sit amet.", 6, 29) as Boolean))
            assertTrue((method.invoke(null, "Lorem [](https://example.com) dolor sit amet.", 7, 29) as Boolean))
            assertTrue((method.invoke(null, "Lorem [](https://example.com) dolor sit amet.", 8, 29) as Boolean))
            assertTrue((method.invoke(null, "Lorem [](https://example.com) dolor sit amet.", 9, 29) as Boolean))
            assertTrue((method.invoke(null, "Lorem [ipsum]() dolor sit amet.", 6, 12) as Boolean))
            assertTrue((method.invoke(null, "Lorem [ipsum]() dolor sit amet.", 6, 13) as Boolean))
            assertTrue((method.invoke(null, "Lorem [ipsum]() dolor sit amet.", 6, 14) as Boolean))
            assertTrue((method.invoke(null, "Lorem [ipsum]() dolor sit amet.", 6, 15) as Boolean))
            assertTrue((method.invoke(null, "Lorem [ipsum]() dolor sit amet.", 7, 12) as Boolean))
            assertTrue((method.invoke(null, "Lorem [ipsum]() dolor sit amet.", 7, 13) as Boolean))
            assertTrue((method.invoke(null, "Lorem [ipsum]() dolor sit amet.", 7, 14) as Boolean))
            assertTrue((method.invoke(null, "Lorem [ipsum]() dolor sit amet.", 7, 15) as Boolean))
            assertFalse((method.invoke(null, "Lorem [ipsum](https://example.com) dolor sit amet.", 0, 6) as Boolean))
            assertFalse((method.invoke(null, "Lorem [ipsum](https://example.com) dolor sit amet.", 34, 50) as Boolean))
            assertFalse((method.invoke(null, "Lorem [ipsum](https://example.com) dolor sit amet.", 41, 44) as Boolean))
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }

    @Test
    fun getListItemIfIsEmpty() {
        assertEquals("- ", MarkdownUtil.getListItemIfIsEmpty("- ").get())
        assertEquals("+ ", MarkdownUtil.getListItemIfIsEmpty("+ ").get())
        assertEquals("* ", MarkdownUtil.getListItemIfIsEmpty("* ").get())
        assertEquals("1. ", MarkdownUtil.getListItemIfIsEmpty("1. ").get())
        assertEquals(" - ", MarkdownUtil.getListItemIfIsEmpty(" - ").get())
        assertEquals(" + ", MarkdownUtil.getListItemIfIsEmpty(" + ").get())
        assertEquals(" * ", MarkdownUtil.getListItemIfIsEmpty(" * ").get())
        assertEquals(" 1. ", MarkdownUtil.getListItemIfIsEmpty(" 1. ").get())
        assertEquals("  - ", MarkdownUtil.getListItemIfIsEmpty("  - ").get())
        assertEquals("  + ", MarkdownUtil.getListItemIfIsEmpty("  + ").get())
        assertEquals("  * ", MarkdownUtil.getListItemIfIsEmpty("  * ").get())
        assertEquals("  1. ", MarkdownUtil.getListItemIfIsEmpty("  1. ").get())
        assertFalse(MarkdownUtil.getListItemIfIsEmpty("- Test").isPresent)
        assertFalse(MarkdownUtil.getListItemIfIsEmpty("+ Test").isPresent)
        assertFalse(MarkdownUtil.getListItemIfIsEmpty("* Test").isPresent)
        assertFalse(MarkdownUtil.getListItemIfIsEmpty("1. s").isPresent)
        assertFalse(MarkdownUtil.getListItemIfIsEmpty("1.  ").isPresent)
        assertFalse(MarkdownUtil.getListItemIfIsEmpty(" - Test").isPresent)
        assertFalse(MarkdownUtil.getListItemIfIsEmpty(" + Test").isPresent)
        assertFalse(MarkdownUtil.getListItemIfIsEmpty(" * Test").isPresent)
        assertFalse(MarkdownUtil.getListItemIfIsEmpty(" 1. s").isPresent)
        assertFalse(MarkdownUtil.getListItemIfIsEmpty(" 1.  ").isPresent)
        assertFalse(MarkdownUtil.getListItemIfIsEmpty("  - Test").isPresent)
        assertFalse(MarkdownUtil.getListItemIfIsEmpty("  + Test").isPresent)
        assertFalse(MarkdownUtil.getListItemIfIsEmpty("  * Test").isPresent)
        assertFalse(MarkdownUtil.getListItemIfIsEmpty("  1. s").isPresent)
        assertFalse(MarkdownUtil.getListItemIfIsEmpty("  1.  ").isPresent)
    }

    @Test
    fun lineStartsWithOrderedList() {
        assertEquals(1, MarkdownUtil.getOrderedListNumber("1. Test").get())
        assertEquals(2, MarkdownUtil.getOrderedListNumber("2. Test").get())
        assertEquals(3, MarkdownUtil.getOrderedListNumber("3. Test").get())
        assertEquals(10, MarkdownUtil.getOrderedListNumber("10. Test").get())
        assertEquals(11, MarkdownUtil.getOrderedListNumber("11. Test").get())
        assertEquals(12, MarkdownUtil.getOrderedListNumber("12. Test").get())
        assertEquals(1, MarkdownUtil.getOrderedListNumber("1. 1").get())
        assertEquals(1, MarkdownUtil.getOrderedListNumber("1. Test 1").get())
        assertFalse(MarkdownUtil.getOrderedListNumber("").isPresent)
        assertFalse(MarkdownUtil.getOrderedListNumber("1.").isPresent)
        assertFalse(MarkdownUtil.getOrderedListNumber("1. ").isPresent)
        assertFalse(MarkdownUtil.getOrderedListNumber("11. ").isPresent)
        assertFalse(MarkdownUtil.getOrderedListNumber("-1. Test").isPresent)
        assertFalse(MarkdownUtil.getOrderedListNumber(" 1. Test").isPresent)
        assertFalse(MarkdownUtil.getOrderedListNumber("123 Test").isPresent)
        assertFalse(MarkdownUtil.getOrderedListNumber("123a Test").isPresent)
        assertFalse(MarkdownUtil.getOrderedListNumber("123").isPresent)
        assertFalse(MarkdownUtil.getOrderedListNumber("123a").isPresent)
        assertFalse(MarkdownUtil.getOrderedListNumber("123 ").isPresent)
        assertFalse(MarkdownUtil.getOrderedListNumber("123a ").isPresent)
    }

    @Test
    fun setCheckboxStatus() {
        for (listType in EListType.values()) {
            val origin_1 = listType.checkboxUnchecked + " Item"
            val expected_1 = listType.checkboxChecked + " Item"
            assertEquals(expected_1, MarkdownUtil.setCheckboxStatus(origin_1, 0, true))
            val origin_2 = listType.checkboxChecked + " Item"
            val expected_2 = listType.checkboxChecked + " Item"
            assertEquals(expected_2, MarkdownUtil.setCheckboxStatus(origin_2, 0, true))
            val origin_3 = listType.checkboxChecked + " Item"
            val expected_3 = listType.checkboxChecked + " Item"
            assertEquals(expected_3, MarkdownUtil.setCheckboxStatus(origin_3, -1, true))
            val origin_4 = listType.checkboxChecked + " Item"
            val expected_4 = listType.checkboxChecked + " Item"
            assertEquals(expected_4, MarkdownUtil.setCheckboxStatus(origin_4, 3, true))
            val origin_5 = """${listType.checkboxChecked} Item
                ${listType.checkboxChecked} Item""".trimIndent()
            val expected_5 = """${listType.checkboxChecked} Item
                ${listType.checkboxUnchecked} Item""".trimIndent()
            assertEquals(expected_5, MarkdownUtil.setCheckboxStatus(origin_5, 1, false))

            // Checkboxes in fenced code block aren't rendered by Markwon and therefore don't count to the checkbox index
            val origin_6 = """
                ${listType.checkboxChecked} Item
                ```
                ${listType.checkboxUnchecked} Item
                ```
                ${listType.checkboxUnchecked} Item
            """.trimIndent()
            val expected_6 = """
                ${listType.checkboxChecked} Item
                ```
                ${listType.checkboxUnchecked} Item
                ```
                ${listType.checkboxChecked} Item
            """.trimIndent()
            assertEquals(expected_6, MarkdownUtil.setCheckboxStatus(origin_6, 1, true))

            // Checkbox in partial nested fenced code block does not count as rendered checkbox
            val origin_7 = """
                ${listType.checkboxChecked} Item
                ````
                ```
                ${listType.checkboxUnchecked} Item
                ````
                ${listType.checkboxUnchecked} Item
            """.trimIndent()
            val expected_7 = """
                ${listType.checkboxChecked} Item
                ````
                ```
                ${listType.checkboxUnchecked} Item
                ````
                ${listType.checkboxChecked} Item
            """.trimIndent()
            assertEquals(expected_7, MarkdownUtil.setCheckboxStatus(origin_7, 1, true))

            // Checkbox in complete nested fenced code block does not count as rendered checkbox
            val origin_8 = """
                ${listType.checkboxChecked} Item
                ````
                ```
                ${listType.checkboxUnchecked} Item
                ```
                ````
                ${listType.checkboxUnchecked} Item
            """.trimIndent()
            val expected_8 = """
                ${listType.checkboxChecked} Item
                ````
                ```
                ${listType.checkboxUnchecked} Item
                ```
                ````
                ${listType.checkboxChecked} Item
            """.trimIndent()
            assertEquals(expected_8, MarkdownUtil.setCheckboxStatus(origin_8, 1, true))

            // If checkbox has no content, it doesn't get rendered by Markwon and therefore can not be checked
            val origin_9 = """
                ${listType.checkboxChecked} Item
                ````
                ```
                ${listType.checkboxUnchecked} Item
                ```
                ````
                ${listType.checkboxUnchecked} 
                ${listType.checkboxUnchecked} Item
            """.trimIndent()
            val expected_9 = """
                ${listType.checkboxChecked} Item
                ````
                ```
                ${listType.checkboxUnchecked} Item
                ```
                ````
                ${listType.checkboxUnchecked} 
                ${listType.checkboxChecked} Item
            """.trimIndent()
            assertEquals(expected_9, MarkdownUtil.setCheckboxStatus(origin_9, 1, true))
            val origin_10 = """${listType.checkboxChecked} Item
                ${listType.checkboxCheckedUpperCase} Item""".trimIndent()
            val expected_10 = """${listType.checkboxChecked} Item
                ${listType.checkboxUnchecked} Item""".trimIndent()
            assertEquals(expected_10, MarkdownUtil.setCheckboxStatus(origin_10, 1, false))
        }
    }

    @Test
    fun removeSpans() {
        try {
            val removeSpans = MarkdownUtil::class.java.getDeclaredMethod("removeSpans", Spannable::class.java, Class::class.java)
            removeSpans.isAccessible = true
            val editable_1 = SpannableStringBuilder("Lorem Ipsum dolor sit amet")
            editable_1.setSpan(SearchSpan(Color.RED, Color.GRAY, false, false), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            editable_1.setSpan(ForegroundColorSpan(Color.BLUE), 6, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            editable_1.setSpan(SearchSpan(Color.BLUE, Color.GREEN, true, false), 12, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            removeSpans.invoke(null, editable_1, SearchSpan::class.java)
            assertEquals(0, editable_1.getSpans(0, editable_1.length, SearchSpan::class.java).size)
            assertEquals(1, editable_1.getSpans(0, editable_1.length, ForegroundColorSpan::class.java).size)
            val editable_2 = SpannableStringBuilder("Lorem Ipsum dolor sit amet")
            editable_2.setSpan(SearchSpan(Color.GRAY, Color.RED, false, true), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            editable_2.setSpan(ForegroundColorSpan(Color.BLUE), 2, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            editable_2.setSpan(SearchSpan(Color.BLUE, Color.GREEN, true, false), 3, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            removeSpans.invoke(null, editable_2, SearchSpan::class.java)
            assertEquals(0, editable_2.getSpans(0, editable_2.length, SearchSpan::class.java).size)
            assertEquals(1, editable_2.getSpans(0, editable_2.length, ForegroundColorSpan::class.java).size)
            assertEquals(2, editable_2.getSpanStart(editable_2.getSpans(0, editable_2.length, ForegroundColorSpan::class.java)[0]))
            assertEquals(7, editable_2.getSpanEnd(editable_2.getSpans(0, editable_2.length, ForegroundColorSpan::class.java)[0]))
            val editable_3 = SpannableStringBuilder("Lorem Ipsum dolor sit amet")
            editable_3.setSpan(ForegroundColorSpan(Color.BLUE), 2, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            removeSpans.invoke(null, editable_3, SearchSpan::class.java)
            assertEquals(0, editable_3.getSpans(0, editable_3.length, SearchSpan::class.java).size)
            assertEquals(1, editable_3.getSpans(0, editable_3.length, ForegroundColorSpan::class.java).size)
            assertEquals(2, editable_3.getSpanStart(editable_3.getSpans(0, editable_3.length, ForegroundColorSpan::class.java)[0]))
            assertEquals(7, editable_3.getSpanEnd(editable_3.getSpans(0, editable_3.length, ForegroundColorSpan::class.java)[0]))
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }

    @Test
    fun removeMarkdown() {
        assertEquals("Test", MarkdownUtil.removeMarkdown("Test"))
        assertEquals("Foo\nBar", MarkdownUtil.removeMarkdown("Foo\nBar"))
        assertEquals("Foo\nBar", MarkdownUtil.removeMarkdown("Foo\n  Bar"))
        assertEquals("Foo\nBar", MarkdownUtil.removeMarkdown("Foo   \nBar"))
        assertEquals("Foo-Bar", MarkdownUtil.removeMarkdown("Foo-Bar"))
        assertEquals("Foo*Bar", MarkdownUtil.removeMarkdown("Foo*Bar"))
        assertEquals("Foo/Bar", MarkdownUtil.removeMarkdown("Foo/Bar"))
        assertEquals("FooTestBar", MarkdownUtil.removeMarkdown("Foo*Test*Bar"))
        assertEquals("FooTestBar", MarkdownUtil.removeMarkdown("Foo**Test**Bar"))
        assertEquals("FooTestBar", MarkdownUtil.removeMarkdown("Foo***Test***Bar"))
        assertEquals("Foo*Test**Bar", MarkdownUtil.removeMarkdown("Foo*Test**Bar"))
        assertEquals("Foo*TestBar", MarkdownUtil.removeMarkdown("Foo***Test**Bar"))
        assertEquals("Foo_Test_Bar", MarkdownUtil.removeMarkdown("Foo_Test_Bar"))
        assertEquals("Foo__Test__Bar", MarkdownUtil.removeMarkdown("Foo__Test__Bar"))
        assertEquals("Foo___Test___Bar", MarkdownUtil.removeMarkdown("Foo___Test___Bar"))
        assertEquals("Foo\nHeader\nBar", MarkdownUtil.removeMarkdown("Foo\n# Header\nBar"))
        assertEquals("Foo\nHeader\nBar", MarkdownUtil.removeMarkdown("Foo\n### Header\nBar"))
        assertEquals("Foo\nHeader\nBar", MarkdownUtil.removeMarkdown("Foo\n# Header #\nBar"))
        assertEquals("Foo\nHeader\nBar", MarkdownUtil.removeMarkdown("Foo\n## Header ####\nBar"))
        assertEquals("Foo\nNo Header #\nBar", MarkdownUtil.removeMarkdown("Foo\nNo Header #\nBar"))
        assertEquals("Foo\nHeader\nBar", MarkdownUtil.removeMarkdown("Foo\nHeader\n=\nBar"))
        assertEquals("Foo\nHeader\nBar", MarkdownUtil.removeMarkdown("Foo\nHeader\n-----\nBar"))
        assertEquals("Foo\nHeader\n--=--\nBar", MarkdownUtil.removeMarkdown("Foo\nHeader\n--=--\nBar"))
        assertEquals("Foo\nAufzählung\nBar", MarkdownUtil.removeMarkdown("Foo\n* Aufzählung\nBar"))
        assertEquals("Foo\nAufzählung\nBar", MarkdownUtil.removeMarkdown("Foo\n+ Aufzählung\nBar"))
        assertEquals("Foo\nAufzählung\nBar", MarkdownUtil.removeMarkdown("Foo\n- Aufzählung\nBar"))
        assertEquals("Foo\n- Aufzählung\nBar", MarkdownUtil.removeMarkdown("Foo\n    - Aufzählung\nBar"))
        assertEquals("Foo\nAufzählung *\nBar", MarkdownUtil.removeMarkdown("Foo\n* Aufzählung *\nBar"))
        assertEquals("Title", MarkdownUtil.removeMarkdown("# Title"))
        assertEquals("Aufzählung", MarkdownUtil.removeMarkdown("* Aufzählung"))
        //        assertEquals("Foo Link Bar", MarkdownUtil.removeMarkdown("Foo [Link](https://example.com) Bar"));
        assertFalse(MarkdownUtil.removeMarkdown("- [ ] Test").contains("- [ ]"))
        assertTrue(MarkdownUtil.removeMarkdown("- [ ] Test").endsWith("Test"))
        assertEquals("", MarkdownUtil.removeMarkdown(null))
        assertEquals("", MarkdownUtil.removeMarkdown(""))

        // https://github.com/stefan-niedermann/nextcloud-notes/issues/1104
        assertEquals("2021-03-24 - Example text", MarkdownUtil.removeMarkdown("2021-03-24 - Example text"))
    }
}
