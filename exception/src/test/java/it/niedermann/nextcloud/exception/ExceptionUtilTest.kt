package it.niedermann.nextcloud.exception

import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import com.nextcloud.android.sso.helper.VersionCheckHelper
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(maxSdk = Build.VERSION_CODES.P)
class ExceptionUtilTest {
    @Before
    fun setup() {
        mockkStatic(VersionCheckHelper::class);
        every { VersionCheckHelper.getNextcloudFilesVersionCode(any(), any()) } returns 4711
    }

    @Test
    fun containAllInformation() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val debug = ExceptionUtil.getDebugInfos(appContext, IllegalStateException())
        assertTrue(debug.contains("App Version:"))
        assertTrue(debug.contains("App Version Code:"))
        assertTrue(debug.contains("Files App Version Code: 4711"))
        assertTrue(debug.contains("OS Version:"))
        assertTrue(debug.contains("OS API Level:"))
        assertTrue(debug.contains("Device:"))
        assertTrue(debug.contains("Manufacturer:"))
        assertTrue(debug.contains("Model (and Product):"))
        assertFalse(debug.contains("Server App Version:"))
        assertFalse(debug.contains("App Flavor:"))
    }

    @Test
    fun containExceptionMessageAndType() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val exceptionMessage = "My fancy message"
        val debug = ExceptionUtil.getDebugInfos(appContext, IllegalStateException(exceptionMessage))
        assertTrue(debug.contains(exceptionMessage))
        assertTrue(debug.contains(IllegalStateException::class.java.canonicalName!!))
    }

    @Test
    fun containsServerAppVersion() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val debugValidVersion = ExceptionUtil.getDebugInfos(appContext, IllegalStateException(), null, "4.7.11")
        assertTrue(debugValidVersion.contains("Server App Version: 4.7.11"))
        val debugEmptyVersion = ExceptionUtil.getDebugInfos(appContext, IllegalStateException(), null, "")
        assertTrue(debugEmptyVersion.contains("Server App Version: unknown"))
    }

    @Test
    fun containsAppFlavor() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val debugValidVersion = ExceptionUtil.getDebugInfos(appContext, IllegalStateException(), "dev")
        assertTrue(debugValidVersion.contains("App Flavor: dev"))
        val debugEmptyVersion = ExceptionUtil.getDebugInfos(appContext, IllegalStateException(), "")
        assertTrue(debugEmptyVersion.contains("App Flavor: unknown"))
    }
}