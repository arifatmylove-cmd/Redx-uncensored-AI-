package com.redxai.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "builds")
data class BuildEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appName: String,
    val description: String,
    val packageName: String,
    val status: String = BuildStatus.QUEUED, // queued|pushing|running|fixing|success|failed
    val runId: Long? = null,
    val attempt: Int = 1,
    val apkUrl: String? = null,
    val logs: String? = null,
    val fixSummary: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

object BuildStatus {
    const val QUEUED = "queued"
    const val PUSHING = "pushing"
    const val RUNNING = "running"
    const val FIXING = "fixing"
    const val SUCCESS = "success"
    const val FAILED = "failed"
}
