package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LauncherDao {
    @Query("SELECT * FROM app_usage ORDER BY launchCount DESC, label ASC")
    fun getAllAppUsages(): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage WHERE packageName = :packageName LIMIT 1")
    suspend fun getAppUsage(packageName: String): AppUsageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAppUsage(app: AppUsageEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllAppUsages(apps: List<AppUsageEntity>)

    @Query("UPDATE app_usage SET launchCount = launchCount + 1, lastLaunchedTimestamp = :timestamp WHERE packageName = :packageName")
    suspend fun recordAppLaunch(packageName: String, timestamp: Long)

    @Query("UPDATE app_usage SET isPinnedToDock = :isPinned WHERE packageName = :packageName")
    suspend fun setPinnedToDock(packageName: String, isPinned: Boolean)

    @Query("UPDATE app_usage SET isLocked = :isLocked WHERE packageName = :packageName")
    suspend fun setAppLocked(packageName: String, isLocked: Boolean)

    @Query("UPDATE app_usage SET isHidden = :isHidden WHERE packageName = :packageName")
    suspend fun setAppHidden(packageName: String, isHidden: Boolean)

    @Query("UPDATE app_usage SET categoryName = :categoryName WHERE packageName = :packageName")
    suspend fun updateAppCategory(packageName: String, categoryName: String)

    @Query("SELECT * FROM launcher_config WHERE id = 1 LIMIT 1")
    fun getLauncherConfig(): Flow<LauncherConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLauncherConfig(config: LauncherConfigEntity)
}
