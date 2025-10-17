package it.niedermann.android.markdown.markwon.plugins.mentions

import androidx.test.core.app.ApplicationProvider
import com.google.common.util.concurrent.MoreExecutors
import com.nextcloud.android.sso.model.SingleSignOnAccount
import com.nextcloud.android.sso.model.ocs.OcsResponse
import com.nextcloud.android.sso.model.ocs.OcsUser
import io.mockk.every
import io.mockk.mockk
import it.niedermann.android.markdown.markwon.plugins.mentions.DisplayNameUtil.ExecutorServiceFactory
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


@RunWith(RobolectricTestRunner::class)
class DisplayNameUtilTest : TestCase() {

    private lateinit var util: DisplayNameUtil
    private val actualUsers = mutableMapOf<String, String>()
    private val cache = MentionsCache.getInstance()
    private val ssoAccount = SingleSignOnAccount("john-doe@example.com", "", "", "", "")

    @Before
    fun setup() {
        actualUsers.putAll(
            mapOf(
                "foo" to "Foo Bidoo",
                "bar" to "Bar Iton",
                "baz" to "Baz Lightyear"
            )
        )
        cache.setDisplayName(ssoAccount, "qux", "Foo Bidoo")
        cache.addKnownInvalidUserId(ssoAccount, "qux")

        val utilConstructor = DisplayNameUtil::class.java.getDeclaredConstructor(
            MentionsCache::class.java,
            ApiProvider.Factory::class.java,
            ExecutorServiceFactory::class.java
        )

        utilConstructor.isAccessible = true

        util = utilConstructor.newInstance(
            cache,
            mockk<ApiProvider.Factory> {
                every {
                    createApiProvider<OcsAPI>(any(), any(), any(), any())
                } returns getApiProviderMock()
            },
            ExecutorServiceFactory {
                MoreExecutors.newDirectExecutorService()
            }
        )
    }

    @After
    fun teardown() {
        actualUsers.clear()
        cache.clear()
    }

    @Test
    fun fetchDisplayNames() {
        val potentialUserNames = setOf("foo", "bar", "baz", "qux", "quux")

        val displayNames = util.fetchDisplayNames(
            ApplicationProvider.getApplicationContext(),
            ssoAccount,
            potentialUserNames
        )

        assertEquals("Foo Bidoo", displayNames["foo"])
        assertEquals("Bar Iton", displayNames["bar"])
        assertEquals("Baz Lightyear", displayNames["baz"])

        assertTrue(cache.isKnownValidUserId(ssoAccount, "foo"))
        assertTrue(
            "Should add the username to cache to avoid querying it the next time",
            cache.isKnownValidUserId(ssoAccount, "bar")
        )
        assertTrue(
            "Should add the username to cache to avoid querying it the next time",
            cache.isKnownValidUserId(ssoAccount, "baz")
        )

        assertTrue(cache.isKnownInvalidUserId(ssoAccount, "qux"))
        assertTrue(
            "Should add the invalid username to cache to avoid querying it the next time",
            cache.isKnownInvalidUserId(ssoAccount, "quux")
        )
    }

    private fun getApiProviderMock(): ApiProvider<OcsAPI> {
        return mockk<ApiProvider<OcsAPI>> {
            every { getApi() } returns getOcsApiMock()
            every { close() } answers { }
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
                            val displayName = actualUsers[userName]
                            val data = OcsUser(
                                false,
                                null,
                                0,
                                null,
                                null,
                                displayName,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                            )
                            val ocs = OcsResponse.OcsWrapper(null, data)
                            OcsResponse(ocs)
                        }
                    }
                }
            }
        }
    }
}