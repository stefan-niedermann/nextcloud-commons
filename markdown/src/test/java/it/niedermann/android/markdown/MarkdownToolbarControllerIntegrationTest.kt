package it.niedermann.android.markdown

import androidx.test.core.app.ApplicationProvider
import io.mockk.clearAllMocks
import io.mockk.spyk
import io.mockk.verify
import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor
import it.niedermann.android.markdown.markwon.format.AbstractFormattingCallback
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MarkdownToolbarControllerIntegrationTest : TestCase() {

    private lateinit var editor: MarkwonMarkdownEditor
    private lateinit var controller: MarkdownToolbarController

    private fun MarkdownToolbarController.getEditor(): MarkwonMarkdownEditor? {
        return javaClass.getDeclaredField("editor").let {
            it.isAccessible = true
            return@let it.get(this);
        } as MarkwonMarkdownEditor?
    }

    private fun MarkwonMarkdownEditor.getControllers(): Collection<MarkdownController> {
        @Suppress("UNCHECKED_CAST")
        return (javaClass.getDeclaredField("controllers").let {
            it.isAccessible = true
            return@let it.get(this);
        } as Collection<MarkdownController>)
            .filter { it !is AbstractFormattingCallback }
    }

    @Before
    fun setup() {
        controller = spyk(MarkdownToolbarController(ApplicationProvider.getApplicationContext()))
        assertNull(controller.getEditor())

        editor = spyk(MarkwonMarkdownEditor(ApplicationProvider.getApplicationContext()))
        assertEquals(0, editor.getControllers().size)
    }

    @Test
    fun `setEditor - from null to null`() {
        controller.setEditor(null)

        assertNull(controller.getEditor())
        verify(exactly = 0) { editor.registerController(any()) }
        verify(exactly = 0) { editor.unregisterController(any()) }
    }

    @Test
    fun `setEditor - from null to A`() {
        controller.setEditor(editor)

        assertEquals(editor, controller.getEditor())
        verify(exactly = 1) { editor.registerController(controller) }
        verify(exactly = 0) { editor.unregisterController(any()) }
    }

    @Test
    fun `setEditor - from A to null`() {
        controller.setEditor(editor)
        clearAllMocks(answers = false)

        controller.setEditor(null)

        assertNull(controller.getEditor())
        verify(exactly = 0) { editor.registerController(any()) }
        verify(exactly = 1) { editor.unregisterController(controller) }
    }

    @Test
    fun `setEditor - from A to A`() {
        controller.setEditor(editor)
        clearAllMocks(answers = false)

        controller.setEditor(editor)

        assertEquals(editor, controller.getEditor())
        verify(exactly = 0) { editor.registerController(any()) }
        verify(exactly = 0) { editor.unregisterController(any()) }
    }

    @Test
    fun `setEditor - from A to B`() {
        val anotherEditor = MarkwonMarkdownEditor(ApplicationProvider.getApplicationContext())
        controller.setEditor(editor)
        clearAllMocks(answers = false)

        controller.setEditor(anotherEditor)

        assertEquals(anotherEditor, controller.getEditor())
        verify(exactly = 0) { editor.registerController(any()) }
        verify(exactly = 1) { editor.unregisterController(controller) }
        verify(exactly = 1) { anotherEditor.registerController(controller) }
        verify(exactly = 0) { anotherEditor.unregisterController(any()) }
    }
}
