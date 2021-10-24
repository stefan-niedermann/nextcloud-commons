package it.niedermann.android.markdown.markwon.textwatcher

import android.view.KeyEvent
import android.widget.EditText
import androidx.test.core.app.ApplicationProvider
import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LowerIndentionTextWatcherTest : TestCase() {
    private lateinit var editText: EditText
    
    @Before
    fun reset() {
        editText = MarkwonMarkdownEditor(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `should lower indention by two when pressing backspace on an indented list`() {
        editText.setText("  - ")
        pressBackspace(4)
        assertText("- ", 2)
        editText.setText("- [ ] Foo\n  - [ ] ")
        pressBackspace(18)
        assertText("- [ ] Foo\n- [ ] ", 16)
    }

    @Test
    fun `should lower indention by one when pressing backspace on a list which is indented by one space`() {
        editText.setText(" - ")
        pressBackspace(3)
        assertText("- ", 2)
    }

    @Test
    fun `should not lower indention by one when cursor is not at the end`() {
        editText.setText(" - ")
        pressBackspace(2)
        assertText("  ", 1)
        editText.setText("  - ")
        pressBackspace(0)
        assertText("  - ", 0)
        editText.setText("  - ")
        pressBackspace(3)
        assertText("   ", 2)
        editText.setText("  - ")
        pressBackspace(2)
        assertText(" - ", 1)
        editText.setText("- Foo\n  - ")
        pressBackspace(9)
        assertText("- Foo\n   ", 8)
    }

    @Test
    fun `should not lower indention if there is any content after the list`() {
        editText.setText("  -  ")
        pressBackspace(4)
        assertText("  - ", 3)
        editText.setText("  -  ")
        pressBackspace(5)
        assertText("  - ", 4)
    }

    @Test
    fun `should not lower indention if backspace was pressed in the next line`() {
        editText.setText("  - \nFoo")
        pressBackspace(5)
        assertText("  - Foo", 4)
    }

    @Test
    fun `should delete last character when pressing backspace`() {
        editText.setText("")
        pressBackspace(0)
        assertText("", 0)
        editText.setText("- [ ] ")
        pressBackspace(6)
        assertText("- [ ]", 5)
        editText.setText("- Foo")
        pressBackspace(5)
        assertText("- Fo", 4)
        editText.setText("- [ ] Foo")
        pressBackspace(9)
        assertText("- [ ] Fo", 8)
    }

    private fun assertText(expected: String, cursorPosition: Int) {
        assertEquals(expected, editText.text.toString())
        assertEquals(cursorPosition, editText.selectionStart)
        assertEquals(cursorPosition, editText.selectionEnd)
    }

    private fun pressBackspace(atPosition: Int) {
        editText.setSelection(atPosition)
        editText.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
    }
}