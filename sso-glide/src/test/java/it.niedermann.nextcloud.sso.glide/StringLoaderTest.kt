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

    private val loader = StringLoader(ApplicationProvider.getApplicationContext())

    @Test
    fun handles() {
        val ssoAccount = SingleSignOnAccount("Test", "test", "", "https://nc.example.com", "")

        mockkStatic(SingleAccountHelper::class);
        every { SingleAccountHelper.getCurrentSingleSignOnAccount(any()) } returns ssoAccount;

        assertTrue(loader.handles("https://nc.example.com/avatar"))
        assertTrue(loader.handles("/avatar"))

        assertFalse(loader.handles("https://example.com/nextcloud/avatar"))
        assertFalse(loader.handles("avatar"))
    }
}