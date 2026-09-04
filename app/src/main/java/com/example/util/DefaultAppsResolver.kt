package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import com.example.model.AppItem
import com.example.model.LauncherApp

object DefaultAppsResolver {

    /**
     * Resolves default apps (Phone, Messages, Browser, Camera) directly from the provided AppItem list.
     * Guaranteed 4 to 5 essential apps returned with priority on resolved system intents and robust fallbacks.
     */
    fun resolveDefaultApps(context: Context, allApps: List<AppItem>): List<AppItem> {
        val pm = context.packageManager
        val resolved = mutableListOf<AppItem>()
        val addedPackages = mutableSetOf<String>()

        fun findAndAdd(pkg: String?): Boolean {
            if (pkg == null) return false
            val app = allApps.firstOrNull { it.packageName == pkg }
            if (app != null && !addedPackages.contains(app.packageName)) {
                resolved.add(app)
                addedPackages.add(app.packageName)
                return true
            }
            return false
        }

        fun findByKeywords(vararg keywords: String): Boolean {
            val match = allApps.firstOrNull { app ->
                !addedPackages.contains(app.packageName) &&
                keywords.any { kw ->
                    app.packageName.contains(kw, ignoreCase = true) ||
                    app.label.contains(kw, ignoreCase = true)
                }
            }
            if (match != null) {
                resolved.add(match)
                addedPackages.add(match.packageName)
                return true
            }
            return false
        }

        // 1. Telefone: Intent(Intent.ACTION_DIAL) -> Fallback: "dialer", "phone", "telecom"
        val phonePkg = resolveIntentPackage(pm, Intent(Intent.ACTION_DIAL))
        if (!findAndAdd(phonePkg)) {
            findByKeywords("dialer", "phone", "telecom", "contatos", "telefone")
        }

        // 2. Mensagens: Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")) -> Fallback: "messaging", "mms", "messages", "com.google.android.apps.messaging"
        val smsPkg = resolveIntentPackage(pm, Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")))
        if (!findAndAdd(smsPkg)) {
            findByKeywords("messaging", "mms", "messages", "torpedo", "sms")
        }

        // 3. Navegador Web: Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")) -> Fallback: "chrome", "browser", "firefox", "samsung.android.app.sbrowser"
        val browserPkg = resolveIntentPackage(pm, Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")))
            ?: resolveIntentPackage(pm, Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER))
        if (!findAndAdd(browserPkg)) {
            findByKeywords("chrome", "browser", "firefox", "sbrowser", "navegador", "internet")
        }

        // 4. Câmera: Intent(MediaStore.ACTION_IMAGE_CAPTURE) -> Fallback: "camera"
        val cameraPkg = resolveIntentPackage(pm, Intent(MediaStore.ACTION_IMAGE_CAPTURE))
            ?: resolveIntentPackage(pm, Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_GALLERY))
        if (!findAndAdd(cameraPkg)) {
            findByKeywords("camera", "câmera", "foto")
        }

        // Complete up to 5 apps if needed with top available apps (e.g., Photos/Gallery, Settings, etc.)
        if (resolved.size < 5) {
            val remaining = allApps.filter { !addedPackages.contains(it.packageName) }
            for (candidate in remaining) {
                if (resolved.size >= 5) break
                resolved.add(candidate)
                addedPackages.add(candidate.packageName)
            }
        }

        return resolved.take(5)
    }

    /**
     * Resolves default apps when working with LauncherApp instances.
     */
    fun resolveDefaultLauncherApps(context: Context, allApps: List<LauncherApp>): List<LauncherApp> {
        val pm = context.packageManager
        val resolved = mutableListOf<LauncherApp>()
        val addedPackages = mutableSetOf<String>()

        fun findAndAdd(pkg: String?): Boolean {
            if (pkg == null) return false
            val app = allApps.firstOrNull { it.packageName == pkg }
            if (app != null && !addedPackages.contains(app.packageName)) {
                resolved.add(app)
                addedPackages.add(app.packageName)
                return true
            }
            return false
        }

        fun findByKeywords(vararg keywords: String): Boolean {
            val match = allApps.firstOrNull { app ->
                !addedPackages.contains(app.packageName) &&
                keywords.any { kw ->
                    app.packageName.contains(kw, ignoreCase = true) ||
                    app.label.contains(kw, ignoreCase = true)
                }
            }
            if (match != null) {
                resolved.add(match)
                addedPackages.add(match.packageName)
                return true
            }
            return false
        }

        // 1. Phone
        val phonePkg = resolveIntentPackage(pm, Intent(Intent.ACTION_DIAL))
        if (!findAndAdd(phonePkg)) {
            findByKeywords("dialer", "phone", "telecom", "contatos", "telefone")
        }

        // 2. Messages
        val smsPkg = resolveIntentPackage(pm, Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")))
        if (!findAndAdd(smsPkg)) {
            findByKeywords("messaging", "mms", "messages", "torpedo", "sms")
        }

        // 3. Web Browser
        val browserPkg = resolveIntentPackage(pm, Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")))
            ?: resolveIntentPackage(pm, Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER))
        if (!findAndAdd(browserPkg)) {
            findByKeywords("chrome", "browser", "firefox", "sbrowser", "navegador", "internet")
        }

        // 4. Camera
        val cameraPkg = resolveIntentPackage(pm, Intent(MediaStore.ACTION_IMAGE_CAPTURE))
            ?: resolveIntentPackage(pm, Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_GALLERY))
        if (!findAndAdd(cameraPkg)) {
            findByKeywords("camera", "câmera", "foto")
        }

        // Complete up to 5 apps if needed
        if (resolved.size < 5) {
            val remaining = allApps.filter { !addedPackages.contains(it.packageName) && !it.isHidden }
            for (candidate in remaining) {
                if (resolved.size >= 5) break
                resolved.add(candidate)
                addedPackages.add(candidate.packageName)
            }
        }

        return resolved.take(5)
    }

    fun getDefaultAppsPackages(context: Context): List<String> {
        val pm = context.packageManager
        val packages = mutableSetOf<String>()

        val phonePkg = resolveIntentPackage(pm, Intent(Intent.ACTION_DIAL))
        phonePkg?.let { packages.add(it) }

        val smsPkg = resolveIntentPackage(pm, Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")))
        smsPkg?.let { packages.add(it) }

        val browserPkg = resolveIntentPackage(pm, Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")))
            ?: resolveIntentPackage(pm, Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER))
        browserPkg?.let { packages.add(it) }

        val cameraPkg = resolveIntentPackage(pm, Intent(MediaStore.ACTION_IMAGE_CAPTURE))
            ?: resolveIntentPackage(pm, Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_GALLERY))
        cameraPkg?.let { packages.add(it) }

        val fallbacks = listOf(
            "com.google.android.dialer", "com.samsung.android.dialer",
            "com.google.android.apps.messaging", "com.samsung.android.messaging",
            "com.android.chrome", "org.mozilla.firefox",
            "com.google.android.GoogleCamera", "com.sec.android.app.camera"
        )

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

    private fun resolveIntentPackage(pm: PackageManager, intent: Intent): String? {
        val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (resolveInfo != null && resolveInfo.activityInfo != null && resolveInfo.activityInfo.packageName != "android") {
            return resolveInfo.activityInfo.packageName
        }
        return null
    }
}
