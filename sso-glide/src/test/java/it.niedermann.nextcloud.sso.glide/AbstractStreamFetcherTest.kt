package it.niedermann.nextcloud.sso.glide

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.bumptech.glide.Priority
import com.bumptech.glide.load.data.DataFetcher
import com.nextcloud.android.sso.api.NextcloudAPI
import com.nextcloud.android.sso.api.Response
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException
import com.nextcloud.android.sso.exceptions.TokenMismatchException
import com.nextcloud.android.sso.model.SingleSignOnAccount
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.InputStream

@RunWith(RobolectricTestRunner::class)
class AbstractStreamFetcherTest {
    private val ssoAccount = SingleSignOnAccount("Test", "test", "", "https://nc.example.com", "")
    private val callback = mockk<DataFetcher.DataCallback<in InputStream?>>(relaxed = true)
    private val api = mockk<NextcloudAPI>(relaxed = true)
    private val apiFactory = mockk<AbstractStreamFetcher.ApiFactory>()

    @Before
    fun setup() {
        clearMocks(callback, api, apiFactory)
        every { api.performNetworkRequestV2(any()) } returns Response(mockk(), mockk())
        every { apiFactory.build(any(), any(), any(), any()) } returns api
        AbstractStreamFetcher.Companion.resetInitializedApis()
    }

    @Test
    fun `Happy path - Given an absolute SingleSignOnUrl with one query param`() {
        val fetcher = object : AbstractStreamFetcher<SingleSignOnUrl>(
            ApplicationProvider.getApplicationContext(),
            SingleSignOnUrl(ssoAccount, "https://nc.example.com/avatar?width=15"),
            apiFactory
        ) {
            override fun getSingleSignOnAccount(
                context: Context,
                model: SingleSignOnUrl
            ): SingleSignOnAccount {
                return ssoAccount
            }
        }
        fetcher.loadData(Priority.NORMAL, callback)

        verify(exactly = 1) {
            api.performNetworkRequestV2(withArg {
                assertEquals("GET", it.method)
                assertEquals("/avatar", it.url)
                assertEquals(1, it.parameter.size)
                assertEquals("15", it.parameter["width"])
            })
        }
        verify(exactly = 1) { callback.onDataReady(any()) }
        verify(exactly = 0) { callback.onLoadFailed(any()) }
    }

    @Test
    fun `Happy path - Given an absolute String URL without query params`() {
        val fetcher = object : AbstractStreamFetcher<String>(
            ApplicationProvider.getApplicationContext(),
            "https://nc.example.com/avatar?",
            apiFactory
        ) {
            override fun getSingleSignOnAccount(
                context: Context,
                model: String
            ): SingleSignOnAccount {
                return ssoAccount
            }
        }
        fetcher.loadData(Priority.NORMAL, callback)

        verify(exactly = 1) {
            api.performNetworkRequestV2(withArg {
                assertEquals("GET", it.method)
                assertEquals("/avatar", it.url)
                assertEquals(0, it.parameter.size)
            })
        }
        verify(exactly = 1) { callback.onDataReady(any()) }
        verify(exactly = 0) { callback.onLoadFailed(any()) }
    }

    @Test
    fun `Happy path - Given a relative SingleSignOnUrl with two query params`() {
        val fetcher = object : AbstractStreamFetcher<SingleSignOnUrl>(
            ApplicationProvider.getApplicationContext(),
            SingleSignOnUrl(ssoAccount, "/avatar?width=15&height=16"),
            apiFactory
        ) {
            override fun getSingleSignOnAccount(
                context: Context,
                model: SingleSignOnUrl
            ): SingleSignOnAccount {
                return ssoAccount
            }
        }
        fetcher.loadData(Priority.NORMAL, callback)

        verify(exactly = 1) {
            api.performNetworkRequestV2(withArg {
                assertEquals("GET", it.method)
                assertEquals("/avatar", it.url)
                assertEquals(2, it.parameter.size)
                assertEquals("15", it.parameter["width"])
                assertEquals("16", it.parameter["height"])
            })
        }
        verify(exactly = 1) { callback.onDataReady(any()) }
        verify(exactly = 0) { callback.onLoadFailed(any()) }
    }

