package it.niedermann.android.markdown.controller

import android.graphics.Color
import android.view.MenuItem
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import it.niedermann.android.markdown.R
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MarkdownToolbarControllerTest : TestCase() {

    private lateinit var controller: MarkdownToolbarController
    private lateinit var commandReceiverMock: CommandReceiver

    private fun MarkdownController.getCommandReceiver(): CommandReceiver? {
        return javaClass.getDeclaredField("commandReceiver").let {
            it.isAccessible = true
            return@let it.get(this);
        } as CommandReceiver?
    }

    private fun MarkdownController.getEditorState(): EditorState? {
        return javaClass.getDeclaredField("state").let {
            it.isAccessible = true
            return@let it.get(this);
        } as EditorState?
    }

    private fun createCommandReceiverMock(): CommandReceiver {
        return mockk<CommandReceiver> {
            every { executeCommand(any(Command::class)) } returns Unit
        }
    }

    private fun createMenuItemMock(): MenuItem {
        return mockk<MenuItem> {
            every { itemId } answers { R.id.bold }
        }
    }

    private fun createEditorStateMock(): EditorState {
        return mockk<EditorState> {
            every { color } answers { Color.WHITE }
            every { commands } answers {
                mapOf(Command.TOGGLE_BOLD to mockk<CommandState> {
                    every { enabled } answers { true }
                })
            }
        }
    }

    @Before
    fun setup() {
        controller = spyk(
            MarkdownToolbarController(
                ApplicationProvider.getApplicationContext()
            )
        )
        commandReceiverMock = createCommandReceiverMock()

        assertNull(controller.getCommandReceiver())
        assertNull(controller.getEditorState())
    }

    @Test
    fun `setCommandReceiver - from null to null`() {
        controller.setCommandReceiver(null)

        assertNull(controller.getCommandReceiver())
        assertNull(controller.getEditorState())
    }

    @Test
    fun `setCommandReceiver - from null to A`() {
        controller.setCommandReceiver(commandReceiverMock)

        assertEquals(commandReceiverMock, controller.getCommandReceiver())
        assertNull(controller.getEditorState())
    }

    @Test
    fun `setCommandReceiver - from A to null`() {
        controller.setCommandReceiver(commandReceiverMock)

        controller.setCommandReceiver(null)

        assertNull(controller.getCommandReceiver())
        assertNull(controller.getEditorState())
    }

    @Test
    fun `setCommandReceiver - from A to A`() {
        controller.setCommandReceiver(commandReceiverMock)

        controller.setCommandReceiver(commandReceiverMock)

        assertEquals(commandReceiverMock, controller.getCommandReceiver())
        assertNull(controller.getEditorState())
    }

    @Test
    fun `setCommandReceiver - from A to B`() {
        val anotherEditorMock = createCommandReceiverMock()
        controller.setCommandReceiver(commandReceiverMock)

        controller.setCommandReceiver(anotherEditorMock)

        assertEquals(anotherEditorMock, controller.getCommandReceiver())
        assertNull(controller.getEditorState())
    }

    @Test
    fun `executing command`() {
        val mockState = createEditorStateMock()
        val mockMenuItem = createMenuItemMock()
        controller.setCommandReceiver(commandReceiverMock)
        controller.onEditorStateChanged(mockState)

        controller.onMenuItemClick(mockMenuItem)

        verify(exactly = 1) { commandReceiverMock.executeCommand(Command.TOGGLE_BOLD) }
    }
}
