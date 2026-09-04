package com.example.util

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppIconCache {
    // In-memory LruCache for ImageBitmap to eliminate repeated processing during recomposition
    private val imageBitmapCache = LruCache<String, ImageBitmap>(250)
    private val bitmapCache = LruCache<String, Bitmap>(250)

    fun getCachedImageBitmap(packageName: String): ImageBitmap? {
        return imageBitmapCache.get(packageName)
    }

    fun putCachedImageBitmap(packageName: String, imageBitmap: ImageBitmap) {
        imageBitmapCache.put(packageName, imageBitmap)
    }

    fun drawableToImageBitmap(drawable: Drawable?): ImageBitmap? {
        if (drawable == null) return null
        return try {
            val bitmap = if (drawable is BitmapDrawable && drawable.bitmap != null) {
                drawable.bitmap
            } else {
                val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceAtMost(192) else 96
                val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceAtMost(192) else 96
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bmp
            }
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getOrLoadImageBitmap(pm: PackageManager, packageName: String): ImageBitmap? = withContext(Dispatchers.IO) {
        imageBitmapCache.get(packageName)?.let { return@withContext it }
        val bmp = getIcon(pm, packageName)
        val imageBitmap = bmp?.asImageBitmap()
        if (imageBitmap != null) {
            imageBitmapCache.put(packageName, imageBitmap)
        }
        imageBitmap
    }

    suspend fun getIcon(pm: PackageManager, packageName: String): Bitmap? = withContext(Dispatchers.IO) {
        bitmapCache.get(packageName)?.let { return@withContext it }
        try {
            val drawable = pm.getApplicationIcon(packageName)
            val bitmap = if (drawable is BitmapDrawable && drawable.bitmap != null) {
                drawable.bitmap
            } else {
                val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceAtMost(192) else 192
                val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceAtMost(192) else 192
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bmp
            }
            bitmapCache.put(packageName, bitmap)
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}
