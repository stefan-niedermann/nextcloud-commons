package it.niedermann.android.markdown.controller

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import io.mockk.clearAllMocks
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

    private lateinit var connector: ControllerConnector
    private lateinit var lifecycle: LifecycleRegistry
    private lateinit var commandReceiverMock: CommandReceiver
    private lateinit var controllerMock: MarkdownController

    @Before
    fun setup() {
        val lifecycleOwner: LifecycleOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle get() = this@ControllerConnectorTest.lifecycle
        }
        lifecycle = LifecycleRegistry(lifecycleOwner)
        commandReceiverMock = mockk<CommandReceiver> {
            every { registerController(any()) } returns CompletableFuture.completedFuture(null)
            every { unregisterController(any()) } returns Unit
        }
        controllerMock = mockk<MarkdownController> {
            every { setCommandReceiver(any()) } returns Unit
        }
        connector = ControllerConnector.connect(lifecycleOwner, commandReceiverMock, controllerMock)
    }

    @Test
    fun `should not have connected the controller and the command receiver`() {
        lifecycle.currentState = Lifecycle.State.INITIALIZED

        verify(exactly = 0) { commandReceiverMock.registerController(controllerMock) }
        verify(exactly = 0) { controllerMock.setCommandReceiver(commandReceiverMock) }
        verify(exactly = 0) { commandReceiverMock.unregisterController(any()) }
        verify(exactly = 0) { controllerMock.setCommandReceiver(null) }
    }

    @Test
    fun `should have connected the controller and the command receiver after the state moved to resumed`() {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        verify(exactly = 1) { commandReceiverMock.registerController(controllerMock) }
        verify(exactly = 1) { controllerMock.setCommandReceiver(commandReceiverMock) }
        verify(exactly = 0) { commandReceiverMock.unregisterController(any()) }
        verify(exactly = 0) { controllerMock.setCommandReceiver(null) }
    }

    @Test
    fun `should connected, disconnect and reconnect the controller and the command receiver after the state moved from initial to resumed, paused and back to resumed`() {
        lifecycle.currentState = Lifecycle.State.INITIALIZED

        verify(exactly = 0) { commandReceiverMock.registerController(any()) }
        verify(exactly = 0) { controllerMock.setCommandReceiver(any()) }
        verify(exactly = 0) { commandReceiverMock.unregisterController(any()) }

        clearAllMocks(answers = false)
        lifecycle.currentState = Lifecycle.State.RESUMED

        verify(exactly = 1) { commandReceiverMock.registerController(controllerMock) }
        verify(exactly = 1) { controllerMock.setCommandReceiver(commandReceiverMock) }
        verify(exactly = 0) { commandReceiverMock.unregisterController(any()) }
        verify(exactly = 0) { controllerMock.setCommandReceiver(null) }

        clearAllMocks(answers = false)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)

        verify(exactly = 0) { commandReceiverMock.registerController(any()) }
        verify(exactly = 1) { commandReceiverMock.unregisterController(controllerMock) }
        verify(exactly = 1) { controllerMock.setCommandReceiver(null) }

        clearAllMocks(answers = false)
        lifecycle.currentState = Lifecycle.State.RESUMED

        verify(exactly = 1) { commandReceiverMock.registerController(controllerMock) }
        verify(exactly = 1) { controllerMock.setCommandReceiver(commandReceiverMock) }
        verify(exactly = 0) { commandReceiverMock.unregisterController(controllerMock) }
        verify(exactly = 0) { controllerMock.setCommandReceiver(null) }
    }
}
