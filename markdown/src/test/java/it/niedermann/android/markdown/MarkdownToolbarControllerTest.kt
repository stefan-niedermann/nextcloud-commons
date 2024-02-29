package it.niedermann.android.markdown

import androidx.test.core.app.ApplicationProvider
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MarkdownToolbarControllerTest : TestCase() {

    private lateinit var controller: MarkdownToolbarController
    private lateinit var editorMock: MarkwonMarkdownEditor

    private fun MarkdownToolbarController.getEditor(): MarkwonMarkdownEditor? {
        return javaClass.getDeclaredField("editor").let {
            it.isAccessible = true
            return@let it.get(this);
        } as MarkwonMarkdownEditor?
    }

    private fun createEditorMock(): MarkwonMarkdownEditor {
        val mock = mockk<MarkwonMarkdownEditor>()
        every { mock.registerController(any()) } returns Unit
        every { mock.unregisterController(any()) } returns Unit
        return mock
    }

    @Before
    fun setup() {
        controller = MarkdownToolbarController(ApplicationProvider.getApplicationContext())
        assertNull(controller.getEditor())
        editorMock = createEditorMock()
    }

    @Test
    fun `setEditor - from null to null`() {
        controller.setEditor(null)

        assertNull(controller.getEditor())
        verify(exactly = 0) { editorMock.registerController(any()) }
        verify(exactly = 0) { editorMock.unregisterController(any()) }
    }

    @Test
    fun `setEditor - from null to A`() {
        controller.setEditor(editorMock)

        assertEquals(editorMock, controller.getEditor())
        verify(exactly = 1) { editorMock.registerController(controller) }
        verify(exactly = 0) { editorMock.unregisterController(any()) }
    }

    @Test
    fun `setEditor - from A to null`() {
        controller.setEditor(editorMock)
        clearAllMocks(answers = false)

        controller.setEditor(null)

        assertNull(controller.getEditor())
        verify(exactly = 0) { editorMock.registerController(any()) }
        verify(exactly = 1) { editorMock.unregisterController(controller) }
    }

    @Test
    fun `setEditor - from A to A`() {
        controller.setEditor(editorMock)
        clearAllMocks(answers = false)

        controller.setEditor(editorMock)

        assertEquals(editorMock, controller.getEditor())
        verify(exactly = 0) { editorMock.registerController(any()) }
        verify(exactly = 0) { editorMock.unregisterController(any()) }
    }

    @Test
    fun `setEditor - from A to B`() {
        val anotherEditorMock = createEditorMock()
        controller.setEditor(editorMock)
        clearAllMocks(answers = false)

        controller.setEditor(anotherEditorMock)

        assertEquals(anotherEditorMock, controller.getEditor())
        verify(exactly = 0) { editorMock.registerController(any()) }
        verify(exactly = 1) { editorMock.unregisterController(controller) }
        verify(exactly = 1) { anotherEditorMock.registerController(controller) }
        verify(exactly = 0) { anotherEditorMock.unregisterController(any()) }
    }
}
