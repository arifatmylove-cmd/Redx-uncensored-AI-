package com.redxai.util

import android.content.Context
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps the CPU awake during long operations (build pipeline, AI code generation).
 * Uses PARTIAL_WAKE_LOCK so the screen can turn off while builds continue in background.
 * Max 30 minutes per acquisition — auto-released after.
 */
@Singleton
class WakeLockManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private var wakeLock: PowerManager.WakeLock? = null

    fun acquire(tag: String = "redxai:build") {
        release() // always release any existing lock first
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag).apply {
            acquire(30 * 60 * 1000L) // 30-minute max safety timeout
        }
    }

    fun release() {
        try {
            wakeLock?.let { if (it.isHeld) it.release() }
        } catch (_: Exception) {}
        wakeLock = null
    }

    val isHeld: Boolean get() = wakeLock?.isHeld == true
}
