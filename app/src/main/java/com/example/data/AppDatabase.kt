package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_6 = object : Migration(4, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        migrateDatabaseToVersion6(db)
    }
}

val MIGRATION_1_6 = object : Migration(1, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        migrateDatabaseToVersion6(db)
    }
}

val MIGRATION_2_6 = object : Migration(2, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        migrateDatabaseToVersion6(db)
    }
}

val MIGRATION_3_6 = object : Migration(3, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        migrateDatabaseToVersion6(db)
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        migrateDatabaseToVersion6(db)
    }
}

private fun migrateDatabaseToVersion6(db: SupportSQLiteDatabase) {
    migrateAppUsage(db)
    migrateLauncherConfig(db)
}

private fun migrateAppUsage(db: SupportSQLiteDatabase) {
    db.execSQL("CREATE TABLE IF NOT EXISTS `_new_app_usage` (`packageName` TEXT NOT NULL, `activityName` TEXT NOT NULL, `label` TEXT NOT NULL, `categoryName` TEXT NOT NULL, `launchCount` INTEGER NOT NULL, `lastLaunchedTimestamp` INTEGER NOT NULL, `isPinnedToDock` INTEGER NOT NULL, `isFavorite` INTEGER NOT NULL, `isHidden` INTEGER NOT NULL, `isLocked` INTEGER NOT NULL, PRIMARY KEY(`packageName`))")

    val cursor = db.query("PRAGMA table_info(`app_usage`)")
    val oldColumns = mutableSetOf<String>()
    while (cursor.moveToNext()) {
        val idx = cursor.getColumnIndex("name")
        if (idx != -1) {
            oldColumns.add(cursor.getString(idx))
        }
    }
    cursor.close()

    if (oldColumns.isNotEmpty()) {
        val targetCols = listOf(
            "packageName", "activityName", "label", "categoryName", "launchCount",
            "lastLaunchedTimestamp", "isPinnedToDock", "isFavorite", "isHidden", "isLocked"
        )
        val selectExprs = targetCols.map { col ->
            if (oldColumns.contains(col)) {
                "`$col`"
            } else {
                when (col) {
                    "categoryName" -> "'OTHER'"
                    "launchCount" -> "0"
                    "lastLaunchedTimestamp" -> "0"
                    "isPinnedToDock" -> "0"
                    "isFavorite" -> "0"
                    "isHidden" -> "0"
                    "isLocked" -> "0"
                    else -> "''"
                }
            }
        }
        val targetColsSql = targetCols.joinToString(", ") { "`$it`" }
        val selectExprsSql = selectExprs.joinToString(", ")

        db.execSQL("INSERT OR REPLACE INTO `_new_app_usage` ($targetColsSql) SELECT $selectExprsSql FROM `app_usage`")
        db.execSQL("DROP TABLE `app_usage`")
        db.execSQL("ALTER TABLE `_new_app_usage` RENAME TO `app_usage`")
    } else {
        db.execSQL("DROP TABLE IF EXISTS `_new_app_usage`")
        db.execSQL("CREATE TABLE IF NOT EXISTS `app_usage` (`packageName` TEXT NOT NULL, `activityName` TEXT NOT NULL, `label` TEXT NOT NULL, `categoryName` TEXT NOT NULL, `launchCount` INTEGER NOT NULL, `lastLaunchedTimestamp` INTEGER NOT NULL, `isPinnedToDock` INTEGER NOT NULL, `isFavorite` INTEGER NOT NULL, `isHidden` INTEGER NOT NULL, `isLocked` INTEGER NOT NULL, PRIMARY KEY(`packageName`))")
    }
}

