package it.niedermann.android.markdown.markwon.controller.applier

import it.niedermann.android.markdown.controller.applier.ToggleInlinePunctuationCa
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ToggleInlinePunctuationCaBoldTest : CommandApplierTest(
    ToggleInlinePunctuationCa("**"), listOf(
        TestCase(
            "Add bold to empty input",
            TestCase.InitialState("", 0, 0),
            TestCase.ExpectedResult("****", 2)
        ),
        TestCase(
            "Add bold",
            TestCase.InitialState("Lorem ipsum dolor sit amet.", 6, 11),
            TestCase.ExpectedResult("Lorem **ipsum** dolor sit amet.", 13)
        ),
        TestCase(
            "Remove bold",
            TestCase.InitialState("Lorem **ipsum** dolor sit amet.", 8, 13),
            TestCase.ExpectedResult("Lorem ipsum dolor sit amet.", 11)
        ),
        TestCase(
            "Do nothing when the same punctuation is contained only one time",
            TestCase.InitialState("Lorem **ipsum dolor sit amet.", 0, 29),
            TestCase.ExpectedResult("Lorem **ipsum dolor sit amet.", 29)
        ),
        TestCase(
            "Toggle bold on italic text",
            TestCase.InitialState("Lorem *ipsum* dolor sit amet.", 7, 12),
            TestCase.ExpectedResult("Lorem ***ipsum*** dolor sit amet.", 14)
        ),
        TestCase(
            "Toggle italic and bold to italic",
            TestCase.InitialState("Lorem ***ipsum*** dolor sit amet.", 9, 14),
            TestCase.ExpectedResult("Lorem *ipsum* dolor sit amet.", 12)
        ),
        TestCase(
            "Toggle multiple italic and bold to italic",
            TestCase.InitialState("Lorem ***ipsum*** dolor ***sit*** amet.", 0, 38),
            TestCase.ExpectedResult("Lorem *ipsum* dolor *sit* amet.", 30)
        ),
        TestCase(
            "Toggle bold on an empty text",
            TestCase.InitialState("", 0, 0),
            TestCase.ExpectedResult("****", 2)
        ),
        TestCase(
            "Toggle bold on a blank selection",
            TestCase.InitialState(" ", 0, 1),
            TestCase.ExpectedResult("** **", 3)
        ),
        TestCase(
            "Toggle bold on a partial blank selection",
            TestCase.InitialState("   ", 1, 2),
            TestCase.ExpectedResult(" ** ** ", 4)
        ),

        // Multiline
        TestCase(
            TestCase.InitialState("Bold\n*Italic*", 0, 4),
            TestCase.ExpectedResult("**Bold**\n*Italic*", 6)
        ),
        TestCase(
            TestCase.InitialState("*Italic*\nBold", 9, 13),
            TestCase.ExpectedResult("*Italic*\n**Bold**", 15)
        )
    )
)
