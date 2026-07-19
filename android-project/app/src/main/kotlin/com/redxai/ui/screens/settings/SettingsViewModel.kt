package com.redxai.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redxai.data.preferences.AppPreferences
import com.redxai.data.remote.github.GitHubService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val openrouterKey: String = "",
    val defaultModel: String = "cognitivecomputations/dolphin-llama-3-70b",
    val githubToken: String = "",
    val githubUsername: String = "",
    val githubRepo: String = "",
    val firebaseConfig: String = "",
    val isSaving: Boolean = false,
    val isVerifying: Boolean = false,
    val aiSaveMessage: String? = null,
    val githubMessage: String? = null,
    val firebaseMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPreferences,
    private val gitHub: GitHubService
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                prefs.openrouterKey,
                prefs.defaultModel,
                prefs.githubToken,
                prefs.githubUsername
            ) { key, model, token, user -> listOf(key, model, token, user) }.collect { values ->
                _state.value = _state.value.copy(
                    openrouterKey = values[0],
                    defaultModel = values[1],
                    githubToken = values[2],
                    githubUsername = values[3]
                )
            }
        }
        viewModelScope.launch {
            combine(prefs.githubRepo, prefs.firebaseConfig) { repo, firebase -> Pair(repo, firebase) }
                .collect { (repo, firebase) ->
                    _state.value = _state.value.copy(githubRepo = repo, firebaseConfig = firebase)
                }
        }
    }

    fun setOpenrouterKey(v: String) { _state.value = _state.value.copy(openrouterKey = v, aiSaveMessage = null) }
    fun setDefaultModel(v: String) { _state.value = _state.value.copy(defaultModel = v) }
    fun setGithubToken(v: String) { _state.value = _state.value.copy(githubToken = v, githubMessage = null) }
    fun setGithubUsername(v: String) { _state.value = _state.value.copy(githubUsername = v) }
    fun setGithubRepo(v: String) { _state.value = _state.value.copy(githubRepo = v) }
    fun setFirebaseConfig(v: String) { _state.value = _state.value.copy(firebaseConfig = v, firebaseMessage = null) }

    fun saveAiSettings() = viewModelScope.launch {
        val s = _state.value
        _state.value = s.copy(isSaving = true)
        try {
            prefs.setOpenrouterKey(s.openrouterKey)
            prefs.setDefaultModel(s.defaultModel)
            _state.value = _state.value.copy(isSaving = false, aiSaveMessage = "✓ Saved successfully")
        } catch (e: Exception) {
            _state.value = _state.value.copy(isSaving = false, aiSaveMessage = "Error: ${e.message}")
        }
    }

    fun saveGitHubSettings() = viewModelScope.launch {
        val s = _state.value
        prefs.setGithubToken(s.githubToken)
        prefs.setGithubUsername(s.githubUsername)
        prefs.setGithubRepo(s.githubRepo)
        _state.value = _state.value.copy(githubMessage = "✓ GitHub settings saved")
    }

    fun verifyGitHub() = viewModelScope.launch {
        val s = _state.value
        _state.value = s.copy(isVerifying = true, githubMessage = "Verifying...")
        try {
            prefs.setGithubToken(s.githubToken)
            prefs.setGithubUsername(s.githubUsername)
            prefs.setGithubRepo(s.githubRepo)
            val result = gitHub.verifyToken(s.githubToken, s.githubUsername, s.githubRepo)
            _state.value = _state.value.copy(
                isVerifying = false,
                githubMessage = result.fold(
                    onSuccess = { "✓ Connected as ${s.githubUsername}" },
                    onFailure = { "✗ ${it.message}" }
                )
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(isVerifying = false, githubMessage = "✗ ${e.message}")
        }
    }

    fun saveFirebaseConfig() = viewModelScope.launch {
        prefs.setFirebaseConfig(_state.value.firebaseConfig)
        _state.value = _state.value.copy(firebaseMessage = "✓ Firebase config saved")
    }
}
