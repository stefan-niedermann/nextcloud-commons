package it.niedermann.nextcloud.sso.glide

import android.os.Build
import com.bumptech.glide.load.model.Headers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SingleSignOnOriginHeaderTest {
    @Test
    fun addSpecialHeader() {
        val testAccountName = "foo@example.com"
        val headers: Headers = SingleSignOnOriginHeader(testAccountName)
        assertEquals(1, headers.headers.size.toLong())
        assertTrue(headers.headers.containsKey(SingleSignOnStreamFetcher.X_HEADER_SSO_ACCOUNT_NAME))
        assertEquals("foo@example.com", headers.headers.get(SingleSignOnStreamFetcher.X_HEADER_SSO_ACCOUNT_NAME))
        val headersWithExistingHeaders: Headers = SingleSignOnOriginHeader(testAccountName) {
            object : HashMap<String?, String?>() {
                init {
                    put("FOO", "BAR")
                    put("MEOW", "WOOF")
                }
            }
        }
        assertEquals(3, headersWithExistingHeaders.headers.size.toLong())
        assertTrue(headersWithExistingHeaders.headers.containsKey(SingleSignOnStreamFetcher.X_HEADER_SSO_ACCOUNT_NAME))
        assertEquals("foo@mexample.com", headers.headers.get(SingleSignOnStreamFetcher.X_HEADER_SSO_ACCOUNT_NAME))
    }
}