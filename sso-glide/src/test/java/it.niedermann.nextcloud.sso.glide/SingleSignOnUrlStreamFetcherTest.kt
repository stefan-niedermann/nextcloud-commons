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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.InputStream

@RunWith(RobolectricTestRunner::class)
class SingleSignOnUrlStreamFetcherTest {
    private val ssoAccountRoot =
        SingleSignOnAccount("NextcloudOnRoot", "ncroot", "", "https://nc.example.com", "")
    private val ssoAccountSubDir =
        SingleSignOnAccount("NextcloudOnSubDir", "ncsubdir", "", "https://example.com/nc", "")

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

    /**
     * Helper method which generates a new [AbstractStreamFetcher<SingleSingOnUrl>] and loads the given [url].
     */
    private fun load(url: SingleSignOnUrl) {
        val fetcher = object : AbstractStreamFetcher<SingleSignOnUrl>(
            ApplicationProvider.getApplicationContext(),
            url,
            apiFactory
        ) {
            override fun getSingleSignOnAccount(
                context: Context,
                model: SingleSignOnUrl
            ): SingleSignOnAccount {
                return when (url.ssoAccountName) {
                    ssoAccountRoot.name -> ssoAccountRoot
                    ssoAccountSubDir.name -> ssoAccountSubDir
                    else -> {
                        throw IllegalArgumentException("Unknown test account ${url.ssoAccountName}")
                    }
                }
            }
        }
        fetcher.loadData(Priority.NORMAL, callback)
    }

    /**
     * Verifies, that the [callback] has been successfully responded and no error occured.
     */
    private fun verifySuccess(success: Boolean = true) {
        if (success) {
            verify(exactly = 1) { callback.onDataReady(any()) }
            verify(exactly = 0) { callback.onLoadFailed(any()) }
        } else {
            verify(exactly = 0) { callback.onDataReady(any()) }
            verify(exactly = 1) { callback.onLoadFailed(any()) }
        }
    }

    private fun verifyCall(
        path: String,
        parameters: Map<String, String> = emptyMap(),
        mandatoryParameters: List<String> = emptyList()
    ) {
        verifySuccess()
        verify(exactly = 1) {
            api.performNetworkRequestV2(withArg {
                assertEquals("GET", it.method)
                assertEquals(path, it.url)
                assertTrue(it.parameterV2.size >= parameters.size)
                for ((k, v) in parameters) {
                    assertEquals(v, it.parameterV2.first { qp -> qp.key == k }.value)
                }
                assertTrue(it.parameterV2.size >= mandatoryParameters.size)
                for (p in mandatoryParameters) {
                    assertTrue(it.parameterV2.any { qp -> qp.key == p })
                }
            })
        }
    }

    // ###########################################################
    // Nextcloud on Root
    // ###########################################################

    // ===========================================================
    // Nextcloud on Root / Full URL
    // ===========================================================

    // -----------------------------------------------------------
    // Nextcloud on Root / Full URL / File ID
    // -----------------------------------------------------------

    @Test
    fun `Root - Full URL - File ID - no index, no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/f/123456"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `Root - Full URL - File ID - no index, trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/f/123456/"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `Root - Full URL - File ID - index, no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/f/123456"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `Root - Full URL - File ID - index, trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/f/123456/"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    // Empty query parameter

    @Test
    fun `Root - Full URL - File ID - no index, no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/f/123456?"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `Root - Full URL - File ID - no index, trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/f/123456/?"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `Root - Full URL - File ID - index, no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/f/123456?"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `Root - Full URL - File ID - index, trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/f/123456/?"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    // One query parameter

