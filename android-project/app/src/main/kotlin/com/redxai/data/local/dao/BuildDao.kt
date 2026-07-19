package com.redxai.data.local.dao

import androidx.room.*
import com.redxai.data.local.entities.BuildEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildDao {
    @Query("SELECT * FROM builds ORDER BY createdAt DESC")
    fun observeBuilds(): Flow<List<BuildEntity>>

    @Query("SELECT * FROM builds WHERE id = :id")
    fun observeBuild(id: Long): Flow<BuildEntity?>

    @Query("SELECT * FROM builds WHERE id = :id")
    suspend fun getBuildById(id: Long): BuildEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuild(build: BuildEntity): Long

    @Update
    suspend fun updateBuild(build: BuildEntity)

    @Query("UPDATE builds SET status = :status, updatedAt = :time WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, time: Long = System.currentTimeMillis())

    @Query("UPDATE builds SET runId = :runId, status = :status, updatedAt = :time WHERE id = :id")
    suspend fun updateRunId(id: Long, runId: Long, status: String, time: Long = System.currentTimeMillis())

    @Query("UPDATE builds SET apkUrl = :url, status = 'success', updatedAt = :time WHERE id = :id")
    suspend fun setApkUrl(id: Long, url: String, time: Long = System.currentTimeMillis())

    @Query("UPDATE builds SET logs = :logs, updatedAt = :time WHERE id = :id")
    suspend fun updateLogs(id: Long, logs: String, time: Long = System.currentTimeMillis())

    @Query("UPDATE builds SET fixSummary = :fix, attempt = attempt + 1, status = 'fixing', updatedAt = :time WHERE id = :id")
    suspend fun recordFix(id: Long, fix: String, time: Long = System.currentTimeMillis())
}
