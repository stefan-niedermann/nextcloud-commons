package it.niedermann.nextcloud.sso.glide

import androidx.test.core.app.ApplicationProvider
import com.bumptech.glide.load.model.Headers
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.nextcloud.android.sso.model.SingleSignOnAccount
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.URL

@RunWith(RobolectricTestRunner::class)
class SingleSignOnUrlLoaderTest {

    private val loader = SingleSignOnUrlLoader(ApplicationProvider.getApplicationContext())

    @Test
    fun `Should handle each SingleSignOnUrl`() {
        val ssoAccount = SingleSignOnAccount("Test", "test", "", "https://nc.example.com", "")

        mockkStatic(SingleAccountHelper::class)
        every { SingleAccountHelper.getCurrentSingleSignOnAccount(any()) } returns ssoAccount

        assertTrue(loader.handles(SingleSignOnUrl(ssoAccount, "/avatar")))
        assertTrue(loader.handles(SingleSignOnUrl(ssoAccount, "https://nc.example.com/avatar")))
        assertTrue(
            loader.handles(
                SingleSignOnUrl(
                    ssoAccount,
                    URL("https://nc.example.com/avatar")
                )
            )
        )

        assertTrue(loader.handles(SingleSignOnUrl(ssoAccount, "/avatar", Headers.DEFAULT)))
        assertTrue(
            loader.handles(
                SingleSignOnUrl(
                    ssoAccount,
                    "https://nc.example.com/avatar",
                    Headers.DEFAULT
                )
            )
        )
        assertTrue(
            loader.handles(
                SingleSignOnUrl(
                    ssoAccount,
                    URL("https://nc.example.com/avatar"),
                    Headers.DEFAULT
                )
            )
        )

        assertTrue(loader.handles(SingleSignOnUrl("foo", "/avatar")))
        assertTrue(loader.handles(SingleSignOnUrl("bar", "https://nc.example.com/avatar")))
        assertTrue(loader.handles(SingleSignOnUrl("baz", URL("https://nc.example.com/avatar"))))

        assertTrue(loader.handles(SingleSignOnUrl("foo", "/avatar", Headers.DEFAULT)))
        assertTrue(
            loader.handles(
                SingleSignOnUrl(
                    "bar",
                    "https://nc.example.com/avatar",
                    Headers.DEFAULT
                )
            )
        )
        assertTrue(
            loader.handles(
                SingleSignOnUrl(
                    "baz",
                    URL("https://nc.example.com/avatar"),
                    Headers.DEFAULT
                )
            )
        )
    }
}