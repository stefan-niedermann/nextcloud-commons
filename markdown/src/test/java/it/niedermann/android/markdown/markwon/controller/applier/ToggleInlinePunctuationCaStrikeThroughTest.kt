package it.niedermann.android.markdown.markwon.controller.applier

import it.niedermann.android.markdown.controller.applier.ToggleInlinePunctuationCa
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ToggleInlinePunctuationCaStrikeThroughTest : CommandApplierTest(
    ToggleInlinePunctuationCa("~~"), listOf(
        TestCase(
            "Add strike",
            TestCase.InitialState("Lorem ipsum dolor sit amet.", 6, 11),
            TestCase.ExpectedResult("Lorem ~~ipsum~~ dolor sit amet.", 13)
        ),
        TestCase(
            "Remove strike",
            TestCase.InitialState("Lorem ~~ipsum~~ dolor sit amet.", 8, 13),
            TestCase.ExpectedResult("Lorem ipsum dolor sit amet.", 11)
        )
    )
)
