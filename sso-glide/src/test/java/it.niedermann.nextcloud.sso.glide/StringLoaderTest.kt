package it.niedermann.nextcloud.sso.glide

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.nextcloud.android.sso.model.SingleSignOnAccount
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class StringLoaderTest {

    private val ssoAccount = SingleSignOnAccount("Test", "test", "", "https://nc.example.com", "")
    private val loader = StringLoader(ApplicationProvider.getApplicationContext())

    init {
        mockkStatic(SingleAccountHelper::class)
        every { SingleAccountHelper.getCurrentSingleSignOnAccount(any()) } returns ssoAccount
    }

    @Test
    fun `Should handle relative URLs (assuming the resource is located on the current SingleSignOnAccount)`() {
        assertTrue(loader.handles("/avatar"))
    }

    @Test
    fun `Should handle absolute URLs if they start with the URL of the current SingleSignOn account`() {
        assertTrue(loader.handles("https://nc.example.com/avatar"))
    }

    @Test
    fun `Should not handle absolute URLs if they don't start with the URL of the current SingleSignOn account`() {
        assertFalse(loader.handles("https://example.com/nextcloud/avatar"))
    }

    @Test
    fun `Should not handle URLs if they are neither absolute nor do start with a slash`() {
        assertFalse(loader.handles("avatar"))
    }
}