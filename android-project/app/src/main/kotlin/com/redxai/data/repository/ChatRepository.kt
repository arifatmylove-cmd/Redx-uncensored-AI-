package com.redxai.data.repository

import com.redxai.data.local.dao.ChatDao
import com.redxai.data.local.dao.MessageDao
import com.redxai.data.local.entities.ChatEntity
import com.redxai.data.local.entities.MessageEntity
import com.redxai.data.preferences.AppPreferences
import com.redxai.data.remote.openrouter.ChatMessage
import com.redxai.data.remote.openrouter.OpenRouterService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val openRouter: OpenRouterService,
    private val prefs: AppPreferences
) {
    fun observeChats(): Flow<List<ChatEntity>> = chatDao.observeChats()
    fun observeMessages(chatId: Long): Flow<List<MessageEntity>> = messageDao.observeMessages(chatId)
    suspend fun getChatById(chatId: Long): ChatEntity? = chatDao.getChatById(chatId)

    suspend fun createChat(title: String, model: String? = null): Long {
        val defaultModel = prefs.defaultModel.first()
        return chatDao.insertChat(ChatEntity(title = title, model = model ?: defaultModel))
    }

    suspend fun deleteChat(chatId: Long) {
        chatDao.deleteMessagesByChatId(chatId)
        chatDao.deleteChatById(chatId)
    }

    suspend fun renameChat(chatId: Long, title: String) {
        val chat = chatDao.getChatById(chatId) ?: return
        chatDao.updateChat(chat.copy(title = title, updatedAt = System.currentTimeMillis()))
    }

    suspend fun updateModel(chatId: Long, model: String) {
        val chat = chatDao.getChatById(chatId) ?: return
        chatDao.updateChat(chat.copy(model = model, updatedAt = System.currentTimeMillis()))
    }

    suspend fun sendMessage(chatId: Long, userText: String): MessageEntity {
        val apiKey = prefs.openrouterKey.first()
        val chat = chatDao.getChatById(chatId) ?: throw IllegalStateException("Chat not found")
        val history = messageDao.getMessages(chatId)

        // Store user message
        val userMsg = MessageEntity(chatId = chatId, role = "user", content = userText)
        messageDao.insertMessage(userMsg)
        chatDao.touchChat(chatId)

        // Build message list for API
        val apiMessages = mutableListOf<ChatMessage>()
        apiMessages.add(ChatMessage("system", openRouter.unrestrictedSystemPrompt))
        history.takeLast(20).forEach { apiMessages.add(ChatMessage(it.role, it.content)) }
        apiMessages.add(ChatMessage("user", userText))

        // Call AI
        val responseText = openRouter.chat(apiKey = apiKey, model = chat.model, messages = apiMessages)

        // Store assistant response
        val assistantMsg = MessageEntity(chatId = chatId, role = "assistant", content = responseText, model = chat.model)
        val id = messageDao.insertMessage(assistantMsg)
        chatDao.touchChat(chatId)

        return assistantMsg.copy(id = id)
    }
}
