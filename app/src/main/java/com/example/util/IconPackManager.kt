package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class IconPackInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable?
)

object IconPackManager {

    fun getAvailableIconPacks(context: Context): List<IconPackInfo> {
        val pm = context.packageManager
        val iconPacks = mutableListOf<IconPackInfo>()
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory("com.anddoes.launcher.THEME")
        }
        val resolved = pm.queryIntentActivities(intent, 0)
        
        // Also support other common launcher theme categories
        val adwIntent = Intent("org.adw.launcher.THEMES")
        val adwResolved = pm.queryIntentActivities(adwIntent, 0)
        
        val allResolved = (resolved + adwResolved).distinctBy { it.activityInfo.packageName }

        for (info in allResolved) {
            try {
                val pkg = info.activityInfo.packageName
                val appInfo = pm.getApplicationInfo(pkg, 0)
                val label = pm.getApplicationLabel(appInfo).toString()
                val icon = pm.getApplicationIcon(appInfo)
                iconPacks.add(IconPackInfo(pkg, label, icon))
            } catch (e: Exception) {
                // Ignore
            }
        }
        return iconPacks
    }
    
    // Simple mock map for appfilter. In a full implementation, you'd parse appfilter.xml.
    private var currentIconPackPkg: String? = null
    private var iconMap = mutableMapOf<String, String>()
    private var iconPackRes: Resources? = null
    private var iconPackPkgName: String? = null
    
    suspend fun loadIconPack(context: Context, packageName: String) = withContext(Dispatchers.IO) {
        if (currentIconPackPkg == packageName) return@withContext
        
        val pm = context.packageManager
        iconMap.clear()
        
        try {
            iconPackRes = pm.getResourcesForApplication(packageName)
            iconPackPkgName = packageName
            currentIconPackPkg = packageName
            
            // Parse appfilter.xml
            val appfilterId = iconPackRes?.getIdentifier("appfilter", "xml", packageName)
            if (appfilterId != null && appfilterId > 0) {
                val xpp = iconPackRes?.getXml(appfilterId)
                if (xpp != null) {
                    var eventType = xpp.eventType
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG && xpp.name == "item") {
                            val component = xpp.getAttributeValue(null, "component")
                            val drawable = xpp.getAttributeValue(null, "drawable")
                            if (component != null && drawable != null) {
                                // Extract package name from ComponentInfo{pkg/cls}
                                val pkgStart = component.indexOf("{") + 1
                                val pkgEnd = component.indexOf("/")
                                if (pkgStart > 0 && pkgEnd > pkgStart) {
                                    val pkg = component.substring(pkgStart, pkgEnd)
                                    iconMap[pkg] = drawable
                                }
                            }
                        }
                        eventType = xpp.next()
                    }
                }
            }
        } catch (e: Exception) {
            currentIconPackPkg = null
            iconPackRes = null
            iconPackPkgName = null
        }
    }

    fun getIconFromPack(context: Context, appPackageName: String): Drawable? {
        if (iconPackRes == null || iconPackPkgName == null) return null
        
        val drawableName = iconMap[appPackageName] ?: return null
        return try {
            val resId = iconPackRes!!.getIdentifier(drawableName, "drawable", iconPackPkgName)
            if (resId > 0) {
                iconPackRes!!.getDrawable(resId, null)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
