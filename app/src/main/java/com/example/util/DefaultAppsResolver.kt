package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

object DefaultAppsResolver {

    fun getDefaultAppsPackages(context: Context): List<String> {
        val pm = context.packageManager
        val packages = mutableSetOf<String>()

        // 1. Phone / Dialer
        val dialerIntent = Intent(Intent.ACTION_DIAL)
        resolveAndAdd(pm, dialerIntent, packages)

        // 2. Messages / SMS
        val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
        resolveAndAdd(pm, smsIntent, packages)

        // 3. Web Browser
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
        val browserResolved = resolveAndAdd(pm, browserIntent, packages)
        if (!browserResolved) {
            val selectorIntent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
            resolveAndAdd(pm, selectorIntent, packages)
        }

        // 4. Camera
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val cameraResolved = resolveAndAdd(pm, cameraIntent, packages)
        if (!cameraResolved) {
            val selectorIntent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_GALLERY)
            resolveAndAdd(pm, selectorIntent, packages)
        }

        // Fallbacks if some are missing
        val fallbacks = listOf(
            "com.google.android.dialer", "com.samsung.android.dialer",
            "com.google.android.apps.messaging", "com.samsung.android.messaging",
            "com.android.chrome", "org.mozilla.firefox",
            "com.google.android.GoogleCamera", "com.sec.android.app.camera"
        )
        
        // Return 4 or 5 apps
        val finalPackages = packages.toMutableList()
        if (finalPackages.size < 4) {
             for (fb in fallbacks) {
                 if (finalPackages.size >= 5) break
                 try {
                     pm.getPackageInfo(fb, 0)
                     if (!finalPackages.contains(fb)) {
                         finalPackages.add(fb)
                     }
                 } catch (e: PackageManager.NameNotFoundException) {
                     // Ignore
                 }
             }
        }
        
        return finalPackages.take(5)
    }

    private fun resolveAndAdd(pm: PackageManager, intent: Intent, packages: MutableSet<String>): Boolean {
        val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (resolveInfo != null && resolveInfo.activityInfo != null && resolveInfo.activityInfo.packageName != "android") {
            packages.add(resolveInfo.activityInfo.packageName)
            return true
        }
        return false
    }
}
