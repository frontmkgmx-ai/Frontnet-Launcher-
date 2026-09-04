package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey val packageName: String,
    val activityName: String = "",
    val label: String = "",
    val categoryName: String = "OTHER",
    val launchCount: Int = 0,
    val lastLaunchedTimestamp: Long = 0L,
    val isPinnedToDock: Boolean = false,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val isLocked: Boolean = false
)

@Entity(tableName = "launcher_config")
data class LauncherConfigEntity(
    @PrimaryKey val id: Int = 1,
    val themeStyle: String = "MATERIAL_YOU", // MATERIAL_YOU, ONE_UI, HYPER_OS
    val iconShape: String = "SQUIRCLE",     // CIRCLE, SQUIRCLE, ROUNDED_SQUARE, TEARDROP
    val iconThemed: Boolean = false,
    val iconSizeDp: Int = 56,
    val showLabels: Boolean = true,
    val gridColumns: Int = 4,
    val wallpaperId: String = "monet_aurora",
    val drawerCategorized: Boolean = true,
    val addNewAppsToHome: Boolean = true,
    val dockApps: List<String> = emptyList(),
    val hasInitializedDock: Boolean = false,
    val homeScreenStyle: String = "CLASSIC_GRID",
    val appDrawerStyle: String = "CATEGORY_TABS",
    val widgetsJson: String = "[]",
    val gestureSwipeDownAction: String = "SEARCH", // SEARCH, NOTIFICATIONS, AI_ASSISTANT
    val gestureDoubleTapAction: String = "AI_ROUTINE", // AI_ROUTINE, LOCK, APP_DRAWER
    val isFirstRunCompleted: Boolean = false,
    @androidx.room.ColumnInfo(defaultValue = "1") val highRefreshRateEnabled: Boolean = true,
    @androidx.room.ColumnInfo(defaultValue = "1") val isAiEnabled: Boolean = true,
    @androidx.room.ColumnInfo(defaultValue = "NULL") val customWallpaperUri: String? = null,
    @androidx.room.ColumnInfo(defaultValue = "1") val homePageCount: Int = 1,
    @androidx.room.ColumnInfo(defaultValue = "GPU_REAL") val renderingMode: String = "GPU_REAL"
)
