package com.redxai.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redxai.data.local.entities.ChatEntity
import com.redxai.data.local.entities.MessageEntity
import com.redxai.data.preferences.AppPreferences
import com.redxai.data.remote.openrouter.RedxModels
import com.redxai.data.local.entities.BuildStatus
import com.redxai.data.remote.venice.VeniceModels
import com.redxai.data.repository.BuildRepository
import com.redxai.data.repository.ChatRepository
import com.redxai.util.WakeLockManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    val inputText: String = "",
    val provider: String = "venice",
    val isBuildRunning: Boolean = false
)

data class UnifiedModelEntry(
    val id: String,
    val name: String,
    val description: String,
    val provider: String    // "venice" | "openrouter"
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
    private val buildRepo: BuildRepository,
    private val prefs: AppPreferences,
    private val wakeLock: WakeLockManager
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    // Venice models first, then OpenRouter
    val allModels: List<UnifiedModelEntry> =
        VeniceModels.models.map { UnifiedModelEntry(it.id, "Venice · ${it.name}", it.description, "venice") } +
        RedxModels.models.map  { UnifiedModelEntry(it.id, "OpenRouter · ${it.name}", it.description, "openrouter") }

    fun loadChat(chatId: Long) = viewModelScope.launch {
        // Load chat entity immediately
        val chat = repo.getChatById(chatId)
        _state.value = _state.value.copy(chat = chat)

        // Watch messages live
        launch {
            repo.observeMessages(chatId).collect { messages ->
                _state.value = _state.value.copy(messages = messages)
            }
        }
        // Watch provider preference live
        launch {
            prefs.aiProvider.collect { provider ->
                _state.value = _state.value.copy(provider = provider)
            }
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

            // Check if AI triggered a build inside its response
            val pending = repo.extractPendingBuild(chatId)
            if (pending != null) {
                val (msgId, appName, description) = pending
                runBuildPipeline(chatId, msgId, appName, description)
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = e.message ?: "Error sending message")
        } finally {
            _state.value = _state.value.copy(isTyping = false)
        }
    }

    private fun runBuildPipeline(chatId: Long, msgId: Long, appName: String, description: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isBuildRunning = true)
            wakeLock.acquire("redxai:build:$appName")

            try {
                repo.updateBuildMessage(msgId, chatId,
                    "🔨 **Starting build: $appName**\n\nGenerating full Kotlin source code with AI… this takes 1–2 minutes.")

                val buildId = buildRepo.startBuild(appName, description, null)

                repo.updateBuildMessage(msgId, chatId,
                    "📝 **Code generated!** Pushing ${appName} files to GitHub…")

                buildRepo.pushAndBuild(buildId, null)

                val username = buildRepo.getGithubUsername()
                val repoName = buildRepo.getGithubRepo()
                repo.updateBuildMessage(msgId, chatId,
                    "⚙️ **Build running on GitHub Actions**\n\n" +
                    "This takes 4–8 minutes. I'll update you automatically.\n\n" +
                    "👉 [Watch live progress](https://github.com/$username/$repoName/actions)")

                // Poll with wake lock held — phone won't sleep during build
                var attempts = 0
                while (attempts < 72) {   // 72 × 10s = 12 minutes max
                    delay(10_000)
                    val build = buildRepo.pollAndFix(buildId) ?: break
                    when (build.status) {
                        com.redxai.data.local.entities.BuildStatus.SUCCESS -> {
                            val url = build.apkUrl ?: "https://github.com/$username/$repoName/actions"
                            repo.updateBuildMessage(msgId, chatId,
                                "✅ **$appName built successfully!**\n\n" +
                                "Your APK is ready to download from GitHub Actions → Artifacts.\n\n" +
                                "👉 [Download APK](${url})\n\n" +
                                "_Transfer to your phone and install — enable 'Unknown sources' in Settings if prompted._")
                            break
                        }
                        com.redxai.data.local.entities.BuildStatus.FAILED -> {
                            repo.updateBuildMessage(msgId, chatId,
                                "❌ **$appName build failed** after ${build.attempt} auto-fix attempts.\n\n" +
                                "${build.logs?.lines()?.takeLast(5)?.joinToString("\n") ?: "Check GitHub Actions for details."}\n\n" +
                                "👉 [View logs](https://github.com/$username/$repoName/actions)\n\n" +
                                "_Try describing the app differently and I'll generate a new version._")
                            break
                        }
                        com.redxai.data.local.entities.BuildStatus.FIXING -> {
                            repo.updateBuildMessage(msgId, chatId,
                                "🔧 **Auto-fixing errors in $appName** (attempt ${build.attempt}/5)…\n\n" +
                                "AI detected a compile error and is rewriting the affected files automatically.")
                        }
                        else -> { /* still running — keep polling */ }
                    }
                    attempts++
                }

                if (attempts >= 72) {
                    repo.updateBuildMessage(msgId, chatId,
                        "⏱️ **$appName build timed out.**\n\n" +
                        "👉 [Check GitHub Actions](https://github.com/$username/$repoName/actions)")
                }

            } catch (e: Exception) {
                repo.updateBuildMessage(msgId, chatId,
                    "❌ **Build error:** ${e.message}\n\n" +
                    "_Check Settings → GitHub Integration and make sure your token and repo name are correct._")
            } finally {
                wakeLock.release()
                _state.value = _state.value.copy(isBuildRunning = false)
            }
        }
    }

    fun changeModel(chatId: Long, model: String, provider: String) = viewModelScope.launch {
        repo.updateModel(chatId, model)
        prefs.setAiProvider(provider)
        prefs.setDefaultModel(model)
        val chat = repo.getChatById(chatId)
        _state.value = _state.value.copy(chat = chat, provider = provider)
    }

    fun renameChat(chatId: Long, title: String) = viewModelScope.launch {
        repo.renameChat(chatId, title)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        wakeLock.release()
    }
}
