package it.niedermann.android.markdown.markwon

import android.content.Context
import android.text.Spannable
import androidx.test.core.app.ApplicationProvider
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.niedermann.android.markdown.controller.EditorStateListener
import it.niedermann.android.markdown.controller.EditorStateNotifier
import it.niedermann.android.markdown.controller.MarkdownController
import it.niedermann.android.markdown.markwon.format.AbstractFormattingCallback
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.CompletableFuture

@RunWith(RobolectricTestRunner::class)
class MarkwonMarkdownEditorTest : TestCase() {

    private lateinit var editor: MarkwonMarkdownEditor
    private lateinit var editorStateNotifierMock: EditorStateNotifier

    private fun MarkwonMarkdownEditor.getControllers(filterFormattingCallbacks: Boolean = true): Collection<MarkdownController> {
        @Suppress("UNCHECKED_CAST")
        val controllers = (javaClass.getDeclaredField("controllers").let {
            it.isAccessible = true
            return@let it.get(this);
        } as Collection<MarkdownController>)
        return if (filterFormattingCallbacks) controllers
            .filter { it !is AbstractFormattingCallback } else controllers
    }

    private fun createControllerMock(): MarkdownController {
        return mockk<MarkdownController> {
            every { setCommandReceiver(any(MarkwonMarkdownEditor::class)) } returns Unit
        }
    }

    private fun createEditorStateNotifierMock(): EditorStateNotifier {
        return mockk<EditorStateNotifier> {
            every {
                notify(
                    any(Context::class),
                    any(),
                    any(),
                    any(Spannable::class),
                    any(),
                    any()
                )
            } returns CompletableFuture.completedFuture(null)
            every {
                forceNotify(
                    any(Context::class),
                    any(EditorStateListener::class),
                    any(Boolean::class),
                    any(),
                    any(Spannable::class),
                    any(),
                    any()
                )
            } returns CompletableFuture.completedFuture(null)
        }
    }

    @Before
    fun setup() {
        editorStateNotifierMock = createEditorStateNotifierMock()
        editor = MarkwonMarkdownEditor(
            ApplicationProvider.getApplicationContext(),
            null,
            android.R.attr.editTextStyle
        ) {
            editorStateNotifierMock
        }
        editor.setMarkdownString("foo")
        clearAllMocks(answers = false)
    }

    @Test
    fun `should forced notify recently registered controllers`() {
        val controllerMock = createControllerMock()
        val notified = editor.registerController(controllerMock)

        assertTrue(notified.isDone)
        assertEquals(1, editor.getControllers().size)
        assertTrue(editor.getControllers().contains(controllerMock))

        verify(exactly = 1) {
            editorStateNotifierMock.forceNotify(
                any(Context::class),
                any(EditorStateListener::class),
                any(Boolean::class),
                any(),
                any(Spannable::class),
                any(),
                any()
            )
        }
    }

    @Test
    fun `should notify all registered controllers on selection changed`() {
        val controllerMock = createControllerMock()
        val notified = editor.registerController(controllerMock)

        assertTrue(notified.isDone)
        assertEquals(1, editor.getControllers().size)
        assertTrue(editor.getControllers().contains(controllerMock))

        editor.setSelection(0) // Does not cause a notification because the selection was already on 0
        editor.setSelection(1)
        editor.setSelection(2)

        verify(exactly = 2) {
            editorStateNotifierMock.notify(
                any(Context::class),
                any(Boolean::class),
                any(),
                any(Spannable::class),
                any(),
                any()
            )
        }
    }
}
