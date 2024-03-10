package it.niedermann.android.markdown.controller

import android.graphics.Color
import android.text.SpannableString
import androidx.test.core.app.ApplicationProvider
import com.google.common.util.concurrent.MoreExecutors
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EditorStateNotifierTest : TestCase() {

    private lateinit var editorStateNotifier: EditorStateNotifier
    private lateinit var listeners: Collection<EditorStateListener>

    private lateinit var editorStateMock: EditorState
    private lateinit var editorStateListenerMock: EditorStateListener

    @Before
    fun setup() {
        editorStateMock = mockk<EditorState>()
        editorStateListenerMock = mockk<EditorStateListener> {
            every { onEditorStateChanged(any()) } returns Unit
        }

        listeners = mutableSetOf(editorStateListenerMock)

        editorStateNotifier = EditorStateNotifier(
            listeners,
            { MoreExecutors.newDirectExecutorService() },
            { MoreExecutors.newDirectExecutorService() },
            MoreExecutors.newDirectExecutorService(),
            mockk<EditorState.Factory> {
                every { build(any(), any(), any(), any(), any(), any(), any()) } returns editorStateMock
            }
        )

        clearAllMocks(answers = false)
    }

    @Test
    fun `should notify controllers after adding them`() {
        editorStateNotifier.notify(
            ApplicationProvider.getApplicationContext(),
            true,
            Color.WHITE,
            SpannableString("foo"),
            1,
            2
        ).get()

        verify(exactly = 1) { editorStateListenerMock.onEditorStateChanged(editorStateMock) }
    }

    @Test
    fun `notify should not notify controllers if the state didn't change`() {
        editorStateNotifier.notify(
            ApplicationProvider.getApplicationContext(),
            true,
            Color.WHITE,
            SpannableString("foo"),
            1,
            2
        ).get()

        editorStateNotifier.notify(
            ApplicationProvider.getApplicationContext(),
            true,
            Color.WHITE,
            SpannableString("foo"),
            1,
            2
        ).get()

        verify(exactly = 1) { editorStateListenerMock.onEditorStateChanged(editorStateMock) }
    }

    @Test
    fun `forceNotify should notify the given controller`() {
        editorStateNotifier.forceNotify(
            ApplicationProvider.getApplicationContext(),
            editorStateListenerMock,
            true,
            Color.WHITE,
            SpannableString("foo"),
            1,
            2
        ).get()

        verify(exactly = 1) { editorStateListenerMock.onEditorStateChanged(editorStateMock) }
    }

    @Test
    fun `forceNotify should always notify the given controller, even if called twice`() {
        editorStateNotifier.forceNotify(
            ApplicationProvider.getApplicationContext(),
            editorStateListenerMock,
            true,
            Color.WHITE,
            SpannableString("foo"),
            1,
            2
        ).get()

        editorStateNotifier.forceNotify(
            ApplicationProvider.getApplicationContext(),
            editorStateListenerMock,
            true,
            Color.WHITE,
            SpannableString("foo"),
            1,
            2
        ).get()

        verify(exactly = 2) { editorStateListenerMock.onEditorStateChanged(editorStateMock) }
    }

    @Test
    fun `forceNotify should always notify the given controller, even if the same state has been notified before`() {
        editorStateNotifier.notify(
            ApplicationProvider.getApplicationContext(),
            true,
            Color.WHITE,
            SpannableString("foo"),
            1,
            2
        ).get()

        editorStateNotifier.forceNotify(
            ApplicationProvider.getApplicationContext(),
            editorStateListenerMock,
            true,
            Color.WHITE,
            SpannableString("foo"),
            1,
            2
        ).get()

        verify(exactly = 2) { editorStateListenerMock.onEditorStateChanged(editorStateMock) }
    }
}
