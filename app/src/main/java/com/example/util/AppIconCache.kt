package com.example.util

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.LruCache
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppIconCache {
    private val cache = LruCache<String, Bitmap>(150)

    suspend fun getIcon(pm: PackageManager, packageName: String): Bitmap? = withContext(Dispatchers.IO) {
        cache.get(packageName)?.let { return@withContext it }
        try {
            val drawable = pm.getApplicationIcon(packageName)
            val bitmap = if (drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
                drawable.toBitmap(
                    width = drawable.intrinsicWidth.coerceAtMost(192),
                    height = drawable.intrinsicHeight.coerceAtMost(192),
                    config = Bitmap.Config.ARGB_8888
                )
            } else {
                drawable.toBitmap(192, 192, Bitmap.Config.ARGB_8888)
            }
            cache.put(packageName, bitmap)
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}
