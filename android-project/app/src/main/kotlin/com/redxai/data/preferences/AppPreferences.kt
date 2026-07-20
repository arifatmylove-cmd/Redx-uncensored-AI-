package com.redxai.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "redx_prefs")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val OPENROUTER_KEY   = stringPreferencesKey("openrouter_key")
        val VENICE_KEY       = stringPreferencesKey("venice_key")
        val AI_PROVIDER      = stringPreferencesKey("ai_provider")   // "venice" | "openrouter"
        val GITHUB_TOKEN     = stringPreferencesKey("github_token")
        val GITHUB_USERNAME  = stringPreferencesKey("github_username")
        val GITHUB_REPO      = stringPreferencesKey("github_repo")
        val DEFAULT_MODEL    = stringPreferencesKey("default_model")
        val FIREBASE_CONFIG  = stringPreferencesKey("firebase_config")
    }

    val openrouterKey:  Flow<String> = context.dataStore.data.map { it[OPENROUTER_KEY]  ?: "" }
    val veniceKey:      Flow<String> = context.dataStore.data.map { it[VENICE_KEY]       ?: "" }
    val aiProvider:     Flow<String> = context.dataStore.data.map { it[AI_PROVIDER]      ?: "venice" }
    val githubToken:    Flow<String> = context.dataStore.data.map { it[GITHUB_TOKEN]     ?: "" }
    val githubUsername: Flow<String> = context.dataStore.data.map { it[GITHUB_USERNAME]  ?: "" }
    val githubRepo:     Flow<String> = context.dataStore.data.map { it[GITHUB_REPO]      ?: "" }
    val defaultModel:   Flow<String> = context.dataStore.data.map { it[DEFAULT_MODEL]    ?: "dolphin-2.9.3-mistral-nemo-12b" }
    val firebaseConfig: Flow<String> = context.dataStore.data.map { it[FIREBASE_CONFIG]  ?: "" }

    suspend fun setOpenrouterKey(v: String)  = context.dataStore.edit { it[OPENROUTER_KEY]  = v }
    suspend fun setVeniceKey(v: String)      = context.dataStore.edit { it[VENICE_KEY]       = v }
    suspend fun setAiProvider(v: String)     = context.dataStore.edit { it[AI_PROVIDER]      = v }
    suspend fun setGithubToken(v: String)    = context.dataStore.edit { it[GITHUB_TOKEN]     = v }
    suspend fun setGithubUsername(v: String) = context.dataStore.edit { it[GITHUB_USERNAME]  = v }
    suspend fun setGithubRepo(v: String)     = context.dataStore.edit { it[GITHUB_REPO]      = v }
    suspend fun setDefaultModel(v: String)   = context.dataStore.edit { it[DEFAULT_MODEL]    = v }
    suspend fun setFirebaseConfig(v: String) = context.dataStore.edit { it[FIREBASE_CONFIG]  = v }
}
