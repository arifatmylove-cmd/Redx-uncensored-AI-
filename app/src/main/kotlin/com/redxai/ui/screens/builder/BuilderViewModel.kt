package com.redxai.ui.screens.builder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redxai.data.local.entities.BuildEntity
import com.redxai.data.repository.BuildRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BuilderState(
    val builds: List<BuildEntity> = emptyList(),
    val appName: String = "",
    val description: String = "",
    val selectedModel: String = "cognitivecomputations/dolphin-llama-3-70b",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val activeBuildId: Long? = null
)

@HiltViewModel
class BuilderViewModel @Inject constructor(
    private val repo: BuildRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BuilderState())
    val state: StateFlow<BuilderState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeBuilds().collect { builds ->
                _state.value = _state.value.copy(builds = builds)
            }
        }
    }

    fun setAppName(v: String) = _state.value.let { _state.value = it.copy(appName = v, error = null) }
    fun setDescription(v: String) = _state.value.let { _state.value = it.copy(description = v, error = null) }
    fun setModel(v: String) = _state.value.let { _state.value = it.copy(selectedModel = v) }

    fun startBuild() = viewModelScope.launch {
        val s = _state.value
        if (s.appName.isBlank()) { _state.value = s.copy(error = "App name is required"); return@launch }
        if (s.description.isBlank()) { _state.value = s.copy(error = "Description is required"); return@launch }

        _state.value = s.copy(isSubmitting = true, error = null)
        try {
            val buildId = repo.startBuild(s.appName, s.description, s.selectedModel)
            _state.value = _state.value.copy(isSubmitting = false, activeBuildId = buildId, appName = "", description = "")
            // Start the push + build pipeline
            repo.pushAndBuild(buildId, s.selectedModel)
            // Start polling
            startPolling(buildId)
        } catch (e: Exception) {
            _state.value = _state.value.copy(isSubmitting = false, error = e.message ?: "Failed to start build")
        }
    }

    fun pollBuild(buildId: Long) = viewModelScope.launch {
        try {
            repo.pollAndFix(buildId)
        } catch (e: Exception) {
            // ignore poll errors silently
        }
    }

    private fun startPolling(buildId: Long) = viewModelScope.launch {
        var attempts = 0
        while (attempts < 120) { // Poll up to 120 times (every 10s = 20 min max)
            delay(10_000)
            val build = repo.pollAndFix(buildId) ?: break
            if (build.status == "success" || (build.status == "failed" && build.attempt >= 5)) break
            attempts++
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
}
