package it.niedermann.nextcloud.sso.glide

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumptech.glide.load.model.Headers
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class SingleSignOnOriginHeaderTest {
    @Test
    fun addSpecialHeader() {
        val testAccountName = "foo@example.com"
        val headers: Headers = SingleSignOnOriginHeader(testAccountName)
        Assert.assertEquals(2, headers.headers.size.toLong())
        Assert.assertTrue(headers.headers.containsKey(SingleSignOnStreamFetcher.X_HEADER_SSO_ACCOUNT_NAME))
        val headersWithExistingHeaders: Headers = SingleSignOnOriginHeader(testAccountName) {
            object : HashMap<String?, String?>() {
                init {
                    put("FOO", "BAR")
                    put("MEOW", "WOOF")
                }
            }
        }
        Assert.assertEquals(4, headersWithExistingHeaders.headers.size.toLong())
        Assert.assertTrue(headersWithExistingHeaders.headers.containsKey(SingleSignOnStreamFetcher.X_HEADER_SSO_ACCOUNT_NAME))
    }
}