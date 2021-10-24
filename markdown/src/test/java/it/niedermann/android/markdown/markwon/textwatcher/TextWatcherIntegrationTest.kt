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
class TextWatcherIntegrationTest : TestCase() {
    private lateinit var editText: EditText

    @Before
    fun reset() {
        editText = MarkwonMarkdownEditor(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `should successfully handle multiple text edits`() {
        editText.setText("")
        assertEquals("")
        sendKeys(KeyEvent.KEYCODE_F, KeyEvent.KEYCODE_O, KeyEvent.KEYCODE_O)
        assertEquals("foo")
        pressEnter()
        assertEquals("foo\n")
        sendCheckboxKeys()
        assertEquals("foo\n- [ ] ")
        pressEnter()
        assertEquals("foo\n\n")
        sendCheckboxKeys()
        sendKeys(KeyEvent.KEYCODE_B, KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_R)
        assertEquals("foo\n\n- [ ] bar")
        pressEnter()
        assertEquals("foo\n\n- [ ] bar\n- [ ] ")
        pressEnter()
        assertEquals("foo\n\n- [ ] bar\n\n")
        pressBackspace()
        assertEquals("foo\n\n- [ ] bar\n")
        sendKeys(KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_SPACE)
        sendCheckboxKeys()
        sendKeys(KeyEvent.KEYCODE_B, KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_R)
        assertEquals("foo\n\n- [ ] bar\n  - [ ] bar")
        pressEnter()
        assertEquals("foo\n\n- [ ] bar\n  - [ ] bar\n  - [ ] ")
        pressBackspace()
        assertEquals("foo\n\n- [ ] bar\n  - [ ] bar\n- [ ] ")
        pressBackspace(6, 33)
        assertEquals("foo\n\n- [ ] bar\n  - [ ] bar\n")
        sendKeys(KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_PERIOD, KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_Q, KeyEvent.KEYCODE_U, KeyEvent.KEYCODE_X)
        assertEquals("foo\n\n- [ ] bar\n  - [ ] bar\n  1. qux")
        pressEnter()
        assertEquals("foo\n\n- [ ] bar\n  - [ ] bar\n  1. qux\n  2. ")
        pressEnter(14)
        pressBackspace(6, 21)
        assertEquals("foo\n\n- [ ] bar\n\n  - [ ] bar\n  1. qux\n  2. ")
        sendKeys(KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_STAR)
        pressBackspace()
        sendKeys(KeyEvent.KEYCODE_STAR, KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_B, KeyEvent.KEYCODE_C)
        assertEquals("foo\n\n- [ ] bar\n  * abc\n  - [ ] bar\n  1. qux\n  2. ")
    }

    // Convenience methods
    private fun sendCheckboxKeys() {
        sendKeys(KeyEvent.KEYCODE_MINUS, KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_LEFT_BRACKET, KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_RIGHT_BRACKET, KeyEvent.KEYCODE_SPACE)
    }

    private fun assertEquals(expected: CharSequence) {
        assertEquals(expected.toString(), editText.text.toString())
    }

    private fun pressEnter(@Suppress("SameParameterValue") atPosition: Int) {
        editText.setSelection(atPosition)
        pressEnter()
    }

    private fun pressEnter() {
        editText.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
    }

    private fun pressBackspace(@Suppress("SameParameterValue") times: Int, startPosition: Int) {
        var backspaceCounter = times
        var currentPosition = startPosition
        require(backspaceCounter <= currentPosition) { "startPosition must be bigger or equal to times" }
        while (backspaceCounter > 0) {
            backspaceCounter--
            pressBackspace(currentPosition--)
        }
    }

    private fun pressBackspace(atPosition: Int) {
        editText.setSelection(atPosition)
        pressBackspace()
    }

    private fun pressBackspace() {
        editText.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
    }

    private fun sendKeys(vararg keyCodes: Int) {
        var release = -1
        for (k in keyCodes) {
            if (release != -1) {
                editText.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, release))
                release = -1
            }
            when (k) {
                KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_CTRL_RIGHT -> {
                    release = k
                }
            }
            editText.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, k))
        }
    }
}