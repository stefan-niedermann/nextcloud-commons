package it.niedermann.android.markdown.markwon.controller.applier

import it.niedermann.android.markdown.controller.applier.ToggleInlinePunctuationCa
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ToggleInlinePunctuationCaItalicTest : CommandApplierTest(
    ToggleInlinePunctuationCa("*"), listOf(
        TestCase(
            "Add italic",
            TestCase.InitialState("Lorem ipsum dolor sit amet.", 6, 11),
            TestCase.ExpectedResult("Lorem *ipsum* dolor sit amet.", 12)
        ),
        TestCase(
            "Remove italic",
            TestCase.InitialState("Lorem *ipsum* dolor sit amet.", 7, 12),
            TestCase.ExpectedResult("Lorem ipsum dolor sit amet.", 11)
        ),
        TestCase(
            "Add italic at first position",
            TestCase.InitialState("Lorem ipsum dolor sit amet.", 0, 5),
            TestCase.ExpectedResult("*Lorem* ipsum dolor sit amet.", 6)
        ),
        TestCase(
            "Remove italic from first position",
            TestCase.InitialState("*Lorem* ipsum dolor sit amet.", 1, 6),
            TestCase.ExpectedResult("Lorem ipsum dolor sit amet.", 5)
        ),
        TestCase(
            "Add italic at last position",
            TestCase.InitialState("Lorem ipsum dolor sit amet.", 22, 27),
            TestCase.ExpectedResult("Lorem ipsum dolor sit *amet.*", 28)
        ),
        TestCase(
            "Remove italic from last position",
            TestCase.InitialState("Lorem ipsum dolor sit *amet.*", 23, 28),
            TestCase.ExpectedResult("Lorem ipsum dolor sit amet.", 27)
        ),

        // Text is not directly surrounded by punctuation but contains it
        TestCase(
            "Do nothing when the same punctuation is contained only one time",
            TestCase.InitialState("Lorem *ipsum dolor sit amet.", 0, 28),
            TestCase.ExpectedResult("Lorem *ipsum dolor sit amet.", 28)
        ),
        TestCase(
            "Remove containing punctuation",
            TestCase.InitialState("Lorem *ipsum* dolor sit amet.", 6, 13),
            TestCase.ExpectedResult("Lorem ipsum dolor sit amet.", 11)
        ),
        TestCase(
            "Remove containing punctuation",
            TestCase.InitialState("Lorem *ipsum* dolor sit amet.", 0, 29),
            TestCase.ExpectedResult("Lorem ipsum dolor sit amet.", 27)
        ),
        TestCase(
            "Remove multiple containing punctuations",
            TestCase.InitialState("Lorem *ipsum* dolor *sit* amet.", 0, 31),
            TestCase.ExpectedResult("Lorem ipsum dolor sit amet.", 27)
        ),
        TestCase(
            "Toggle italic on bold text",
            TestCase.InitialState("Lorem **ipsum** dolor sit amet.", 8, 13),
            TestCase.ExpectedResult("Lorem ***ipsum*** dolor sit amet.", 14)
        ),
        TestCase(
            "Toggle italic on bold text",
            TestCase.InitialState("Lorem **ipsum** dolor sit amet.", 6, 15),
            TestCase.ExpectedResult("Lorem ***ipsum*** dolor sit amet.", 16)
        ),
        TestCase(
            "Toggle bold to italic",
            TestCase.InitialState("Lorem **ipsum** dolor sit amet.", 0, 31),
            TestCase.ExpectedResult("*Lorem **ipsum** dolor sit amet.*", 32)
        ),
        TestCase(
            "Toggle italic and bold to bold",
            TestCase.InitialState("Lorem ***ipsum*** dolor sit amet.", 0, 14),
            TestCase.ExpectedResult("Lorem **ipsum** dolor sit amet.", 13)
        ),
        TestCase(
            "toggle italic around multiple existing bolds",
            TestCase.InitialState("Lorem **ipsum** dolor **sit** amet.", 0, 34),
            TestCase.ExpectedResult("*Lorem **ipsum** dolor **sit** amet*.", 35)
        ),
        TestCase(
            "Toggle multiple italic and bold to bold",
            TestCase.InitialState("Lorem ***ipsum*** dolor ***sit*** amet.", 0, 38),
            TestCase.ExpectedResult("Lorem **ipsum** dolor **sit** amet.", 34)
        ),
        TestCase(
            "Toggle italic on an empty text",
            TestCase.InitialState("", 0, 0),
            TestCase.ExpectedResult("**", 1)
        ),
        TestCase(
            "Toggle italic on a blank selection",
            TestCase.InitialState(" ", 0, 1),
            TestCase.ExpectedResult("* *", 2)
        ),
        TestCase(
            "Toggle italic on a partial blank selection",
            TestCase.InitialState("   ", 1, 2),
            TestCase.ExpectedResult(" * * ", 3)
        ),
        TestCase(
            "Toggle italic right after bold",
            TestCase.InitialState("**Bold**Italic", 8, 14),
            TestCase.ExpectedResult("**Bold***Italic*", 15)
        ),

        // Toggle italic for last of many bolds in one line
        TestCase(
            TestCase.InitialState("Lorem **Ipsum** **Dolor**", 18, 23),
            TestCase.ExpectedResult("Lorem **Ipsum** ***Dolor***", 24)
        ),
        TestCase(
            TestCase.InitialState("Lorem **Ipsum** **Dolor**", 8, 13),
            TestCase.ExpectedResult("Lorem ***Ipsum*** **Dolor**", 14)
        ),
        TestCase(
            TestCase.InitialState("Lorem **Ipsum** **Dolor**", 6, 15),
            TestCase.ExpectedResult("Lorem ***Ipsum*** **Dolor**", 16)
        ),

        // Toggle italic for last bold + italic in a row of multiple marked elements
        TestCase(
            TestCase.InitialState("Lorem **Ipsum** ***Dolor***", 19, 24),
            TestCase.ExpectedResult("Lorem **Ipsum** **Dolor**", 23)
        ),
        TestCase(
            TestCase.InitialState("Lorem ***Ipsum*** **Dolor**", 9, 14),
            TestCase.ExpectedResult("Lorem **Ipsum** **Dolor**", 13)
        ),
        TestCase(
            TestCase.InitialState("Lorem ***Ipsum*** **Dolor**", 6, 17),
            TestCase.ExpectedResult("Lorem **Ipsum** **Dolor**", 15)
        ),
        TestCase(
            TestCase.InitialState("Lorem ***Ipsum*** **Dolor**", 7, 16),
            TestCase.ExpectedResult("Lorem **Ipsum** **Dolor**", 15)
        ),
        TestCase(
            TestCase.InitialState("Lorem ***Ipsum*** **Dolor**", 7, 17),
            TestCase.ExpectedResult("Lorem **Ipsum** **Dolor**", 15)
        ),
        TestCase(
            TestCase.InitialState("Lorem ***Ipsum*** **Dolor**", 8, 16),
            TestCase.ExpectedResult("Lorem **Ipsum** **Dolor**", 15)
        ),

        // Multiline
        TestCase(
            TestCase.InitialState("Lorem ***Ipsum***\n **Dolor**", 0, 28),
            TestCase.ExpectedResult("*Lorem ***Ipsum***\n **Dolor***", 29)
        ),
        TestCase(
            TestCase.InitialState("**Bold**\nItalic", 9, 15),
            TestCase.ExpectedResult("**Bold**\n*Italic*", 16)
        ),
        TestCase(
            TestCase.InitialState("Italic\n**Bold**", 0, 6),
            TestCase.ExpectedResult("*Italic*\n**Bold**", 7)
        )
    )
)
