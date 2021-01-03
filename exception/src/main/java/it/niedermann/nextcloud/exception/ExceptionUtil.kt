package it.niedermann.nextcloud.exception

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import com.nextcloud.android.sso.helper.VersionCheckHelper
import java.io.PrintWriter
import java.io.StringWriter

object ExceptionUtil {
    fun getDebugInfos(context: Context, throwable: Throwable): String {
        return getDebugInfos(context, throwable, null, null)
    }

    fun getDebugInfos(context: Context, throwable: Throwable, flavor: String?): String {
        return getDebugInfos(context, throwable, flavor, null)
    }

    fun getDebugInfos(context: Context, throwable: Throwable, flavor: String?, serverAppVersion: String?): String {
        return getDebugInfos(context, setOf(throwable), flavor, serverAppVersion)
    }

    fun getDebugInfos(context: Context, throwables: Collection<Throwable>): String {
        return getDebugInfos(context, throwables, null, null)
    }

    fun getDebugInfos(context: Context, throwables: Collection<Throwable>, flavor: String?): String {
        return getDebugInfos(context, throwables, flavor, null)
    }

    fun getDebugInfos(context: Context, throwables: Collection<Throwable>, flavor: String?, serverAppVersion: String?): String {
        val debugInfos = StringBuilder()
                .append(getAppVersions(context, flavor, serverAppVersion))
                .append("\n\n---\n")
                .append(deviceInfos)
                .append("\n\n---")
        for (throwable in throwables) {
            debugInfos.append("\n\n").append(getStacktraceOf(throwable))
        }
        return debugInfos.toString()
    }

    private fun getAppVersions(context: Context, flavor: String?, serverAppVersion: String?): String {
        var versions = ""
        try {
            val pInfo = context.applicationContext.packageManager.getPackageInfo(context.applicationContext.packageName, 0)
            versions += """
                App Version: ${pInfo.versionName}
                
                """.trimIndent()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versions += """
                    App Version Code: ${pInfo.longVersionCode}
                    
                    """.trimIndent()
            } else {
                versions += """
                    App Version Code: ${pInfo.versionCode}
                    
                    """.trimIndent()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            versions += """
                App Version: ${e.message}
                
                """.trimIndent()
            e.printStackTrace()
        }
        if (serverAppVersion != null) {
            versions += if (TextUtils.isEmpty(serverAppVersion)) {
                "Server App Version: " + "unknown"
            } else {
                "Server App Version: $serverAppVersion\n"
            }
        }
        if (flavor != null) {
            versions += if (TextUtils.isEmpty(flavor)) {
                "App Flavor: " + "unknown"
            } else {
                "App Flavor: $flavor\n"
            }
        }
        versions += "\n"
        try {
            versions += "Files App Version Code: " + VersionCheckHelper.getNextcloudFilesVersionCode(context, true)
        } catch (e: PackageManager.NameNotFoundException) {
            try {
                versions += "Files App Version Code: " + VersionCheckHelper.getNextcloudFilesVersionCode(context, false) + " (Beta)"
            } catch (e: PackageManager.NameNotFoundException) {
                versions += "Files App Version Code: " + e.message
                e.printStackTrace()
            }
        }
        return versions
    }

    private val deviceInfos: String
        get() = """
OS Version: ${System.getProperty("os.version")}(${Build.VERSION.INCREMENTAL})
OS API Level: ${Build.VERSION.SDK_INT}
Device: ${Build.DEVICE}
Manufacturer: ${Build.MANUFACTURER}
Model (and Product): ${Build.MODEL} (${Build.PRODUCT})"""

    private fun getStacktraceOf(e: Throwable): String {
        val sw = StringWriter()
        e.printStackTrace(PrintWriter(sw))
        return sw.toString()
    }
}