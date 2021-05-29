package it.niedermann.nextcloud.sso.glide

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.bumptech.glide.load.model.GlideUrl
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
class SingleSignOnOriginUrlLoaderTest {

    private val loader = SingleSignOnUrlLoader(ApplicationProvider.getApplicationContext())

    @Test
    fun handles() {
        val ssoAccount = SingleSignOnAccount("Test", "test", "", "https://nc.example.com", "")

        mockkStatic(SingleAccountHelper::class);
        every { SingleAccountHelper.getCurrentSingleSignOnAccount(any()) } returns ssoAccount;


        assertTrue(loader.handles(SingleSignOnUrl(ssoAccount, "https://nc.example.com/avatar")))

        assertTrue("Should try to handle default ${GlideUrl::class.java.simpleName}s if they start with the same URL as the current ${SingleSignOnAccount::class.java.simpleName}",
                loader.handles(GlideUrl("https://nc.example.com/avatar")))

        assertTrue("Should try to handle default ${GlideUrl::class.java.simpleName}s if they are only relative paths",
                loader.handles(GlideUrl("/avatar")))


        assertFalse("Should not try to handle default ${GlideUrl::class.java.simpleName}s if they don't start with the same URL as the current ${SingleSignOnAccount::class.java.simpleName}",
                loader.handles(GlideUrl("https://example.com/avatar")))

        assertFalse("Should not try to handle default ${GlideUrl::class.java.simpleName}s if they don't start with the same URL as the current ${SingleSignOnAccount::class.java.simpleName}",
                loader.handles(GlideUrl("http://nc.example.com/avatar")))

        assertFalse("Should not try to handle default ${GlideUrl::class.java.simpleName}s if they don't start with the same URL as the current ${SingleSignOnAccount::class.java.simpleName}",
                loader.handles(GlideUrl("nc.example.com/avatar")))
    }
}