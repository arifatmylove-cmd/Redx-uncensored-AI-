package com.redxai.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redxai.data.preferences.AppPreferences
import com.redxai.data.remote.github.GitHubService
import com.redxai.data.remote.venice.VeniceService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    // AI
    val veniceKey:        String  = "",
    val openrouterKey:    String  = "",
    val aiProvider:       String  = "venice",
    val defaultModel:     String  = "dolphin-2.9.3-mistral-nemo-12b",
    // GitHub
    val githubToken:      String  = "",
    val githubUsername:   String  = "",
    val githubRepo:       String  = "",
    // Firebase
    val firebaseConfig:   String  = "",
    // UI state
    val isSaving:         Boolean = false,
    val isTesting:        Boolean = false,
    val isVerifying:      Boolean = false,
    val aiSaveMessage:    String? = null,
    val aiTestMessage:    String? = null,
    val githubMessage:    String? = null,
    val firebaseMessage:  String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPreferences,
    private val gitHub: GitHubService,
    private val veniceService: VeniceService
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                prefs.veniceKey,
                prefs.openrouterKey,
                prefs.aiProvider,
                prefs.defaultModel
            ) { venice, or, prov, model -> listOf(venice, or, prov, model) }
                .collect { v ->
                    _state.value = _state.value.copy(
                        veniceKey     = v[0],
                        openrouterKey = v[1],
                        aiProvider    = v[2],
                        defaultModel  = v[3]
                    )
                }
        }
        viewModelScope.launch {
            combine(
                prefs.githubToken,
                prefs.githubUsername,
                prefs.githubRepo,
                prefs.firebaseConfig
            ) { tok, usr, repo, fb -> listOf(tok, usr, repo, fb) }
                .collect { v ->
                    _state.value = _state.value.copy(
                        githubToken    = v[0],
                        githubUsername = v[1],
                        githubRepo     = v[2],
                        firebaseConfig = v[3]
                    )
                }
        }
    }

    // ── Field setters ──────────────────────────────────────────────────────────
    fun setVeniceKey(v: String)      { _state.value = _state.value.copy(veniceKey = v, aiSaveMessage = null, aiTestMessage = null) }
    fun setOpenrouterKey(v: String)  { _state.value = _state.value.copy(openrouterKey = v, aiSaveMessage = null, aiTestMessage = null) }
    fun setAiProvider(v: String)     { _state.value = _state.value.copy(aiProvider = v, aiSaveMessage = null, aiTestMessage = null) }
    fun setDefaultModel(v: String)   { _state.value = _state.value.copy(defaultModel = v) }
    fun setGithubToken(v: String)    { _state.value = _state.value.copy(githubToken = v, githubMessage = null) }
    fun setGithubUsername(v: String) { _state.value = _state.value.copy(githubUsername = v) }
    fun setGithubRepo(v: String)     { _state.value = _state.value.copy(githubRepo = v) }
    fun setFirebaseConfig(v: String) { _state.value = _state.value.copy(firebaseConfig = v, firebaseMessage = null) }

    // ── Save AI settings ───────────────────────────────────────────────────────
    fun saveAiSettings() = viewModelScope.launch {
        val s = _state.value
        _state.value = s.copy(isSaving = true)
        try {
            // Always trim — pasted keys often have trailing whitespace
            prefs.setVeniceKey(s.veniceKey.trim())
            prefs.setOpenrouterKey(s.openrouterKey.trim())
            prefs.setAiProvider(s.aiProvider)
            prefs.setDefaultModel(
                if (s.aiProvider == "venice") "dolphin-2.9.3-mistral-nemo-12b"
                else "google/gemini-2.0-flash-exp:free"
            )
            _state.value = _state.value.copy(
                isSaving      = false,
                aiSaveMessage = "✓ Saved! Now tap 'Test Connection' to verify your key works."
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(isSaving = false, aiSaveMessage = "✗ Save failed: ${e.message}")
        }
    }

    // ── Test AI connection ─────────────────────────────────────────────────────
    fun testVeniceConnection() = viewModelScope.launch {
        val s = _state.value
        _state.value = s.copy(isTesting = true, aiTestMessage = "Testing connection…")
        try {
            val key = s.veniceKey.trim()
            if (key.isBlank()) {
                _state.value = _state.value.copy(isTesting = false, aiTestMessage = "✗ No key entered — paste your Venice.ai key first")
                return@launch
            }
            // Save first so the trimmed key is persisted
            prefs.setVeniceKey(key)
            val result = veniceService.testKey(key)
            _state.value = _state.value.copy(
                isTesting      = false,
                aiTestMessage  = result.fold(
                    onSuccess = { it },
                    onFailure = { it.message ?: "✗ Connection failed" }
                )
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(isTesting = false, aiTestMessage = "✗ ${e.message}")
        }
    }

    // ── GitHub settings ───────────────────────────────────────────────────────
    fun saveGitHubSettings() = viewModelScope.launch {
        val s = _state.value
        prefs.setGithubToken(s.githubToken.trim())
        prefs.setGithubUsername(s.githubUsername.trim())
        prefs.setGithubRepo(s.githubRepo.trim())
        _state.value = _state.value.copy(githubMessage = "✓ GitHub settings saved")
    }

    fun verifyGitHub() = viewModelScope.launch {
        val s = _state.value
        _state.value = s.copy(isVerifying = true, githubMessage = "Verifying…")
        try {
            prefs.setGithubToken(s.githubToken.trim())
            prefs.setGithubUsername(s.githubUsername.trim())
            prefs.setGithubRepo(s.githubRepo.trim())
            val result = gitHub.verifyToken(s.githubToken.trim(), s.githubUsername.trim(), s.githubRepo.trim())
            _state.value = _state.value.copy(
                isVerifying   = false,
                githubMessage = result.fold(
                    onSuccess = { "✓ Connected as ${s.githubUsername.trim()} — repo found!" },
                    onFailure = { "✗ ${it.message}" }
                )
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(isVerifying = false, githubMessage = "✗ ${e.message}")
        }
    }

    // ── Firebase ───────────────────────────────────────────────────────────────
    fun saveFirebaseConfig() = viewModelScope.launch {
        prefs.setFirebaseConfig(_state.value.firebaseConfig)
        _state.value = _state.value.copy(firebaseMessage = "✓ Firebase config saved")
    }
}
