package it.niedermann.android.markdown.controller

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.CompletableFuture

@RunWith(RobolectricTestRunner::class)
class ControllerConnectorTest : TestCase() {

    private lateinit var commandReceiverMock: CommandReceiver
    private lateinit var controllerMock: MarkdownController

    @Before
    fun setup() {
        commandReceiverMock = mockk<CommandReceiver> {
            every { registerController(any()) } returns CompletableFuture.completedFuture(null)
            every { unregisterController(any()) } returns Unit
        }
        controllerMock = mockk<MarkdownController> {
            every { setCommandReceiver(any()) } returns Unit
        }
        ControllerConnector.connect(null, commandReceiverMock, controllerMock)
    }

    @Test
    fun `should directly connect controller and command receiver`() {
        verify(exactly = 1) { commandReceiverMock.registerController(controllerMock) }
        verify(exactly = 0) { commandReceiverMock.unregisterController(any()) }
        verify(exactly = 0) { controllerMock.setCommandReceiver(null) }
    }
}