    @Test
    fun `Happy path - Given a relative String URL with one query param and a trailing ampersand`() {
        val fetcher = object : AbstractStreamFetcher<String>(
            ApplicationProvider.getApplicationContext(),
            "/avatar?width=15&",
            apiFactory
        ) {
            override fun getSingleSignOnAccount(
                context: Context,
                model: String
            ): SingleSignOnAccount {
                return ssoAccount
            }
        }
        fetcher.loadData(Priority.NORMAL, callback)

        verify(exactly = 1) {
            api.performNetworkRequestV2(withArg {
                assertEquals("GET", it.method)
                assertEquals("/avatar", it.url)
                assertEquals(1, it.parameter.size)
                assertEquals("15", it.parameter["width"])
            })
        }
        verify(exactly = 1) { callback.onDataReady(any()) }
        verify(exactly = 0) { callback.onLoadFailed(any()) }
    }

    @Test
    fun `Happy path - Given an absolute SingleSignOnUrl with a file ID`() {
        val fetcher = object : AbstractStreamFetcher<SingleSignOnUrl>(
            ApplicationProvider.getApplicationContext(),
            SingleSignOnUrl(ssoAccount, "https://nc.example.com/f/4711"),
            apiFactory
        ) {
            override fun getSingleSignOnAccount(
                context: Context,
                model: SingleSignOnUrl
            ): SingleSignOnAccount {
                return ssoAccount
            }
        }
        fetcher.loadData(Priority.NORMAL, callback)

        verify(exactly = 1) {
            api.performNetworkRequestV2(withArg {
                assertEquals("GET", it.method)
                assertEquals("/index.php/core/preview", it.url)
                assertEquals(4, it.parameter.size)
                assertEquals("true", it.parameter["a"])
                assertEquals("4711", it.parameter["fileId"])
            })
        }
        verify(exactly = 1) { callback.onDataReady(any()) }
        verify(exactly = 0) { callback.onLoadFailed(any()) }
    }

    @Test
    fun `Happy path - Given a relative String fileId URL`() {
        val fetcher = object : AbstractStreamFetcher<String>(
            ApplicationProvider.getApplicationContext(),
            "/f/123456",
            apiFactory
        ) {
            override fun getSingleSignOnAccount(
                context: Context,
                model: String
            ): SingleSignOnAccount {
                return ssoAccount
            }
        }
        fetcher.loadData(Priority.NORMAL, callback)

        verify(exactly = 1) {
            api.performNetworkRequestV2(withArg {
                assertEquals("GET", it.method)
                assertEquals("/index.php/core/preview", it.url)
                assertEquals(4, it.parameter.size)
                assertEquals("123456", it.parameter["fileId"])
                assertEquals("true", it.parameter["a"])
            })
        }
        verify(exactly = 1) { callback.onDataReady(any()) }
        verify(exactly = 0) { callback.onLoadFailed(any()) }
    }

    // ---------------

    @Test
    fun `Happy path - Given an absolute SingleSignOnUrl with a share ID`() {
        val fetcher = object : AbstractStreamFetcher<SingleSignOnUrl>(
            ApplicationProvider.getApplicationContext(),
            SingleSignOnUrl(ssoAccount, "https://nc.example.com/index.php/s/DQzqy7zEB4abqEb"),
            apiFactory
        ) {
            override fun getSingleSignOnAccount(
                context: Context,
                model: SingleSignOnUrl
            ): SingleSignOnAccount {
                return ssoAccount
            }
        }
        fetcher.loadData(Priority.NORMAL, callback)

        verify(exactly = 1) {
            api.performNetworkRequestV2(withArg {
                assertEquals("GET", it.method)
                assertEquals("/index.php/s/DQzqy7zEB4abqEb/download", it.url)
                assertEquals(0, it.parameter.size)
            })
        }
        verify(exactly = 1) { callback.onDataReady(any()) }
        verify(exactly = 0) { callback.onLoadFailed(any()) }
    }

