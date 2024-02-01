package it.niedermann.android.markdown.markwon.plugins.mentions

import androidx.test.core.app.ApplicationProvider
import com.nextcloud.android.sso.model.SingleSignOnAccount
import com.nextcloud.android.sso.model.ocs.OcsResponse
import com.nextcloud.android.sso.model.ocs.OcsUser
import io.mockk.every
import io.mockk.mockk
import it.niedermann.nextcloud.ocs.ApiProvider
import it.niedermann.nextcloud.ocs.OcsAPI
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Call
import retrofit2.Response
import java.util.*

@RunWith(RobolectricTestRunner::class)
class DisplayNameUtilTest : TestCase() {

    private lateinit var util: DisplayNameUtil
    private val actualUsers = mutableMapOf<String, String>()
    private val userCache = mutableMapOf<String, String>()
    private val noUserCache = mutableSetOf<String>()

    @Before
    fun setup() {
        actualUsers.putAll(
            mapOf(
                "foo" to "Foo Bidoo",
                "bar" to "Bar Iton",
                "baz" to "Baz Lightyear"
            )
        )
        userCache.putAll(
            mapOf(
                "foo" to "Foo Bidoo"
            )
        )
        noUserCache.addAll(
            setOf(
                "qux"
            )
        )

        val utilConstructor = DisplayNameUtil::class.java.getDeclaredConstructor(
            Map::class.java,
            Set::class.java,
            ApiProvider.Factory::class.java
        )

        utilConstructor.isAccessible = true

        util = utilConstructor.newInstance(
            userCache,
            noUserCache,
            mockk<ApiProvider.Factory> {
                every {
                    createApiProvider<OcsAPI>(any(), any(), any(), any())
                } returns getApiProviderMock()
            }
        )
    }

    @After
    fun teardown() {
        actualUsers.clear()
        userCache.clear()
        noUserCache.clear()
    }

    @Test
    fun fetchDisplayNames() {
        val potentialUserNames = setOf("foo", "bar", "baz", "qux", "quux")

        val displayNames = util.fetchDisplayNames(
            ApplicationProvider.getApplicationContext(),
            mockk<SingleSignOnAccount>(),
            potentialUserNames
        )

        assertEquals("Foo Bidoo", displayNames["foo"])
        assertEquals("Bar Iton", displayNames["bar"])
        assertEquals("Baz Lightyear", displayNames["baz"])

        assertTrue(userCache.containsKey("foo"))
        assertTrue(userCache.containsKey("bar"))
        assertTrue(userCache.containsKey("baz"))

        assertTrue(noUserCache.contains("qux"))
        assertTrue(noUserCache.contains("quux"))
    }

    private fun getApiProviderMock(): ApiProvider<OcsAPI> {
        return mockk<ApiProvider<OcsAPI>> {
            every { getApi() } returns getOcsApiMock()
            every { close() } answers {

            }
        }
    }

    private fun getOcsApiMock(): OcsAPI {
        return mockk<OcsAPI> {
            every {
                getUser(any())
            } answers {
                val userName = firstArg<String>();
                mockk<Call<OcsResponse<OcsUser>>> {
                    every {
                        execute()
                    } returns mockk<Response<OcsResponse<OcsUser>>> {
                        every {
                            isSuccessful
                        } returns (actualUsers.containsKey(userName))
                        every {
                            code()
                        } returns if (actualUsers.containsKey(userName)) 200 else 404
                        every {
                            message()
                        } returns "--- mock ---"
                        every {
                            body()
                        } answers {
                            val response = OcsResponse<OcsUser>()
                            response.ocs = OcsResponse.OcsWrapper()
                            response.ocs.data = OcsUser()
                            response.ocs.data.displayName = actualUsers[userName]
                            response
                        }
                    }
                }
            }
        }
    }
}