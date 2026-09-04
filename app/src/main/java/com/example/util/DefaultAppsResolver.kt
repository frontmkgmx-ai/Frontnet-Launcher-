package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Telephony
import android.os.Build

object DefaultAppsResolver {

    fun getDefaultApps(context: Context): List<String> {
        val pm = context.packageManager
        val defaultApps = mutableListOf<String>()

        // 1. Phone / Dialer
        val dialerIntent = Intent(Intent.ACTION_DIAL)
        val dialerResolveInfo = pm.resolveActivity(dialerIntent, PackageManager.MATCH_DEFAULT_ONLY)
        if (dialerResolveInfo != null && dialerResolveInfo.activityInfo.packageName != "android") {
            defaultApps.add(dialerResolveInfo.activityInfo.packageName)
        } else {
            val dialerFallbacks = listOf("com.google.android.dialer", "com.samsung.android.dialer", "com.android.dialer")
            dialerFallbacks.firstOrNull { isPackageInstalled(it, pm) }?.let { defaultApps.add(it) }
        }

        // 2. Messaging
        var messagingApp: String? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            messagingApp = Telephony.Sms.getDefaultSmsPackage(context)
        }
        if (messagingApp == null) {
            val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
            val smsResolveInfo = pm.resolveActivity(smsIntent, PackageManager.MATCH_DEFAULT_ONLY)
            if (smsResolveInfo != null && smsResolveInfo.activityInfo.packageName != "android") {
                messagingApp = smsResolveInfo.activityInfo.packageName
            } else {
                val smsFallbacks = listOf("com.google.android.apps.messaging", "com.samsung.android.messaging", "com.android.mms")
                messagingApp = smsFallbacks.firstOrNull { isPackageInstalled(it, pm) }
            }
        }
        messagingApp?.let { defaultApps.add(it) }

        // 3. Browser
        var browserApp: String? = null
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
        val browserResolveInfo = pm.resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY)
        if (browserResolveInfo != null && browserResolveInfo.activityInfo.packageName != "android") {
            browserApp = browserResolveInfo.activityInfo.packageName
        } else {
            val browserSelectorIntent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
            val browserSelectorResolveInfo = pm.resolveActivity(browserSelectorIntent, PackageManager.MATCH_DEFAULT_ONLY)
            if (browserSelectorResolveInfo != null && browserSelectorResolveInfo.activityInfo.packageName != "android") {
                browserApp = browserSelectorResolveInfo.activityInfo.packageName
            } else {
                val browserFallbacks = listOf("com.android.chrome", "org.mozilla.firefox", "com.opera.browser")
                browserApp = browserFallbacks.firstOrNull { isPackageInstalled(it, pm) }
            }
        }
        browserApp?.let { defaultApps.add(it) }

        // 4. Camera
        var cameraApp: String? = null
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val cameraResolveInfo = pm.resolveActivity(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY)
        if (cameraResolveInfo != null && cameraResolveInfo.activityInfo.packageName != "android") {
            cameraApp = cameraResolveInfo.activityInfo.packageName
        } else {
            val cameraFallbacks = listOf("com.google.android.GoogleCamera", "com.sec.android.app.camera", "com.android.camera2", "com.android.camera")
            cameraApp = cameraFallbacks.firstOrNull { isPackageInstalled(it, pm) }
        }
        cameraApp?.let { defaultApps.add(it) }

        return defaultApps.distinct().take(5)
    }

    private fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