    @Test
    fun `Happy path - Given a relative String shareId URL`() {
        val fetcher = object : AbstractStreamFetcher<String>(
            ApplicationProvider.getApplicationContext(),
            "/s/DQzqy7zEB4abqEb/",
            apiFactory
        ) {
            override fun getSingleSignOnAccount(
                context: Context,
                model: String
            ): SingleSignOnAccount {
                return ssoAccount
            }
        }
        fetcher.loadData(Priority.NORMAL, callback)

        verify(exactly = 1) {
            api.performNetworkRequestV2(withArg {
                assertEquals("GET", it.method)
                assertEquals("/index.php/s/DQzqy7zEB4abqEb/download", it.url)
                assertEquals(0, it.parameter.size)
            })
        }
        verify(exactly = 1) { callback.onDataReady(any()) }
        verify(exactly = 0) { callback.onLoadFailed(any()) }
    }

    @Test
    fun `Given the absolute SingleSignOnUrl does not match the SingleSignOnAccount, an IllegalArgumentException should be passed to the callback`() {
        val fetcher = object : AbstractStreamFetcher<SingleSignOnUrl>(
            ApplicationProvider.getApplicationContext(),
            SingleSignOnUrl(ssoAccount, "https://example.com/nextcloud/avatar?width=15&height=16"),
            apiFactory
        ) {
            override fun getSingleSignOnAccount(
                context: Context,
                model: SingleSignOnUrl
            ): SingleSignOnAccount {
                return ssoAccount
            }
        }
        fetcher.loadData(Priority.NORMAL, callback)

        verify(exactly = 0) { api.performNetworkRequestV2(any()) }
        verify(exactly = 0) { callback.onDataReady(any()) }
        verify(exactly = 1) { callback.onLoadFailed(withArg { it is IllegalArgumentException }) }
    }

    @Test
    fun `Given the absolute String URL does not match the SingleSignOnAccount, an IllegalArgumentException should be passed to the callback`() {
        val fetcher = object : AbstractStreamFetcher<String>(
            ApplicationProvider.getApplicationContext(),
            "https://example.com/nextcloud/avatar?width=15&height=16",
            apiFactory
        ) {
            override fun getSingleSignOnAccount(
                context: Context,
                model: String
            ): SingleSignOnAccount {
                return ssoAccount
            }
        }
        fetcher.loadData(Priority.NORMAL, callback)

        verify(exactly = 0) { api.performNetworkRequestV2(any()) }
        verify(exactly = 0) { callback.onDataReady(any()) }
        verify(exactly = 1) { callback.onLoadFailed(withArg { it is IllegalArgumentException }) }
    }

    @Test
    fun `Given the SingleSignOnUrl is neither relative nor absolute, an IllegalArgumentException should be passed to the callback`() {
        val fetcher = object : AbstractStreamFetcher<SingleSignOnUrl>(
            ApplicationProvider.getApplicationContext(),
            SingleSignOnUrl(ssoAccount, "avatar?width=15&height=16"),
            apiFactory
        ) {
            override fun getSingleSignOnAccount(
                context: Context,
                model: SingleSignOnUrl
            ): SingleSignOnAccount {
                return ssoAccount
            }
        }
        fetcher.loadData(Priority.NORMAL, callback)

        verify(exactly = 0) { api.performNetworkRequestV2(any()) }
        verify(exactly = 0) { callback.onDataReady(any()) }
        verify(exactly = 1) { callback.onLoadFailed(withArg { it is IllegalArgumentException }) }
    }

