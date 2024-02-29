package it.niedermann.android.markdown.markwon

import androidx.test.core.app.ApplicationProvider
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.niedermann.android.markdown.MarkdownController
import it.niedermann.android.markdown.MarkdownToolbarController
import it.niedermann.android.markdown.markwon.format.AbstractFormattingCallback
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MarkwonMarkdownEditorTest : TestCase() {

    private lateinit var editor: MarkwonMarkdownEditor
    private lateinit var controllerMock: MarkdownController

    private fun MarkwonMarkdownEditor.getControllers(): Collection<MarkdownController> {
        @Suppress("UNCHECKED_CAST")
        return (javaClass.getDeclaredField("controllers").let {
            it.isAccessible = true
            return@let it.get(this);
        } as Collection<MarkdownController>)
            .filter { it !is AbstractFormattingCallback }
    }

    private fun createControllerMock(): MarkdownController {
        val mock = mockk<MarkdownToolbarController>()
        every { mock.setEditor(any(MarkwonMarkdownEditor::class)) } returns Unit
        return mock
    }

    @Before
    fun setup() {
        editor = MarkwonMarkdownEditor(ApplicationProvider.getApplicationContext())
        assertEquals(0, editor.getControllers().size)
        controllerMock = createControllerMock()
        clearAllMocks(answers = false)
    }

    @Test
    fun `registerController - A to ()`() {
        editor.registerController(controllerMock)

        assertEquals(1, editor.getControllers().size)
        assertTrue(editor.getControllers().contains(controllerMock))
        verify(exactly = 1) { controllerMock.setEditor(any()) }
        verify(exactly = 1) { editor.registerController(any()) }
        verify(exactly = 0) { editor.unregisterController(any()) }
    }
//
//    @Test
//    fun `registerController - A to (A)`() {
//    }
//
//    @Test
//    fun `registerController - A to (B)`() {
//    }
//
//    @Test
//    fun `registerController - A to (A, B)`() {
//    }
//
//    @Test
//    fun `unregisterController - A from ()`() {
//    }
//
//    @Test
//    fun `unregisterController - A from (A)`() {
//    }
//
//    @Test
//    fun `unregisterController - A from (B)`() {
//    }
//
//    @Test
//    fun `unregisterController - A from (A, B)`() {
//    }
}
