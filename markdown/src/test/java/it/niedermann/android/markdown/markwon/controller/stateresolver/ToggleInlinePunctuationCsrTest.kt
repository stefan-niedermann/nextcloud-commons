package it.niedermann.android.markdown.markwon.controller.stateresolver

import android.content.Context
import android.text.SpannableStringBuilder
import androidx.test.core.app.ApplicationProvider
import it.niedermann.android.markdown.controller.stateresolver.CommandStateResolver
import it.niedermann.android.markdown.controller.stateresolver.ToggleInlinePunctuationCsr
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ToggleInlinePunctuationCsrTest {

    lateinit var csr: CommandStateResolver
    val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setup() {
        csr = ToggleInlinePunctuationCsr("*")
    }

    @Test
    fun isEnabled() {
        val content = SpannableStringBuilder("")

        assertEquals(true, csr.isEnabled(context, content, 0, 0))
        assertThrows(IndexOutOfBoundsException::class.java) {
            assertEquals(true, csr.isEnabled(context, content, 0, 1))
        }
    }
}
