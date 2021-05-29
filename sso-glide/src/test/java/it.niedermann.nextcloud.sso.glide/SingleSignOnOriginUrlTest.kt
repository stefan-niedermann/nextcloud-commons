package it.niedermann.nextcloud.sso.glide

import android.os.Build
import com.nextcloud.android.sso.model.SingleSignOnAccount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.URL

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SingleSignOnOriginUrlTest {

    @Test
    fun handles() {
        val rootSsoAccount = SingleSignOnAccount("Test 1", "test1", "", "https://nc.example.com", "")

        assertEquals("https://nc.example.com/avatar", SingleSignOnUrl(rootSsoAccount, "/avatar").toStringUrl())
        assertEquals("https://nc.example.com/avatar", SingleSignOnUrl(rootSsoAccount, "https://nc.example.com/avatar").toStringUrl())
        assertEquals("https://nc.example.com/avatar", SingleSignOnUrl(rootSsoAccount, URL("https://nc.example.com/avatar")).toStringUrl())
        assertEquals("https://nc.example.com/avatar", SingleSignOnUrl(rootSsoAccount, "/avatar").toURL().toString())
        assertEquals("https://nc.example.com/avatar", SingleSignOnUrl(rootSsoAccount, "https://nc.example.com/avatar").toURL().toString())
        assertEquals("https://nc.example.com/avatar", SingleSignOnUrl(rootSsoAccount, URL("https://nc.example.com/avatar")).toURL().toString())
        assertEquals("https://nc.example.com/avatar", SingleSignOnUrl(rootSsoAccount, "/avatar").toString())
        assertEquals("https://nc.example.com/avatar", SingleSignOnUrl(rootSsoAccount, "https://nc.example.com/avatar").toString())
        assertEquals("https://nc.example.com/avatar", SingleSignOnUrl(rootSsoAccount, URL("https://nc.example.com/avatar")).toString())
        assertThrows(IllegalArgumentException::class.java) { SingleSignOnUrl(rootSsoAccount, "https://example.com/avatar") }
        assertThrows(IllegalArgumentException::class.java) { SingleSignOnUrl(rootSsoAccount, URL("https://example.com/avatar")) }


        val subDirSsoAccount = SingleSignOnAccount("Test 2", "test2", "", "https://example.com/nextcloud", "")

        assertEquals("https://example.com/nextcloud/avatar", SingleSignOnUrl(subDirSsoAccount, "/avatar").toStringUrl())
        assertEquals("https://example.com/nextcloud/avatar", SingleSignOnUrl(subDirSsoAccount, "https://example.com/nextcloud/avatar").toStringUrl())
        assertEquals("https://example.com/nextcloud/avatar", SingleSignOnUrl(subDirSsoAccount, URL("https://example.com/nextcloud/avatar")).toStringUrl())
        assertEquals("https://example.com/nextcloud/avatar", SingleSignOnUrl(subDirSsoAccount, "/avatar").toURL().toString())
        assertEquals("https://example.com/nextcloud/avatar", SingleSignOnUrl(subDirSsoAccount, "https://example.com/nextcloud/avatar").toURL().toString())
        assertEquals("https://example.com/nextcloud/avatar", SingleSignOnUrl(subDirSsoAccount, URL("https://example.com/nextcloud/avatar")).toURL().toString())
        assertEquals("https://example.com/nextcloud/avatar", SingleSignOnUrl(subDirSsoAccount, "/avatar").toString())
        assertEquals("https://example.com/nextcloud/avatar", SingleSignOnUrl(subDirSsoAccount, "https://example.com/nextcloud/avatar").toString())
        assertEquals("https://example.com/nextcloud/avatar", SingleSignOnUrl(subDirSsoAccount, URL("https://example.com/nextcloud/avatar")).toString())
        assertThrows(IllegalArgumentException::class.java) { SingleSignOnUrl(rootSsoAccount, "https://example.com/avatar") }
        assertThrows(IllegalArgumentException::class.java) { SingleSignOnUrl(rootSsoAccount, URL("https://example.com/avatar")) }
    }
}