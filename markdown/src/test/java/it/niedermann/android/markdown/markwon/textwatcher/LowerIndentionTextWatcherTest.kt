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
    private var editText: EditText? = null
    @Before
    fun reset() {
        editText = MarkwonMarkdownEditor(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun shouldLowerIndentionByTwoWhenPressingBackspaceOnAnIndentedList() {
        editText!!.setText("  - ")
        pressBackspace(4)
        assertText("- ", 2)
        editText!!.setText("- [ ] Foo\n  - [ ] ")
        pressBackspace(18)
        assertText("- [ ] Foo\n- [ ] ", 16)
    }

    @Test
    fun shouldLowerIndentionByOneWhenPressingBackspaceOnAListWhichIsIndentedByOneSpace() {
        editText!!.setText(" - ")
        pressBackspace(3)
        assertText("- ", 2)
    }

    @Test
    fun shouldNotLowerIndentionByOneWhenCursorIsNotAtTheEnd() {
        editText!!.setText(" - ")
        pressBackspace(2)
        assertText("  ", 1)
        editText!!.setText("  - ")
        pressBackspace(0)
        assertText("  - ", 0)
        editText!!.setText("  - ")
        pressBackspace(3)
        assertText("   ", 2)
        editText!!.setText("  - ")
        pressBackspace(2)
        assertText(" - ", 1)
        editText!!.setText("- Foo\n  - ")
        pressBackspace(9)
        assertText("- Foo\n   ", 8)
    }

    @Test
    fun shouldNotLowerIndentionIfThereIsAnyContentAfterTheList() {
        editText!!.setText("  -  ")
        pressBackspace(4)
        assertText("  - ", 3)
        editText!!.setText("  -  ")
        pressBackspace(5)
        assertText("  - ", 4)
    }

    @Test
    fun shouldNotLowerIndentionIfBackspaceWasPressedInTheNextLine() {
        editText!!.setText("  - \nFoo")
        pressBackspace(5)
        assertText("  - Foo", 4)
    }

    @Test
    fun shouldDeleteLastCharacterWhenPressingBackspace() {
        editText!!.setText("")
        pressBackspace(0)
        assertText("", 0)
        editText!!.setText("- [ ] ")
        pressBackspace(6)
        assertText("- [ ]", 5)
        editText!!.setText("- Foo")
        pressBackspace(5)
        assertText("- Fo", 4)
        editText!!.setText("- [ ] Foo")
        pressBackspace(9)
        assertText("- [ ] Fo", 8)
    }

    private fun assertText(expected: String, cursorPosition: Int) {
        assertEquals(expected, editText!!.text.toString())
        assertEquals(cursorPosition, editText!!.selectionStart)
        assertEquals(cursorPosition, editText!!.selectionEnd)
    }

    private fun pressBackspace(atPosition: Int) {
        editText!!.setSelection(atPosition)
        editText!!.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
    }
}