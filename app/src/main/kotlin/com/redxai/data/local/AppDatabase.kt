package com.redxai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.redxai.data.local.dao.BuildDao
import com.redxai.data.local.dao.ChatDao
import com.redxai.data.local.dao.MessageDao
import com.redxai.data.local.entities.BuildEntity
import com.redxai.data.local.entities.ChatEntity
import com.redxai.data.local.entities.MessageEntity

@Database(
    entities = [ChatEntity::class, MessageEntity::class, BuildEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun buildDao(): BuildDao
}
