package it.niedermann.android.markdown.markwon.controller.applier

import it.niedermann.android.markdown.controller.applier.ToggleCheckboxCa
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ToggleCheckboxCaTest : CommandApplierTest(
    ToggleCheckboxCa(), listOf(
        TestCase(
            "Add checkbox to empty content",
            TestCase.InitialState("", 0, 0),
            TestCase.ExpectedResult("- [ ] ", 6)
        ),
        TestCase(
            "Remove existing unchecked checkbox without trailing space",
            TestCase.InitialState("- [ ]", 5, 5),
            TestCase.ExpectedResult("", 0)
        ),
        TestCase(
            "Remove existing unchecked checkbox with trailing space",
            TestCase.InitialState("- [ ] ", 6, 6),
            TestCase.ExpectedResult("", 0)
        ),
        TestCase(
            "Continue with same punctuation",
            TestCase.InitialState("* [ ] Foo\nBar", 10, 13),
            TestCase.ExpectedResult("* [ ] Foo\n* [ ] Bar", 19)
        ),
    )
)