    @Test
    fun `Given the String URL is neither relative nor absolute, an IllegalArgumentException should be passed to the callback`() {
        val fetcher = object : AbstractStreamFetcher<String>(
            ApplicationProvider.getApplicationContext(),
            "avatar?width=15&height=16",
            apiFactory
        ) {
            override fun getSingleSignOnAccount(
                context: Context,
                model: String
            ): SingleSignOnAccount {
                return ssoAccount
            }
        }
        fetcher.loadData(Priority.NORMAL, callback)

        verify(exactly = 0) { api.performNetworkRequestV2(any()) }
        verify(exactly = 0) { callback.onDataReady(any()) }
        verify(exactly = 1) { callback.onLoadFailed(withArg { it is IllegalArgumentException }) }
    }

    @Test
    fun `Given getting the SingleSignOnAccount throws an exception, it should be passed to the callback`() {
        val fetcher = object : AbstractStreamFetcher<String>(
            ApplicationProvider.getApplicationContext(),
            "avatar?width=15&height=16",
            apiFactory
        ) {
            override fun getSingleSignOnAccount(
                context: Context,
                model: String
            ): SingleSignOnAccount {
                throw NextcloudFilesAppAccountNotFoundException()
            }
        }
        fetcher.loadData(Priority.NORMAL, callback)

        verify(exactly = 0) { api.performNetworkRequestV2(any()) }
        verify(exactly = 0) { callback.onDataReady(any()) }
        verify(exactly = 1) { callback.onLoadFailed(withArg { it is NextcloudFilesAppAccountNotFoundException }) }
    }

    @Test
    fun `Given a TokenMismatchException is thrown while performing the request directly after API creation, it should reset this API instance and call the error handler of the callback`() {
        every { api.performNetworkRequestV2(any()) } throws TokenMismatchException()

        val fetcher = object : AbstractStreamFetcher<String>(
            ApplicationProvider.getApplicationContext(),
            "/avatar?width=15&height=16",
            apiFactory
        ) {
            override fun getSingleSignOnAccount(
                context: Context,
                model: String
            ): SingleSignOnAccount {
                return ssoAccount
            }
        }
        fetcher.loadData(Priority.NORMAL, callback)

        verify(exactly = 1) { api.performNetworkRequestV2(any()) }
        verify(exactly = 1) { api.stop() }
        verify(exactly = 0) { callback.onDataReady(any()) }
        verify(exactly = 1) { callback.onLoadFailed(withArg { it is TokenMismatchException }) }
    }

    @Test
    fun `Given a TokenMismatchException is thrown while performing the request after a successful request, it should retry with a new instance and then call the error handler of the callback`() {
        val recreatedApi = mockk<NextcloudAPI>(relaxed = true)
        every { api.performNetworkRequestV2(any()) } returns Response(
            mockk(),
            mockk()
        ) andThenThrows TokenMismatchException()
        every { recreatedApi.performNetworkRequestV2(any()) } throws TokenMismatchException()
        every { apiFactory.build(any(), any(), any(), any()) } returnsMany listOf(api, recreatedApi)

        val fetcher = object : AbstractStreamFetcher<String>(
            ApplicationProvider.getApplicationContext(),
            "/avatar?width=15&height=16",
            apiFactory
        ) {
            override fun getSingleSignOnAccount(
                context: Context,
                model: String
            ): SingleSignOnAccount {
                return ssoAccount
            }
        }
        fetcher.loadData(Priority.NORMAL, callback) // Successful call
        fetcher.loadData(Priority.NORMAL, callback) // TokenMismatchException

        verify(exactly = 2) { api.performNetworkRequestV2(any()) } // First call is successful, second fails
        verify(exactly = 1) { callback.onDataReady(any()) } // The very first call was successful
        verify(exactly = 1) { api.stop() } // after failing call, clear the API instance. It worked before, so we will try to create a new API before giving up
        verify(exactly = 1) { recreatedApi.performNetworkRequestV2(any()) } // First call to the new API also fails
        verify(exactly = 1) { recreatedApi.stop() } // So we clear also this API instance. This instance did never work, so we don't even try to create a new API but
        verify(exactly = 1) { callback.onLoadFailed(withArg { it is TokenMismatchException }) } // fail now
    }
}