private fun migrateLauncherConfig(db: SupportSQLiteDatabase) {
    db.execSQL("CREATE TABLE IF NOT EXISTS `_new_launcher_config` (`id` INTEGER NOT NULL, `themeStyle` TEXT NOT NULL, `iconShape` TEXT NOT NULL, `iconThemed` INTEGER NOT NULL, `iconSizeDp` INTEGER NOT NULL, `showLabels` INTEGER NOT NULL, `gridColumns` INTEGER NOT NULL, `wallpaperId` TEXT NOT NULL, `drawerCategorized` INTEGER NOT NULL, `addNewAppsToHome` INTEGER NOT NULL, `dockApps` TEXT NOT NULL, `hasInitializedDock` INTEGER NOT NULL, `homeScreenStyle` TEXT NOT NULL, `appDrawerStyle` TEXT NOT NULL, `widgetsJson` TEXT NOT NULL, `gestureSwipeDownAction` TEXT NOT NULL, `gestureDoubleTapAction` TEXT NOT NULL, `isFirstRunCompleted` INTEGER NOT NULL, `highRefreshRateEnabled` INTEGER NOT NULL, `isAiEnabled` INTEGER NOT NULL, `customWallpaperUri` TEXT, `homePageCount` INTEGER NOT NULL, PRIMARY KEY(`id`))")

    val cursor = db.query("PRAGMA table_info(`launcher_config`)")
    val oldColumns = mutableSetOf<String>()
    while (cursor.moveToNext()) {
        val idx = cursor.getColumnIndex("name")
        if (idx != -1) {
            oldColumns.add(cursor.getString(idx))
        }
    }
    cursor.close()

    if (oldColumns.isNotEmpty()) {
        val targetCols = listOf(
            "id", "themeStyle", "iconShape", "iconThemed", "iconSizeDp",
            "showLabels", "gridColumns", "wallpaperId", "drawerCategorized",
            "addNewAppsToHome", "dockApps", "hasInitializedDock",
            "homeScreenStyle", "appDrawerStyle", "widgetsJson",
            "gestureSwipeDownAction", "gestureDoubleTapAction",
            "isFirstRunCompleted", "highRefreshRateEnabled", "isAiEnabled",
            "customWallpaperUri", "homePageCount"
        )
        val selectExprs = targetCols.map { col ->
            if (oldColumns.contains(col)) {
                "`$col`"
            } else {
                when (col) {
                    "id" -> "1"
                    "themeStyle" -> "'MATERIAL_YOU'"
                    "iconShape" -> "'SQUIRCLE'"
                    "iconThemed" -> "0"
                    "iconSizeDp" -> "56"
                    "showLabels" -> "1"
                    "gridColumns" -> "4"
                    "wallpaperId" -> "'monet_aurora'"
                    "drawerCategorized" -> "1"
                    "addNewAppsToHome" -> "1"
                    "dockApps" -> "'[]'"
                    "hasInitializedDock" -> "0"
                    "homeScreenStyle" -> "'CLASSIC_GRID'"
                    "appDrawerStyle" -> "'CATEGORY_TABS'"
                    "widgetsJson" -> "'[]'"
                    "gestureSwipeDownAction" -> "'SEARCH'"
                    "gestureDoubleTapAction" -> "'AI_ROUTINE'"
                    "isFirstRunCompleted" -> "0"
                    "highRefreshRateEnabled" -> "1"
                    "isAiEnabled" -> "1"
                    "customWallpaperUri" -> "NULL"
                    "homePageCount" -> "1"
                    else -> "NULL"
                }
            }
        }
        val targetColsSql = targetCols.joinToString(", ") { "`$it`" }
        val selectExprsSql = selectExprs.joinToString(", ")

        db.execSQL("INSERT OR REPLACE INTO `_new_launcher_config` ($targetColsSql) SELECT $selectExprsSql FROM `launcher_config`")
        db.execSQL("DROP TABLE `launcher_config`")
        db.execSQL("ALTER TABLE `_new_launcher_config` RENAME TO `launcher_config`")
    } else {
        db.execSQL("DROP TABLE IF EXISTS `_new_launcher_config`")
        db.execSQL("CREATE TABLE IF NOT EXISTS `launcher_config` (`id` INTEGER NOT NULL, `themeStyle` TEXT NOT NULL, `iconShape` TEXT NOT NULL, `iconThemed` INTEGER NOT NULL, `iconSizeDp` INTEGER NOT NULL, `showLabels` INTEGER NOT NULL, `gridColumns` INTEGER NOT NULL, `wallpaperId` TEXT NOT NULL, `drawerCategorized` INTEGER NOT NULL, `addNewAppsToHome` INTEGER NOT NULL, `dockApps` TEXT NOT NULL, `hasInitializedDock` INTEGER NOT NULL, `homeScreenStyle` TEXT NOT NULL, `appDrawerStyle` TEXT NOT NULL, `widgetsJson` TEXT NOT NULL, `gestureSwipeDownAction` TEXT NOT NULL, `gestureDoubleTapAction` TEXT NOT NULL, `isFirstRunCompleted` INTEGER NOT NULL, `highRefreshRateEnabled` INTEGER NOT NULL, `isAiEnabled` INTEGER NOT NULL, `customWallpaperUri` TEXT, `homePageCount` INTEGER NOT NULL, PRIMARY KEY(`id`))")
    }
}

@androidx.room.TypeConverters(StringListConverter::class)
@Database(
    entities = [AppUsageEntity::class, LauncherConfigEntity::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun launcherDao(): LauncherDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "launcher_database.db"
                )
                    .addMigrations(
                        MIGRATION_1_6,
                        MIGRATION_2_6,
                        MIGRATION_3_6,
                        MIGRATION_4_6,
                        MIGRATION_5_6
                    )
                    .fallbackToDestructiveMigration(true)
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

