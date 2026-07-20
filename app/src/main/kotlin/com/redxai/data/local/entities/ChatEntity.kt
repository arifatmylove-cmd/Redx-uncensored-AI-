package com.redxai.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val model: String = "dolphin-2.9.3-mistral-nemo-12b",   // Venice default (uncensored, free)
    val provider: String = "venice",                          // "venice" | "openrouter"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: Long,
    val role: String,           // user | assistant | system | build
    val content: String,
    val model: String? = null,
    val buildId: Long? = null,  // non-null for build-progress messages
    val createdAt: Long = System.currentTimeMillis()
)
