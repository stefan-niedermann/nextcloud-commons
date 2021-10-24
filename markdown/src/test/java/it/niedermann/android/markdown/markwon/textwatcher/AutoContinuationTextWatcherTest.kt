package it.niedermann.android.markdown.markwon.textwatcher

import android.view.KeyEvent
import android.widget.EditText
import androidx.test.core.app.ApplicationProvider
import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor
import it.niedermann.android.markdown.model.EListType
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AutoContinuationTextWatcherTest : TestCase() {
    private lateinit var editText: EditText

    @Before
    fun reset() {
        editText = MarkwonMarkdownEditor(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `should continue simple lists`() {
        for (listType in EListType.values()) {
            editText.setText(listType.listSymbolWithTrailingSpace + "Test")
            pressEnter(6)
            assertText(
                """
                    ${listType.listSymbolWithTrailingSpace}Test
                    ${listType.listSymbolWithTrailingSpace}
                """.trimIndent(), 9)
        }
        for (listType in EListType.values()) {
            editText.setText(listType.checkboxUncheckedWithTrailingSpace + "Test")
            pressEnter(10)
            assertText(
                """
                    ${listType.checkboxUncheckedWithTrailingSpace}Test
                    ${listType.checkboxUncheckedWithTrailingSpace}
                """.trimIndent(), 17)
        }
        for (listType in EListType.values()) {
            editText.setText(listType.checkboxChecked + " Test")
            pressEnter(10)
            assertText(
                """
                    ${listType.checkboxChecked} Test
                    ${listType.checkboxUncheckedWithTrailingSpace}
                """.trimIndent(), 17)
        }
        editText.setText("1. Test")
        pressEnter(7)
        assertText("1. Test\n2. ", 11)
        editText.setText("11. Test")
        pressEnter(8)
        assertText("11. Test\n12. ", 13)
    }

    @Test
    fun `should continue lists with multiple items`() {
        val sample: CharSequence = "- [ ] Foo\n- [x] Bar\n- [ ] Baz\n\nQux"
        editText.setText(sample)
        pressEnter(0)
        assertText("\n- [ ] Foo\n- [x] Bar\n- [ ] Baz\n\nQux", 1)
        editText.setText(sample)
        pressEnter(9)
        assertText("- [ ] Foo\n- [ ] \n- [x] Bar\n- [ ] Baz\n\nQux", 16)
        editText.setText(sample)
        pressEnter(19)
        assertText("- [ ] Foo\n- [x] Bar\n- [ ] \n- [ ] Baz\n\nQux", 26)
    }

    @Test
    fun `should split item if cursor is not on end`() {
        editText.setText("- [ ] Foo\n- [x] Bar")
        pressEnter(8)
        assertText("- [ ] Fo\n- [ ] o\n- [x] Bar", 15)
    }

    @Test
    fun `should continue nested lists at the same indention`() {
        editText.setText("- [ ] Parent\n  - [x] Child\n    - [ ] Third")
        pressEnter(26)
        assertText("- [ ] Parent\n  - [x] Child\n  - [ ] \n    - [ ] Third", 35)
        editText.setText("- [ ] Parent\n  - [x] Child\n    - [ ] Third")
        pressEnter(42)
        assertText("- [ ] Parent\n  - [x] Child\n    - [ ] Third\n    - [ ] ", 53)
        editText.setText("1. Parent\n  1. Child")
        pressEnter(20)
        assertText("1. Parent\n  1. Child\n  2. ", 26)
    }

    @Test
    fun `should remove continued list item when pressing enter multiple times`() {
        editText.setText("- Foo")
        pressEnter(5)
        assertText("- Foo\n- ", 8)
        pressEnter(8)
        assertText("- Foo\n\n", 7)
        pressEnter(7)
        assertText("- Foo\n\n\n", 8)
        editText.setText("- Foo\n  - Bar")
        pressEnter(13)
        assertText("- Foo\n  - Bar\n  - ", 18)
        pressEnter(18)
        assertText("- Foo\n  - Bar\n\n", 15)
        pressEnter(15)
        assertText("- Foo\n  - Bar\n\n\n", 16)
    }

    @Test
    fun `should not continue if no list`() {
        editText.setText("Foo")
        pressEnter(3)
        assertText("Foo\n", 4)
        editText.setText(" Foo")
        pressEnter(4)
        assertText(" Foo\n", 5)
    }

    @Test
    fun `should not continue if blank`() {
        editText.setText("")
        pressEnter(0)
        assertText("\n", 1)
    }

    /**
     * [Issue #66](https://github.com/stefan-niedermann/nextcloud-commons/issues/66)
     */
    @Test
    fun `should end list auto continuation after pressing backspace and enter`() {
        editText.setText("- Foo")
        pressEnter(5)
        assertText("- Foo\n- ", 8)
        editText.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
        assertText("- Foo\n-", 7)
        editText.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        assertText("- Foo\n\n", 7)
    }

    /**
     * [Issue #66](https://github.com/stefan-niedermann/nextcloud-commons/issues/66)
     */
    @Test
    fun `should end checkbox auto continuation after pressing backspace and enter`() {
        editText.setText("- [x] Foo")
        pressEnter(9)
        assertText("- [x] Foo\n- [ ] ", 16)
        editText.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
        assertText("- [x] Foo\n- [ ]", 15)
        editText.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        assertText("- [x] Foo\n\n", 11)
    }

    /**
     * [Issue #66](https://github.com/stefan-niedermann/nextcloud-commons/issues/66)
     */
    @Test
    fun `should end list auto continuation after adding text, remove the space separator of the list item and then press enter`() {
        editText.setText("- Foo")
        pressEnter(5)
        assertText("- Foo\n- ", 8)
        editText.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_A))
        editText.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_B))
        editText.setSelection(8)
        editText.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
        assertText("- Foo\n-ab", 7)
        editText.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        assertText("- Foo\n\nab", 7)
    }

    /**
     * [Issue #66](https://github.com/stefan-niedermann/nextcloud-commons/issues/66)
     */
    @Test
    fun `should end checkbox auto continuation after adding text, remove the space separator of the checkbox and then press enter`() {
        editText.setText("- [x] Foo")
        pressEnter(9)
        assertText("- [x] Foo\n- [ ] ", 16)
        editText.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_A))
        editText.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_B))
        editText.setSelection(16)
        editText.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
        assertText("- [x] Foo\n- [ ]ab", 15)
        editText.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        assertText("- [x] Foo\n\nab", 11)
    }

    private fun assertText(expected: String, cursorPosition: Int) {
        assertEquals(expected, editText.text.toString())
        assertEquals(cursorPosition, editText.selectionStart)
        assertEquals(cursorPosition, editText.selectionEnd)
    }

    private fun pressEnter(atPosition: Int) {
        editText.setSelection(atPosition)
        editText.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
    }
}