    @Test
    fun `Root - Full URL - File ID - no index, no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/f/123456?foo=bar"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true", "foo" to "bar"),
            listOf("x", "y")
        )
    }

    @Test
    fun `Root - Full URL - File ID - no index, trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/f/123456/?foo=bar"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true", "foo" to "bar"),
            listOf("x", "y")
        )
    }

    @Test
    fun `Root - Full URL - File ID - index, no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/f/123456?foo=bar"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true", "foo" to "bar"),
            listOf("x", "y")
        )
    }

    @Test
    fun `Root - Full URL - File ID - index, trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/f/123456/?foo=bar"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true", "foo" to "bar"),
            listOf("x", "y")
        )
    }

    // -----------------------------------------------------------
    // Nextcloud on Root / Full URL / Share ID
    // -----------------------------------------------------------

    @Test
    fun `Root - Full URL - Share ID - no index, no trailing slash, no download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/s/Wr99tjfBCs96kxZ"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Full URL - Share ID - no index, trailing slash, no download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/s/Wr99tjfBCs96kxZ/"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Full URL - Share ID - no index, no trailing slash, download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/s/Wr99tjfBCs96kxZ/download"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Full URL - Share ID - no index, trailing slash, download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/s/Wr99tjfBCs96kxZ/download/"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Full URL - Share ID - index, no trailing slash, no download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/s/Wr99tjfBCs96kxZ"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Full URL - Share ID - index, trailing slash, no download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/s/Wr99tjfBCs96kxZ/"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Full URL - Share ID - index, no trailing slash, download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/s/Wr99tjfBCs96kxZ/download"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Full URL - Share ID - index, trailing slash, download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/s/Wr99tjfBCs96kxZ/download/"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    // Empty query parameter

    @Test
    fun `Root - Full URL - Share ID - no index, no trailing slash, no download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/s/Wr99tjfBCs96kxZ?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Full URL - Share ID - no index, trailing slash, no download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/s/Wr99tjfBCs96kxZ/?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Full URL - Share ID - no index, no trailing slash, download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/s/Wr99tjfBCs96kxZ/download?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Full URL - Share ID - no index, trailing slash, download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/s/Wr99tjfBCs96kxZ/download/?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Full URL - Share ID - index, no trailing slash, no download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/s/Wr99tjfBCs96kxZ?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Full URL - Share ID - index, trailing slash, no download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/s/Wr99tjfBCs96kxZ/?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Full URL - Share ID - index, no trailing slash, download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/s/Wr99tjfBCs96kxZ/download?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Full URL - Share ID - index, trailing slash, download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/s/Wr99tjfBCs96kxZ/download/?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    // One query parameter

    @Test
    fun `Root - Full URL - Share ID - no index, no trailing slash, no download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/s/Wr99tjfBCs96kxZ?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `Root - Full URL - Share ID - no index, trailing slash, no download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/s/Wr99tjfBCs96kxZ/?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `Root - Full URL - Share ID - no index, no trailing slash, download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/s/Wr99tjfBCs96kxZ/download?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `Root - Full URL - Share ID - no index, trailing slash, download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/s/Wr99tjfBCs96kxZ/download/?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `Root - Full URL - Share ID - index, no trailing slash, no download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/s/Wr99tjfBCs96kxZ?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `Root - Full URL - Share ID - index, trailing slash, no download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/s/Wr99tjfBCs96kxZ/?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `Root - Full URL - Share ID - index, no trailing slash, download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/s/Wr99tjfBCs96kxZ/download?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `Root - Full URL - Share ID - index, trailing slash, download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/s/Wr99tjfBCs96kxZ/download/?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    // -----------------------------------------------------------
    // Nextcloud on Root / Full URL / Avatar
    // -----------------------------------------------------------

    @Test
    fun `Root - Full URL - Avatar - no index, no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/avatar/my-user/32"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `Root - Full URL - Avatar - no index, trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/avatar/my-user/32/"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `Root - Full URL - Avatar - index, no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/avatar/my-user/32"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `Root - Full URL - Avatar - index, trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/avatar/my-user/32/"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    // Empty query parameter

    @Test
    fun `Root - Full URL - Avatar - no index, no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/avatar/my-user/32?"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `Root - Full URL - Avatar - no index, trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/avatar/my-user/32/?"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `Root - Full URL - Avatar - index, no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/avatar/my-user/32?"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `Root - Full URL - Avatar - index, trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/avatar/my-user/32/?"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    // One query parameter

    @Test
    fun `Root - Full URL - Avatar - no index, no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/avatar/my-user/32?foo=bar"))
        verifyCall("/index.php/avatar/my-user/32", mapOf("foo" to "bar"))
    }

    @Test
    fun `Root - Full URL - Avatar - no index, trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/avatar/my-user/32/?foo=bar"))
        verifyCall("/index.php/avatar/my-user/32", mapOf("foo" to "bar"))
    }

    @Test
    fun `Root - Full URL - Avatar - index, no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/avatar/my-user/32?foo=bar"))
        verifyCall("/index.php/avatar/my-user/32", mapOf("foo" to "bar"))
    }

    @Test
    fun `Root - Full URL - Avatar - index, trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/avatar/my-user/32/?foo=bar"))
        verifyCall("/index.php/avatar/my-user/32", mapOf("foo" to "bar"))
    }

    // -----------------------------------------------------------
    // Nextcloud on Root / Full URL / WebDAV
    // -----------------------------------------------------------

    @Test
    fun `Root - Full URL - WebDAV - no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/foo/bar.png"))
        verifyCall("/remote.php/webdav/foo/bar.png")
    }

    @Test
    fun `Root - Full URL - WebDAV - trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/foo/bar.png/"))
        verifyCall("/remote.php/webdav/foo/bar.png/")
    }

    // Empty query parameter

    @Test
    fun `Root - Full URL - WebDAV - no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/foo/bar.png?"))
        verifyCall("/remote.php/webdav/foo/bar.png")
    }

    @Test
    fun `Root - Full URL - WebDAV - trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/foo/bar.png/?"))
        verifyCall("/remote.php/webdav/foo/bar.png/")
    }

    // One query parameter

    @Test
    fun `Root - Full URL - WebDAV - no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/foo/bar.png?baz=qux"))
        verifyCall("/remote.php/webdav/foo/bar.png", mapOf("baz" to "qux"))
    }

    @Test
    fun `Root - Full URL - WebDAV - trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/foo/bar.png/?baz=qux"))
        verifyCall("/remote.php/webdav/foo/bar.png/", mapOf("baz" to "qux"))
    }

    // -----------------------------------------------------------
    // Nextcloud on Root / Full URL / remote.php and index.php
    // -----------------------------------------------------------

    @Test
    fun `Root - Full URL - remote endpoints - no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/remote.php/foo/bar.png"))
        verifyCall("/remote.php/foo/bar.png")
    }

    @Test
    fun `Root - Full URL - remote endpoints - trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/remote.php/foo/bar.png/"))
        verifyCall("/remote.php/foo/bar.png/")
    }

    // Empty query parameter

    @Test
    fun `Root - Full URL - remote endpoints - no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/remote.php/foo/bar.png?"))
        verifyCall("/remote.php/foo/bar.png")
    }

    @Test
    fun `Root - Full URL - remote endpoints - trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/remote.php/foo/bar.png/?"))
        verifyCall("/remote.php/foo/bar.png/")
    }

    // One query parameter

    @Test
    fun `Root - Full URL - remote endpoints - no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/remote.php/foo/bar.png?baz=qux"))
        verifyCall("/remote.php/foo/bar.png", mapOf("baz" to "qux"))
    }

    @Test
    fun `Root - Full URL - remote endpoints - trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/remote.php/foo/bar.png/?baz=qux"))
        verifyCall("/remote.php/foo/bar.png/", mapOf("baz" to "qux"))
    }

    // index endpoints

    @Test
    fun `Root - Full URL - index endpoints - no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/foo/bar.png"))
        verifyCall("/index.php/foo/bar.png")
    }

    @Test
    fun `Root - Full URL - index endpoints - trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/foo/bar.png/"))
        verifyCall("/index.php/foo/bar.png/")
    }

    // Empty query parameter

    @Test
    fun `Root - Full URL - index endpoints - no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/foo/bar.png?"))
        verifyCall("/index.php/foo/bar.png")
    }

    @Test
    fun `Root - Full URL - index endpoints - trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/foo/bar.png/?"))
        verifyCall("/index.php/foo/bar.png/")
    }

    // One query parameter

    @Test
    fun `Root - Full URL - index endpoints - no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/foo/bar.png?baz=qux"))
        verifyCall("/index.php/foo/bar.png", mapOf("baz" to "qux"))
    }

    @Test
    fun `Root - Full URL - index endpoints - trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://nc.example.com/index.php/foo/bar.png/?baz=qux"))
        verifyCall("/index.php/foo/bar.png/", mapOf("baz" to "qux"))
    }

    // ===========================================================
    // Nextcloud on Root / Absolute path
    // ===========================================================

    // -----------------------------------------------------------
    // Nextcloud on Root / Absolute path / File ID
    // -----------------------------------------------------------

    @Test
    fun `Root - Absolute path - File ID - no index, no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/f/123456"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `Root - Absolute path - File ID - no index, trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/f/123456/"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `Root - Absolute path - File ID - index, no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/f/123456"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `Root - Absolute path - File ID - index, trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/f/123456/"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    // Empty query parameter

    @Test
    fun `Root - Absolute path - File ID - no index, no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/f/123456?"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `Root - Absolute path - File ID - no index, trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/f/123456/?"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `Root - Absolute path - File ID - index, no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/f/123456?"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `Root - Absolute path - File ID - index, trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/f/123456/?"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    // One query parameter

    @Test
    fun `Root - Absolute path - File ID - no index, no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/f/123456?foo=bar"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true", "foo" to "bar"),
            listOf("x", "y")
        )
    }

    @Test
    fun `Root - Absolute path - File ID - no index, trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/f/123456/?foo=bar"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true", "foo" to "bar"),
            listOf("x", "y")
        )
    }

    @Test
    fun `Root - Absolute path - File ID - index, no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/f/123456?foo=bar"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true", "foo" to "bar"),
            listOf("x", "y")
        )
    }

    @Test
    fun `Root - Absolute path - File ID - index, trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/f/123456/?foo=bar"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true", "foo" to "bar"),
            listOf("x", "y")
        )
    }

    // -----------------------------------------------------------
    // Nextcloud on Root / Absolute path / Share ID
    // -----------------------------------------------------------

    @Test
    fun `Root - Absolute path - Share ID - no index, no trailing slash, no download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/s/Wr99tjfBCs96kxZ"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Absolute path - Share ID - no index, trailing slash, no download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/s/Wr99tjfBCs96kxZ/"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Absolute path - Share ID - no index, no trailing slash, download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/s/Wr99tjfBCs96kxZ/download"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Absolute path - Share ID - no index, trailing slash, download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/s/Wr99tjfBCs96kxZ/download/"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Absolute path - Share ID - index, no trailing slash, no download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/s/Wr99tjfBCs96kxZ"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Absolute path - Share ID - index, trailing slash, no download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/s/Wr99tjfBCs96kxZ/"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Absolute path - Share ID - index, no trailing slash, download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/s/Wr99tjfBCs96kxZ/download"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Absolute path - Share ID - index, trailing slash, download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/s/Wr99tjfBCs96kxZ/download/"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    // Empty query parameter

    @Test
    fun `Root - Absolute path - Share ID - no index, no trailing slash, no download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/s/Wr99tjfBCs96kxZ?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Absolute path - Share ID - no index, trailing slash, no download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/s/Wr99tjfBCs96kxZ/?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Absolute path - Share ID - no index, no trailing slash, download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/s/Wr99tjfBCs96kxZ/download?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Absolute path - Share ID - no index, trailing slash, download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/s/Wr99tjfBCs96kxZ/download/?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Absolute path - Share ID - index, no trailing slash, no download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/s/Wr99tjfBCs96kxZ?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Absolute path - Share ID - index, trailing slash, no download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/s/Wr99tjfBCs96kxZ/?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Absolute path - Share ID - index, no trailing slash, download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/s/Wr99tjfBCs96kxZ/download?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `Root - Absolute path - Share ID - index, trailing slash, download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/s/Wr99tjfBCs96kxZ/download/?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    // One query parameter

    @Test
    fun `Root - Absolute path - Share ID - no index, no trailing slash, no download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/s/Wr99tjfBCs96kxZ?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `Root - Absolute path - Share ID - no index, trailing slash, no download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/s/Wr99tjfBCs96kxZ/?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `Root - Absolute path - Share ID - no index, no trailing slash, download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/s/Wr99tjfBCs96kxZ/download?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `Root - Absolute path - Share ID - no index, trailing slash, download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/s/Wr99tjfBCs96kxZ/download/?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `Root - Absolute path - Share ID - index, no trailing slash, no download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/s/Wr99tjfBCs96kxZ?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `Root - Absolute path - Share ID - index, trailing slash, no download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/s/Wr99tjfBCs96kxZ/?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `Root - Absolute path - Share ID - index, no trailing slash, download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/s/Wr99tjfBCs96kxZ/download?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `Root - Absolute path - Share ID - index, trailing slash, download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/s/Wr99tjfBCs96kxZ/download/?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    // -----------------------------------------------------------
    // Nextcloud on Root / Absolute path / Avatar
    // -----------------------------------------------------------

    @Test
    fun `Root - Absolute path - Avatar - no index, no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/avatar/my-user/32"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `Root - Absolute path - Avatar - no index, trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/avatar/my-user/32/"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `Root - Absolute path - Avatar - index, no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/avatar/my-user/32"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `Root - Absolute path - Avatar - index, trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/avatar/my-user/32/"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    // Empty query parameter

    @Test
    fun `Root - Absolute path - Avatar - no index, no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/avatar/my-user/32?"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `Root - Absolute path - Avatar - no index, trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/avatar/my-user/32/?"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `Root - Absolute path - Avatar - index, no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/avatar/my-user/32?"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `Root - Absolute path - Avatar - index, trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/avatar/my-user/32/?"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    // One query parameter

    @Test
    fun `Root - Absolute path - Avatar - no index, no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/avatar/my-user/32?foo=bar"))
        verifyCall("/index.php/avatar/my-user/32", mapOf("foo" to "bar"))
    }

    @Test
    fun `Root - Absolute path - Avatar - no index, trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/avatar/my-user/32/?foo=bar"))
        verifyCall("/index.php/avatar/my-user/32", mapOf("foo" to "bar"))
    }

    @Test
    fun `Root - Absolute path - Avatar - index, no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/avatar/my-user/32?foo=bar"))
        verifyCall("/index.php/avatar/my-user/32", mapOf("foo" to "bar"))
    }

    @Test
    fun `Root - Absolute path - Avatar - index, trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/avatar/my-user/32/?foo=bar"))
        verifyCall("/index.php/avatar/my-user/32", mapOf("foo" to "bar"))
    }

    // -----------------------------------------------------------
    // Nextcloud on Root / Absolute path / WebDAV
    // -----------------------------------------------------------

    @Test
    fun `Root - Absolute path - WebDAV - no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/foo/bar.png"))
        verifyCall("/remote.php/webdav/foo/bar.png")
    }

    @Test
    fun `Root - Absolute path - WebDAV - trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/foo/bar.png/"))
        verifyCall("/remote.php/webdav/foo/bar.png/")
    }

    // Empty query parameter

    @Test
    fun `Root - Absolute path - WebDAV - no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/foo/bar.png?"))
        verifyCall("/remote.php/webdav/foo/bar.png")
    }

    @Test
    fun `Root - Absolute path - WebDAV - trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/foo/bar.png/?"))
        verifyCall("/remote.php/webdav/foo/bar.png/")
    }

    // One query parameter

    @Test
    fun `Root - Absolute path - WebDAV - no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/foo/bar.png?baz=qux"))
        verifyCall("/remote.php/webdav/foo/bar.png", mapOf("baz" to "qux"))
    }

    @Test
    fun `Root - Absolute path - WebDAV - trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/foo/bar.png/?baz=qux"))
        verifyCall("/remote.php/webdav/foo/bar.png/", mapOf("baz" to "qux"))
    }

    // -----------------------------------------------------------
    // Nextcloud on Root / Absolute path / remote.php and index.php
    // -----------------------------------------------------------

    @Test
    fun `Root - Absolute path - remote endpoints - no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/remote.php/foo/bar.png"))
        verifyCall("/remote.php/foo/bar.png")
    }

    @Test
    fun `Root - Absolute path - remote endpoints - trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/remote.php/foo/bar.png/"))
        verifyCall("/remote.php/foo/bar.png/")
    }

    // Empty query parameter

    @Test
    fun `Root - Absolute path - remote endpoints - no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/remote.php/foo/bar.png?"))
        verifyCall("/remote.php/foo/bar.png")
    }

    @Test
    fun `Root - Absolute path - remote endpoints - trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/remote.php/foo/bar.png/?"))
        verifyCall("/remote.php/foo/bar.png/")
    }

    // One query parameter

    @Test
    fun `Root - Absolute path - remote endpoints - no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/remote.php/foo/bar.png?baz=qux"))
        verifyCall("/remote.php/foo/bar.png", mapOf("baz" to "qux"))
    }

    @Test
    fun `Root - Absolute path - remote endpoints - trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/remote.php/foo/bar.png/?baz=qux"))
        verifyCall("/remote.php/foo/bar.png/", mapOf("baz" to "qux"))
    }

    // index endpoints

    @Test
    fun `Root - Absolute path - index endpoints - no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/foo/bar.png"))
        verifyCall("/index.php/foo/bar.png")
    }

    @Test
    fun `Root - Absolute path - index endpoints - trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/foo/bar.png/"))
        verifyCall("/index.php/foo/bar.png/")
    }

    // Empty query parameter

    @Test
    fun `Root - Absolute path - index endpoints - no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/foo/bar.png?"))
        verifyCall("/index.php/foo/bar.png")
    }

    @Test
    fun `Root - Absolute path - index endpoints - trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/foo/bar.png/?"))
        verifyCall("/index.php/foo/bar.png/")
    }

    // One query parameter

    @Test
    fun `Root - Absolute path - index endpoints - no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/foo/bar.png?baz=qux"))
        verifyCall("/index.php/foo/bar.png", mapOf("baz" to "qux"))
    }

    @Test
    fun `Root - Absolute path - index endpoints - trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountRoot, "/index.php/foo/bar.png/?baz=qux"))
        verifyCall("/index.php/foo/bar.png/", mapOf("baz" to "qux"))
    }

    // ###########################################################
    // Nextcloud on SubDir
    // ###########################################################

    // ===========================================================
    // Nextcloud on SubDir / Full URL
    // ===========================================================

    // -----------------------------------------------------------
    // Nextcloud on SubDir / Full URL / File ID
    // -----------------------------------------------------------

    @Test
    fun `SubDir - Full URL - File ID - no index, no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/f/123456"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `SubDir - Full URL - File ID - no index, trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/f/123456/"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `SubDir - Full URL - File ID - index, no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/f/123456"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `SubDir - Full URL - File ID - index, trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/f/123456/"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    // Empty query parameter

    @Test
    fun `SubDir - Full URL - File ID - no index, no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/f/123456?"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `SubDir - Full URL - File ID - no index, trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/f/123456/?"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `SubDir - Full URL - File ID - index, no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/f/123456?"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `SubDir - Full URL - File ID - index, trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/f/123456/?"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    // One query parameter

    @Test
    fun `SubDir - Full URL - File ID - no index, no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/f/123456?foo=bar"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true", "foo" to "bar"),
            listOf("x", "y")
        )
    }

    @Test
    fun `SubDir - Full URL - File ID - no index, trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/f/123456/?foo=bar"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true", "foo" to "bar"),
            listOf("x", "y")
        )
    }

    @Test
    fun `SubDir - Full URL - File ID - index, no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/f/123456?foo=bar"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true", "foo" to "bar"),
            listOf("x", "y")
        )
    }

    @Test
    fun `SubDir - Full URL - File ID - index, trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/f/123456/?foo=bar"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true", "foo" to "bar"),
            listOf("x", "y")
        )
    }

    // -----------------------------------------------------------
    // Nextcloud on SubDir / Full URL / Share ID
    // -----------------------------------------------------------

    @Test
    fun `SubDir - Full URL - Share ID - no index, no trailing slash, no download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/s/Wr99tjfBCs96kxZ"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Full URL - Share ID - no index, trailing slash, no download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/s/Wr99tjfBCs96kxZ/"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Full URL - Share ID - no index, no trailing slash, download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/s/Wr99tjfBCs96kxZ/download"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Full URL - Share ID - no index, trailing slash, download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/s/Wr99tjfBCs96kxZ/download/"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Full URL - Share ID - index, no trailing slash, no download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/s/Wr99tjfBCs96kxZ"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Full URL - Share ID - index, trailing slash, no download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/s/Wr99tjfBCs96kxZ/"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Full URL - Share ID - index, no trailing slash, download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/s/Wr99tjfBCs96kxZ/download"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Full URL - Share ID - index, trailing slash, download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/s/Wr99tjfBCs96kxZ/download/"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    // Empty query parameter

    @Test
    fun `SubDir - Full URL - Share ID - no index, no trailing slash, no download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/s/Wr99tjfBCs96kxZ?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Full URL - Share ID - no index, trailing slash, no download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/s/Wr99tjfBCs96kxZ/?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Full URL - Share ID - no index, no trailing slash, download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/s/Wr99tjfBCs96kxZ/download?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Full URL - Share ID - no index, trailing slash, download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/s/Wr99tjfBCs96kxZ/download/?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Full URL - Share ID - index, no trailing slash, no download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/s/Wr99tjfBCs96kxZ?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Full URL - Share ID - index, trailing slash, no download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/s/Wr99tjfBCs96kxZ/?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Full URL - Share ID - index, no trailing slash, download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/s/Wr99tjfBCs96kxZ/download?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Full URL - Share ID - index, trailing slash, download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/s/Wr99tjfBCs96kxZ/download/?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    // One query parameter

    @Test
    fun `SubDir - Full URL - Share ID - no index, no trailing slash, no download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/s/Wr99tjfBCs96kxZ?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `SubDir - Full URL - Share ID - no index, trailing slash, no download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/s/Wr99tjfBCs96kxZ/?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `SubDir - Full URL - Share ID - no index, no trailing slash, download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/s/Wr99tjfBCs96kxZ/download?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `SubDir - Full URL - Share ID - no index, trailing slash, download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/s/Wr99tjfBCs96kxZ/download/?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `SubDir - Full URL - Share ID - index, no trailing slash, no download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/s/Wr99tjfBCs96kxZ?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `SubDir - Full URL - Share ID - index, trailing slash, no download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/s/Wr99tjfBCs96kxZ/?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `SubDir - Full URL - Share ID - index, no trailing slash, download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/s/Wr99tjfBCs96kxZ/download?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `SubDir - Full URL - Share ID - index, trailing slash, download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/s/Wr99tjfBCs96kxZ/download/?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    // -----------------------------------------------------------
    // Nextcloud on SubDir / Full URL / Avatar
    // -----------------------------------------------------------

    @Test
    fun `SubDir - Full URL - Avatar - no index, no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/avatar/my-user/32"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `SubDir - Full URL - Avatar - no index, trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/avatar/my-user/32/"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `SubDir - Full URL - Avatar - index, no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/avatar/my-user/32"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `SubDir - Full URL - Avatar - index, trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/avatar/my-user/32/"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    // Empty query parameter

    @Test
    fun `SubDir - Full URL - Avatar - no index, no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/avatar/my-user/32?"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `SubDir - Full URL - Avatar - no index, trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/avatar/my-user/32/?"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `SubDir - Full URL - Avatar - index, no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/avatar/my-user/32?"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `SubDir - Full URL - Avatar - index, trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/avatar/my-user/32/?"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    // One query parameter

    @Test
    fun `SubDir - Full URL - Avatar - no index, no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/avatar/my-user/32?foo=bar"))
        verifyCall("/index.php/avatar/my-user/32", mapOf("foo" to "bar"))
    }

    @Test
    fun `SubDir - Full URL - Avatar - no index, trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/avatar/my-user/32/?foo=bar"))
        verifyCall("/index.php/avatar/my-user/32", mapOf("foo" to "bar"))
    }

    @Test
    fun `SubDir - Full URL - Avatar - index, no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/avatar/my-user/32?foo=bar"))
        verifyCall("/index.php/avatar/my-user/32", mapOf("foo" to "bar"))
    }

    @Test
    fun `SubDir - Full URL - Avatar - index, trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/avatar/my-user/32/?foo=bar"))
        verifyCall("/index.php/avatar/my-user/32", mapOf("foo" to "bar"))
    }

    // -----------------------------------------------------------
    // Nextcloud on SubDir / Full URL / WebDAV
    // -----------------------------------------------------------

    @Test
    fun `SubDir - Full URL - WebDAV - no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/foo/bar.png"))
        verifyCall("/remote.php/webdav/foo/bar.png")
    }

    @Test
    fun `SubDir - Full URL - WebDAV - trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/foo/bar.png/"))
        verifyCall("/remote.php/webdav/foo/bar.png/")
    }

    // Empty query parameter

    @Test
    fun `SubDir - Full URL - WebDAV - no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/foo/bar.png?"))
        verifyCall("/remote.php/webdav/foo/bar.png")
    }

    @Test
    fun `SubDir - Full URL - WebDAV - trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/foo/bar.png/?"))
        verifyCall("/remote.php/webdav/foo/bar.png/")
    }

    // One query parameter

    @Test
    fun `SubDir - Full URL - WebDAV - no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/foo/bar.png?baz=qux"))
        verifyCall("/remote.php/webdav/foo/bar.png", mapOf("baz" to "qux"))
    }

    @Test
    fun `SubDir - Full URL - WebDAV - trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/foo/bar.png/?baz=qux"))
        verifyCall("/remote.php/webdav/foo/bar.png/", mapOf("baz" to "qux"))
    }

    // -----------------------------------------------------------
    // Nextcloud on SubDir / Full URL / remote.php and index.php
    // -----------------------------------------------------------

    @Test
    fun `SubDir - Full URL - remote endpoints - no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/remote.php/foo/bar.png"))
        verifyCall("/remote.php/foo/bar.png")
    }

    @Test
    fun `SubDir - Full URL - remote endpoints - trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/remote.php/foo/bar.png/"))
        verifyCall("/remote.php/foo/bar.png/")
    }

    // Empty query parameter

    @Test
    fun `SubDir - Full URL - remote endpoints - no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/remote.php/foo/bar.png?"))
        verifyCall("/remote.php/foo/bar.png")
    }

    @Test
    fun `SubDir - Full URL - remote endpoints - trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/remote.php/foo/bar.png/?"))
        verifyCall("/remote.php/foo/bar.png/")
    }

    // One query parameter

    @Test
    fun `SubDir - Full URL - remote endpoints - no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/remote.php/foo/bar.png?baz=qux"))
        verifyCall("/remote.php/foo/bar.png", mapOf("baz" to "qux"))
    }

    @Test
    fun `SubDir - Full URL - remote endpoints - trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/remote.php/foo/bar.png/?baz=qux"))
        verifyCall("/remote.php/foo/bar.png/", mapOf("baz" to "qux"))
    }

    // index endpoints

    @Test
    fun `SubDir - Full URL - index endpoints - no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/foo/bar.png"))
        verifyCall("/index.php/foo/bar.png")
    }

    @Test
    fun `SubDir - Full URL - index endpoints - trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/foo/bar.png/"))
        verifyCall("/index.php/foo/bar.png/")
    }

    // Empty query parameter

    @Test
    fun `SubDir - Full URL - index endpoints - no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/foo/bar.png?"))
        verifyCall("/index.php/foo/bar.png")
    }

    @Test
    fun `SubDir - Full URL - index endpoints - trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/foo/bar.png/?"))
        verifyCall("/index.php/foo/bar.png/")
    }

    // One query parameter

    @Test
    fun `SubDir - Full URL - index endpoints - no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/foo/bar.png?baz=qux"))
        verifyCall("/index.php/foo/bar.png", mapOf("baz" to "qux"))
    }

    @Test
    fun `SubDir - Full URL - index endpoints - trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "https://example.com/nc/index.php/foo/bar.png/?baz=qux"))
        verifyCall("/index.php/foo/bar.png/", mapOf("baz" to "qux"))
    }

    // ===========================================================
    // Nextcloud on SubDir / Absolute path
    // ===========================================================

    // -----------------------------------------------------------
    // Nextcloud on SubDir / Absolute path / File ID
    // -----------------------------------------------------------

    @Test
    fun `SubDir - Absolute path - File ID - no index, no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/f/123456"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `SubDir - Absolute path - File ID - no index, trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/f/123456/"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `SubDir - Absolute path - File ID - index, no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/f/123456"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `SubDir - Absolute path - File ID - index, trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/f/123456/"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    // Empty query parameter

    @Test
    fun `SubDir - Absolute path - File ID - no index, no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/f/123456?"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `SubDir - Absolute path - File ID - no index, trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/f/123456/?"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `SubDir - Absolute path - File ID - index, no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/f/123456?"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    @Test
    fun `SubDir - Absolute path - File ID - index, trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/f/123456/?"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true"),
            listOf("x", "y")
        )
    }

    // One query parameter

    @Test
    fun `SubDir - Absolute path - File ID - no index, no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/f/123456?foo=bar"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true", "foo" to "bar"),
            listOf("x", "y")
        )
    }

    @Test
    fun `SubDir - Absolute path - File ID - no index, trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/f/123456/?foo=bar"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true", "foo" to "bar"),
            listOf("x", "y")
        )
    }

    @Test
    fun `SubDir - Absolute path - File ID - index, no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/f/123456?foo=bar"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true", "foo" to "bar"),
            listOf("x", "y")
        )
    }

    @Test
    fun `SubDir - Absolute path - File ID - index, trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/f/123456/?foo=bar"))
        verifyCall(
            "/index.php/core/preview", mapOf("fileId" to "123456", "a" to "true", "foo" to "bar"),
            listOf("x", "y")
        )
    }

    // -----------------------------------------------------------
    // Nextcloud on SubDir / Absolute path / Share ID
    // -----------------------------------------------------------

    @Test
    fun `SubDir - Absolute path - Share ID - no index, no trailing slash, no download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/s/Wr99tjfBCs96kxZ"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Absolute path - Share ID - no index, trailing slash, no download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/s/Wr99tjfBCs96kxZ/"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Absolute path - Share ID - no index, no trailing slash, download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/s/Wr99tjfBCs96kxZ/download"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Absolute path - Share ID - no index, trailing slash, download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/s/Wr99tjfBCs96kxZ/download/"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Absolute path - Share ID - index, no trailing slash, no download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/s/Wr99tjfBCs96kxZ"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Absolute path - Share ID - index, trailing slash, no download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/s/Wr99tjfBCs96kxZ/"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Absolute path - Share ID - index, no trailing slash, download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/s/Wr99tjfBCs96kxZ/download"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Absolute path - Share ID - index, trailing slash, download, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/s/Wr99tjfBCs96kxZ/download/"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    // Empty query parameter

    @Test
    fun `SubDir - Absolute path - Share ID - no index, no trailing slash, no download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/s/Wr99tjfBCs96kxZ?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Absolute path - Share ID - no index, trailing slash, no download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/s/Wr99tjfBCs96kxZ/?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Absolute path - Share ID - no index, no trailing slash, download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/s/Wr99tjfBCs96kxZ/download?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Absolute path - Share ID - no index, trailing slash, download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/s/Wr99tjfBCs96kxZ/download/?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Absolute path - Share ID - index, no trailing slash, no download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/s/Wr99tjfBCs96kxZ?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Absolute path - Share ID - index, trailing slash, no download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/s/Wr99tjfBCs96kxZ/?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Absolute path - Share ID - index, no trailing slash, download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/s/Wr99tjfBCs96kxZ/download?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    @Test
    fun `SubDir - Absolute path - Share ID - index, trailing slash, download, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/s/Wr99tjfBCs96kxZ/download/?"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download")
    }

    // One query parameter

    @Test
    fun `SubDir - Absolute path - Share ID - no index, no trailing slash, no download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/s/Wr99tjfBCs96kxZ?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `SubDir - Absolute path - Share ID - no index, trailing slash, no download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/s/Wr99tjfBCs96kxZ/?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `SubDir - Absolute path - Share ID - no index, no trailing slash, download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/s/Wr99tjfBCs96kxZ/download?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `SubDir - Absolute path - Share ID - no index, trailing slash, download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/s/Wr99tjfBCs96kxZ/download/?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `SubDir - Absolute path - Share ID - index, no trailing slash, no download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/s/Wr99tjfBCs96kxZ?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `SubDir - Absolute path - Share ID - index, trailing slash, no download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/s/Wr99tjfBCs96kxZ/?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `SubDir - Absolute path - Share ID - index, no trailing slash, download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/s/Wr99tjfBCs96kxZ/download?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    @Test
    fun `SubDir - Absolute path - Share ID - index, trailing slash, download, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/s/Wr99tjfBCs96kxZ/download/?foo=bar"))
        verifyCall("/index.php/s/Wr99tjfBCs96kxZ/download", mapOf("foo" to "bar"))
    }

    // -----------------------------------------------------------
    // Nextcloud on SubDir / Absolute path / Avatar
    // -----------------------------------------------------------

    @Test
    fun `SubDir - Absolute path - Avatar - no index, no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/avatar/my-user/32"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `SubDir - Absolute path - Avatar - no index, trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/avatar/my-user/32/"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `SubDir - Absolute path - Avatar - index, no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/avatar/my-user/32"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `SubDir - Absolute path - Avatar - index, trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/avatar/my-user/32/"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    // Empty query parameter

    @Test
    fun `SubDir - Absolute path - Avatar - no index, no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/avatar/my-user/32?"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `SubDir - Absolute path - Avatar - no index, trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/avatar/my-user/32/?"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `SubDir - Absolute path - Avatar - index, no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/avatar/my-user/32?"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    @Test
    fun `SubDir - Absolute path - Avatar - index, trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/avatar/my-user/32/?"))
        verifyCall("/index.php/avatar/my-user/32")
    }

    // One query parameter

    @Test
    fun `SubDir - Absolute path - Avatar - no index, no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/avatar/my-user/32?foo=bar"))
        verifyCall("/index.php/avatar/my-user/32", mapOf("foo" to "bar"))
    }

    @Test
    fun `SubDir - Absolute path - Avatar - no index, trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/avatar/my-user/32/?foo=bar"))
        verifyCall("/index.php/avatar/my-user/32", mapOf("foo" to "bar"))
    }

    @Test
    fun `SubDir - Absolute path - Avatar - index, no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/avatar/my-user/32?foo=bar"))
        verifyCall("/index.php/avatar/my-user/32", mapOf("foo" to "bar"))
    }

    @Test
    fun `SubDir - Absolute path - Avatar - index, trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/avatar/my-user/32/?foo=bar"))
        verifyCall("/index.php/avatar/my-user/32", mapOf("foo" to "bar"))
    }

    // -----------------------------------------------------------
    // Nextcloud on SubDir / Absolute path / WebDAV
    // -----------------------------------------------------------

    @Test
    fun `SubDir - Absolute path - WebDAV - no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/foo/bar.png"))
        verifyCall("/remote.php/webdav/foo/bar.png")
    }

    @Test
    fun `SubDir - Absolute path - WebDAV - trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/foo/bar.png/"))
        verifyCall("/remote.php/webdav/foo/bar.png/")
    }

    // Empty query parameter

    @Test
    fun `SubDir - Absolute path - WebDAV - no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/foo/bar.png?"))
        verifyCall("/remote.php/webdav/foo/bar.png")
    }

    @Test
    fun `SubDir - Absolute path - WebDAV - trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/foo/bar.png/?"))
        verifyCall("/remote.php/webdav/foo/bar.png/")
    }

    // One query parameter

    @Test
    fun `SubDir - Absolute path - WebDAV - no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/foo/bar.png?baz=qux"))
        verifyCall("/remote.php/webdav/foo/bar.png", mapOf("baz" to "qux"))
    }

    @Test
    fun `SubDir - Absolute path - WebDAV - trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/foo/bar.png/?baz=qux"))
        verifyCall("/remote.php/webdav/foo/bar.png/", mapOf("baz" to "qux"))
    }

    // -----------------------------------------------------------
    // Nextcloud on SubDir / Absolute path / remote.php and index.php
    // -----------------------------------------------------------

    @Test
    fun `SubDir - Absolute path - remote endpoints - no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/remote.php/foo/bar.png"))
        verifyCall("/remote.php/foo/bar.png")
    }

    @Test
    fun `SubDir - Absolute path - remote endpoints - trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/remote.php/foo/bar.png/"))
        verifyCall("/remote.php/foo/bar.png/")
    }

    // Empty query parameter

    @Test
    fun `SubDir - Absolute path - remote endpoints - no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/remote.php/foo/bar.png?"))
        verifyCall("/remote.php/foo/bar.png")
    }

    @Test
    fun `SubDir - Absolute path - remote endpoints - trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/remote.php/foo/bar.png/?"))
        verifyCall("/remote.php/foo/bar.png/")
    }

    // One query parameter

    @Test
    fun `SubDir - Absolute path - remote endpoints - no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/remote.php/foo/bar.png?baz=qux"))
        verifyCall("/remote.php/foo/bar.png", mapOf("baz" to "qux"))
    }

    @Test
    fun `SubDir - Absolute path - remote endpoints - trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/remote.php/foo/bar.png/?baz=qux"))
        verifyCall("/remote.php/foo/bar.png/", mapOf("baz" to "qux"))
    }

    // index endpoints

    @Test
    fun `SubDir - Absolute path - index endpoints - no trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/foo/bar.png"))
        verifyCall("/index.php/foo/bar.png")
    }

    @Test
    fun `SubDir - Absolute path - index endpoints - trailing slash, no query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/foo/bar.png/"))
        verifyCall("/index.php/foo/bar.png/")
    }

    // Empty query parameter

    @Test
    fun `SubDir - Absolute path - index endpoints - no trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/foo/bar.png?"))
        verifyCall("/index.php/foo/bar.png")
    }

    @Test
    fun `SubDir - Absolute path - index endpoints - trailing slash, empty query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/foo/bar.png/?"))
        verifyCall("/index.php/foo/bar.png/")
    }

    // One query parameter

    @Test
    fun `SubDir - Absolute path - index endpoints - no trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/foo/bar.png?baz=qux"))
        verifyCall("/index.php/foo/bar.png", mapOf("baz" to "qux"))
    }

    @Test
    fun `SubDir - Absolute path - index endpoints - trailing slash, one query parameter`() {
        load(SingleSignOnUrl(ssoAccountSubDir, "/nc/index.php/foo/bar.png/?baz=qux"))
        verifyCall("/index.php/foo/bar.png/", mapOf("baz" to "qux"))
    }

    // ################################################
    // BAD PATHS
    // ################################################

    @Test
    fun `Given the host of the URL does not match the SingleSignOn account, an IllegalArgumentException should be passed to the callback`() {
        load(SingleSignOnUrl(ssoAccountRoot, "https://example.com/nc/foo/bar.png"))
        verifySuccess(false)
        verify(exactly = 1) { callback.onLoadFailed(withArg { it is IllegalArgumentException }) }
    }

    @Test
    fun `Given an URL which is neither an URL nor an absolute path, an IllegalArgumentException should be passed to the callback`() {
        load(SingleSignOnUrl(ssoAccountRoot, "nc/foo/bar.png"))
        verifySuccess(false)
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
            "/f/123456?width=15&height=16",
            apiFactory
        ) {
            override fun getSingleSignOnAccount(
                context: Context,
                model: String
            ): SingleSignOnAccount {
                return ssoAccountRoot
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
            "/f/123456?width=15&height=16",
            apiFactory
        ) {
            override fun getSingleSignOnAccount(
                context: Context,
                model: String
            ): SingleSignOnAccount {
                return ssoAccountRoot
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