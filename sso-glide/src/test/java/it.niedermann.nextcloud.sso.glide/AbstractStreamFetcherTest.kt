package it.niedermann.nextcloud.sso.glide

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.bumptech.glide.Priority
import com.bumptech.glide.load.data.DataFetcher
import com.nextcloud.android.sso.api.NextcloudAPI
import com.nextcloud.android.sso.api.Response
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
import org.robolectric.annotation.Config
import java.io.InputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
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
    fun `Happy path - Given an absolute SingleSignOnUrl`() {
        val fetcher = object : AbstractStreamFetcher<SingleSignOnUrl>(
            ApplicationProvider.getApplicationContext(),
            SingleSignOnUrl(ssoAccount, "https://nc.example.com/avatar?width=15&height=16"),
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
    fun `Happy path - Given an absolute String URL`() {
        val fetcher = object : AbstractStreamFetcher<String>(
            ApplicationProvider.getApplicationContext(),
            "https://nc.example.com/avatar?width=15&height=16",
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
                assertEquals(2, it.parameter.size)
                assertEquals("15", it.parameter["width"])
                assertEquals("16", it.parameter["height"])
            })
        }
        verify(exactly = 1) { callback.onDataReady(any()) }
        verify(exactly = 0) { callback.onLoadFailed(any()) }
    }

    @Test
    fun `Happy path - Given a relative SingleSignOnUrl`() {
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
    fun `Happy path - Given a relative String URL`() {
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
}