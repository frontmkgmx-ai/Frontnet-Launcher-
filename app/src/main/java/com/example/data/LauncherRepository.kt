package com.example.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.example.model.AppCategory
import com.example.model.AppItem
import com.example.model.LauncherApp
import com.example.model.LauncherThemeStyle
import com.example.util.AppIconCache
import com.example.util.CategoryClassifier
import com.example.util.DefaultAppsResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class LauncherRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val dao = database.launcherDao()

    val configFlow: Flow<LauncherConfigEntity?> = dao.getLauncherConfig()
    val appUsageFlow: Flow<List<AppUsageEntity>> = dao.getAllAppUsages()

    /**
     * Converts a Drawable into a ready-to-render ImageBitmap strictly on background threads.
     */
    private fun decodeDrawableToImageBitmap(drawable: Drawable?): ImageBitmap? {
        if (drawable == null) return null
        return try {
            val bitmap = when (drawable) {
                is BitmapDrawable -> drawable.bitmap
                is ColorDrawable -> {
                    val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bmp)
                    drawable.draw(canvas)
                    bmp
                }
                else -> {
                    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceAtMost(192) else 96
                    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceAtMost(192) else 96
                    drawable.toBitmap(width, height, Bitmap.Config.ARGB_8888)
                }
            }
            bitmap?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Pre-loads all apps and decodes all drawables into ImageBitmaps strictly on Dispatchers.IO.
     * Guaranteed zero UI thread stalling during scrolling.
     */
    suspend fun getInstalledOrPreloadedApps(): List<LauncherApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolvedList = try {
            pm.queryIntentActivities(mainIntent, 0)
        } catch (e: Exception) {
            emptyList()
        }

        val existingUsageMap = dao.getAllAppUsages().firstOrNull()?.associateBy { it.packageName } ?: emptyMap()
        val appList = mutableListOf<LauncherApp>()
        val currentPackageName = context.packageName

        val defaultDockPackages = if (existingUsageMap.none { it.value.isPinnedToDock }) {
            DefaultAppsResolver.getDefaultAppsPackages(context)
        } else {
            emptyList()
        }

        for (resolveInfo in resolvedList) {
            val pkg = resolveInfo.activityInfo.packageName
            // Exclude self launcher from drawer if desired
            if (pkg == currentPackageName) continue

            val appInfo = try {
                pm.getApplicationInfo(pkg, 0)
            } catch (e: Exception) {
                null
            }

            val label = try {
                if (appInfo != null) pm.getApplicationLabel(appInfo).toString()
                else resolveInfo.loadLabel(pm).toString()
            } catch (e: Exception) {
                try {
                    resolveInfo.loadLabel(pm).toString()
                } catch (e2: Exception) {
                    pkg
                }
            }

            val autoCategory = CategoryClassifier.classify(pkg, label, appInfo)

            // Extract Drawable and convert to ImageBitmap strictly on background thread
            val cachedImage = AppIconCache.getCachedImageBitmap(pkg)
            val decodedImage = if (cachedImage != null) {
                cachedImage
            } else {
                val iconDrawable = try {
                    resolveInfo.loadIcon(pm)
                } catch (e: Exception) {
                    null
                }
                val img = decodeDrawableToImageBitmap(iconDrawable)
                if (img != null) {
                    AppIconCache.putCachedImageBitmap(pkg, img)
                }
                img
            }

            val usage = existingUsageMap[pkg]
            val effectiveCategory = if (usage != null && usage.categoryName != "OTHER") {
                AppCategory.fromName(usage.categoryName)
            } else {
                autoCategory
            }
            
            val isPinned = usage?.isPinnedToDock ?: defaultDockPackages.contains(pkg)

            appList.add(
                LauncherApp(
                    packageName = pkg,
                    activityName = resolveInfo.activityInfo.name,
                    label = label,
                    category = effectiveCategory,
                    icon = decodedImage,
                    iconDrawable = null,
                    iconBitmap = null,
                    launchCount = usage?.launchCount ?: 0,
                    lastLaunchedTimestamp = usage?.lastLaunchedTimestamp ?: 0L,
                    isPinnedToDock = isPinned,
                    isFavorite = usage?.isFavorite ?: false,
                    isHidden = usage?.isHidden ?: false,
                    isLocked = usage?.isLocked ?: false,
                    isSystemDefault = false
                )
            )
        }

        // If running in minimal environment or emulator where few user apps exist, enrich with core realistic system suite
        if (appList.size < 8) {
            val fallbackSuite = getSystemFallbackSuite(existingUsageMap)
            for (fallback in fallbackSuite) {
                if (appList.none { it.packageName == fallback.packageName }) {
                    appList.add(fallback)
                }
            }
        }

        // Save new apps to Room for persistence if not present
        val entitiesToSave = appList.map { app ->
            AppUsageEntity(
                packageName = app.packageName,
                activityName = app.activityName,
                label = app.label,
                categoryName = app.category.name,
                launchCount = app.launchCount,
                lastLaunchedTimestamp = app.lastLaunchedTimestamp,
                isPinnedToDock = app.isPinnedToDock,
                isFavorite = app.isFavorite,
                isHidden = app.isHidden,
                isLocked = app.isLocked
            )
        }
        dao.insertAllAppUsages(entitiesToSave)

        appList.sortedBy { it.label.lowercase() }
    }

    suspend fun launchApp(packageName: String): Boolean = withContext(Dispatchers.Main) {
        dao.recordAppLaunch(packageName, System.currentTimeMillis())

        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(launchIntent)
                return@withContext true
            } catch (e: Exception) {
                Log.e("LauncherRepo", "Could not start activity for $packageName", e)
            }
        }

        // Fallback for virtual/system actions
        val fallbackIntent = when (packageName) {
            "com.android.dialer", "com.google.android.dialer" -> Intent(Intent.ACTION_DIAL)
            "com.android.mms", "com.google.android.apps.messaging" -> Intent(Intent.ACTION_VIEW).apply {
                type = "vnd.android-dir/mms-sms"
            }
            "com.android.camera", "com.google.android.GoogleCamera" -> Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            "com.android.settings" -> Intent(Settings.ACTION_SETTINGS)
            "com.android.chrome" -> Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"))
            "com.google.android.apps.maps" -> Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=restaurantes"))
            "com.google.android.youtube" -> Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com"))
            "com.spotify.music" -> Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com"))
            else -> null
        }

        if (fallbackIntent != null) {
            fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(fallbackIntent)
                return@withContext true
            } catch (e: Exception) {
                Log.e("LauncherRepo", "Fallback launch failed for $packageName", e)
            }
        }

        false
    }

    suspend fun togglePinToDock(packageName: String, currentPinned: Boolean) = withContext(Dispatchers.IO) {
        dao.setPinnedToDock(packageName, !currentPinned)
    }

    suspend fun toggleAppLock(packageName: String, currentLocked: Boolean) = withContext(Dispatchers.IO) {
        dao.setAppLocked(packageName, !currentLocked)
    }

    suspend fun toggleAppHidden(packageName: String, currentHidden: Boolean) = withContext(Dispatchers.IO) {
        dao.setAppHidden(packageName, !currentHidden)
    }

    suspend fun updateCategory(packageName: String, newCategory: AppCategory) = withContext(Dispatchers.IO) {
        dao.updateAppCategory(packageName, newCategory.name)
    }

    suspend fun saveConfig(config: LauncherConfigEntity) = withContext(Dispatchers.IO) {
        dao.saveLauncherConfig(config)
    }

    suspend fun setFirstRunCompleted(completed: Boolean = true) = withContext(Dispatchers.IO) {
        val current = dao.getLauncherConfig().firstOrNull() ?: LauncherConfigEntity()
        dao.saveLauncherConfig(current.copy(isFirstRunCompleted = completed))
    }

    suspend fun setHighRefreshRateEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        val current = dao.getLauncherConfig().firstOrNull() ?: LauncherConfigEntity()
        dao.saveLauncherConfig(current.copy(highRefreshRateEnabled = enabled))
    }

    private fun getSystemFallbackSuite(usageMap: Map<String, AppUsageEntity>): List<LauncherApp> {
        val list = listOf(
            FallbackApp("com.front.launcher.settings", "Configurações do Front", AppCategory.TOOLS, Icons.Rounded.Tune, Color(0xFF00E5FF), false, isLauncherSettings = true),
            FallbackApp("com.android.dialer", "Telefone", AppCategory.COMMUNICATION, Icons.Rounded.Phone, Color(0xFF22C55E), true),
            FallbackApp("com.google.android.apps.messaging", "Mensagens", AppCategory.COMMUNICATION, Icons.AutoMirrored.Rounded.Chat, Color(0xFF0EA5E9), true),
            FallbackApp("com.android.chrome", "Navegador Web", AppCategory.TOOLS, Icons.Rounded.Language, Color(0xFFEAB308), true),
            FallbackApp("com.android.camera", "Câmera", AppCategory.TOOLS, Icons.Rounded.CameraAlt, Color(0xFFEF4444), true),
            FallbackApp("com.google.android.apps.photos", "Galeria & Fotos", AppCategory.MEDIA, Icons.Rounded.PhotoLibrary, Color(0xFFF97316), true),
            FallbackApp("com.google.android.gm", "E-mail", AppCategory.PRODUCTIVITY, Icons.Rounded.Email, Color(0xFFEA4335), false),
            FallbackApp("com.google.android.apps.maps", "Mapas & Rotas", AppCategory.TRAVEL, Icons.Rounded.Navigation, Color(0xFF34D399), false),
            FallbackApp("com.spotify.music", "Spotify Música", AppCategory.MEDIA, Icons.Rounded.MusicNote, Color(0xFF1DB954), false),
            FallbackApp("com.google.android.youtube", "Vídeos & Streaming", AppCategory.MEDIA, Icons.Rounded.PlayArrow, Color(0xFFFF0000), false),
            FallbackApp("com.whatsapp", "WhatsApp", AppCategory.COMMUNICATION, Icons.AutoMirrored.Rounded.Chat, Color(0xFF25D366), false),
            FallbackApp("com.instagram.android", "Instagram", AppCategory.COMMUNICATION, Icons.Rounded.PhotoLibrary, Color(0xFFE1306C), false),
            FallbackApp("com.google.android.calendar", "Calendário", AppCategory.PRODUCTIVITY, Icons.Rounded.CalendarMonth, Color(0xFF4285F4), false),
            FallbackApp("com.google.android.calculator", "Calculadora", AppCategory.TOOLS, Icons.Rounded.Calculate, Color(0xFF64748B), false),
            FallbackApp("com.google.android.deskclock", "Relógio & Alarme", AppCategory.TOOLS, Icons.Rounded.Schedule, Color(0xFF3B82F6), false),
            FallbackApp("com.google.android.apps.docs", "Arquivos & Docs", AppCategory.PRODUCTIVITY, Icons.Rounded.Folder, Color(0xFF10B981), false),
            FallbackApp("com.nu.production", "Nubank", AppCategory.FINANCE, Icons.Rounded.AccountBalance, Color(0xFF820AD1), false),
            FallbackApp("com.mercadolivre", "Mercado Livre", AppCategory.FINANCE, Icons.Rounded.ShoppingBag, Color(0xFFFFE600), false),
            FallbackApp("com.android.settings", "Configurações do Sistema", AppCategory.TOOLS, Icons.Rounded.Settings, Color(0xFF6B7280), false),
            FallbackApp("com.google.android.play.games", "Jogos", AppCategory.GAMES, Icons.Rounded.SportsEsports, Color(0xFF10B981), false)
        )

        return list.map { item ->
            val usage = usageMap[item.pkg]
            LauncherApp(
                packageName = item.pkg,
                activityName = "",
                label = item.label,
                category = usage?.let { AppCategory.fromName(it.categoryName) } ?: item.category,
                icon = null,
                iconDrawable = null,
                iconVector = item.icon,
                iconTint = item.tint,
                launchCount = usage?.launchCount ?: 0,
                lastLaunchedTimestamp = usage?.lastLaunchedTimestamp ?: 0L,
                isPinnedToDock = usage?.isPinnedToDock ?: item.defaultDock,
                isFavorite = usage?.isFavorite ?: false,
                isHidden = usage?.isHidden ?: false,
                isLocked = usage?.isLocked ?: false,
                isSystemDefault = true,
                isLauncherSettingsShortcut = item.isLauncherSettings
            )
        }
    }

    private data class FallbackApp(
        val pkg: String,
        val label: String,
        val category: AppCategory,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val tint: Color,
        val defaultDock: Boolean,
        val isLauncherSettings: Boolean = false
    )
}
