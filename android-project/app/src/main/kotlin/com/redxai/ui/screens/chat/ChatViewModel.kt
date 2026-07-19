package com.redxai.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redxai.data.local.entities.ChatEntity
import com.redxai.data.local.entities.MessageEntity
import com.redxai.data.preferences.AppPreferences
import com.redxai.data.remote.openrouter.RedxModels
import com.redxai.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatListState(
    val chats: List<ChatEntity> = emptyList(),
    val isLoading: Boolean = false
)

data class ChatState(
    val chat: ChatEntity? = null,
    val messages: List<MessageEntity> = emptyList(),
    val isTyping: Boolean = false,
    val error: String? = null,
    val inputText: String = ""
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val repo: ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatListState())
    val state: StateFlow<ChatListState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeChats().collect { chats ->
                _state.value = _state.value.copy(chats = chats)
            }
        }
    }

    fun newChat(onCreated: (Long) -> Unit) = viewModelScope.launch {
        val id = repo.createChat("New Chat")
        onCreated(id)
    }

    fun deleteChat(chatId: Long) = viewModelScope.launch {
        repo.deleteChat(chatId)
    }
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: ChatRepository,
    private val prefs: AppPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    val availableModels = RedxModels.models

    fun loadChat(chatId: Long) = viewModelScope.launch {
        // Load chat entity first so topbar/model picker has data
        val chat = repo.getChatById(chatId)
        _state.value = _state.value.copy(chat = chat)
        repo.observeMessages(chatId).collect { messages ->
            _state.value = _state.value.copy(messages = messages)
        }
    }

    fun setInput(text: String) {
        _state.value = _state.value.copy(inputText = text, error = null)
    }

    fun send(chatId: Long) = viewModelScope.launch {
        val text = _state.value.inputText.trim()
        if (text.isBlank() || _state.value.isTyping) return@launch

        _state.value = _state.value.copy(inputText = "", isTyping = true, error = null)
        try {
            repo.sendMessage(chatId, text)
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = e.message ?: "Error sending message")
        } finally {
            _state.value = _state.value.copy(isTyping = false)
        }
    }

    fun changeModel(chatId: Long, model: String) = viewModelScope.launch {
        repo.updateModel(chatId, model)
    }

    fun renameChat(chatId: Long, title: String) = viewModelScope.launch {
        repo.renameChat(chatId, title)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
