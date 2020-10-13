package it.niedermann.nextcloud.exception;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ExceptionUtilTest {
    @Test
    public void containAllInformation() {
        final Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        final String debug = ExceptionUtil.getDebugInfos(appContext, new IllegalStateException());

        assertTrue(debug.contains("App Version:"));
        assertTrue(debug.contains("App Version Code:"));
        assertTrue(debug.contains("Files App Version Code:"));
        assertTrue(debug.contains("OS Version:"));
        assertTrue(debug.contains("OS API Level:"));
        assertTrue(debug.contains("Device:"));
        assertTrue(debug.contains("Manufacturer:"));
        assertTrue(debug.contains("Model (and Product):"));

        assertFalse(debug.contains("Server App Version:"));
        assertFalse(debug.contains("App Flavor:"));
    }

    @Test
    public void containExceptionMessageAndType() {
        final Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        final String exceptionMessage = "My fancy message";
        final String debug = ExceptionUtil.getDebugInfos(appContext, new IllegalStateException(exceptionMessage));

        assertTrue(debug.contains(exceptionMessage));
        assertTrue(debug.contains(IllegalStateException.class.getCanonicalName()));
    }

    @Test
    public void containsServerAppVersion() {
        final Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        final String debugValidVersion = ExceptionUtil.getDebugInfos(appContext, new IllegalStateException(), null, "4.7.11");

        assertTrue(debugValidVersion.contains("Server App Version: 4.7.11"));

        final String debugEmptyVersion = ExceptionUtil.getDebugInfos(appContext, new IllegalStateException(), null, "");

        assertTrue(debugEmptyVersion.contains("Server App Version: unknown"));
    }

    @Test
    public void containsAppFlavor() {
        final Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        final String debugValidVersion = ExceptionUtil.getDebugInfos(appContext, new IllegalStateException(), "dev");

        assertTrue(debugValidVersion.contains("App Flavor: dev"));

        final String debugEmptyVersion = ExceptionUtil.getDebugInfos(appContext, new IllegalStateException(), "");

        assertTrue(debugEmptyVersion.contains("App Flavor: unknown"));
    }
}
