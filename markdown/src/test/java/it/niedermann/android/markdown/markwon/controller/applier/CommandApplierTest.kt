package it.niedermann.android.markdown.markwon.controller.applier

import android.content.Context
import android.text.SpannableStringBuilder
import androidx.test.core.app.ApplicationProvider
import it.niedermann.android.markdown.controller.applier.CommandApplier
import org.junit.Assert.assertEquals
import org.junit.ComparisonFailure
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
abstract class CommandApplierTest(
    private val ca: CommandApplier,
    private val cases: List<TestCase>
) {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun applyCommand() {
        for ((index, case) in cases.withIndex()) {
            try {
                val result = ca.applyCommand(
                    context,
                    SpannableStringBuilder(case.initialState.content),
                    case.initialState.selectionStart,
                    case.initialState.selectionEnd
                )
                assertEquals(case.expectedResult.content, result.content.toString())
                assertEquals(case.expectedResult.selection, result.selection)
            } catch (cf: ComparisonFailure) {
                val description = case.description ?: "Test #${index}"
                throw ComparisonFailure(description, cf.expected, cf.actual)
            } catch (e: AssertionError) {
                val description = case.description ?: "Test #${index}"
                throw AssertionError(description, e)
            } catch (e: Exception) {
                val description = case.description ?: "Test #${index}"
                throw Exception(description, e)
            }
        }
    }

    class TestCase(
        val description: String?,
        val initialState: InitialState,
        val expectedResult: ExpectedResult
    ) {
        constructor(
            initialState: InitialState,
            expectedResult: ExpectedResult
        ) : this(null, initialState, expectedResult)

        class InitialState(val content: String, val selectionStart: Int, val selectionEnd: Int)
        class ExpectedResult(val content: String, val selection: Int)
    }
}
