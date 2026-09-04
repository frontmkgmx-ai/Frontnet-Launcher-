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
    val isHidden: Boolean = false
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
    val gestureSwipeDownAction: String = "SEARCH", // SEARCH, NOTIFICATIONS, AI_ASSISTANT
    val gestureDoubleTapAction: String = "AI_ROUTINE" // AI_ROUTINE, LOCK, APP_DRAWER
)
