package it.niedermann.nextcloud.exception

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExceptionUtilTest {
    @Test
    fun containAllInformation() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val debug = ExceptionUtil.getDebugInfos(appContext, IllegalStateException())
        assertTrue(debug.contains("App Version:"))
        assertTrue(debug.contains("App Version Code:"))
        assertTrue(debug.contains("Files App Version Code:"))
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