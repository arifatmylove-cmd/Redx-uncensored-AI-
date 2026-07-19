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
        val OPENROUTER_KEY = stringPreferencesKey("openrouter_key")
        val GITHUB_TOKEN = stringPreferencesKey("github_token")
        val GITHUB_USERNAME = stringPreferencesKey("github_username")
        val GITHUB_REPO = stringPreferencesKey("github_repo")
        val DEFAULT_MODEL = stringPreferencesKey("default_model")
        val FIREBASE_CONFIG = stringPreferencesKey("firebase_config")
    }

    val openrouterKey: Flow<String> = context.dataStore.data.map { it[OPENROUTER_KEY] ?: "" }
    val githubToken: Flow<String> = context.dataStore.data.map { it[GITHUB_TOKEN] ?: "" }
    val githubUsername: Flow<String> = context.dataStore.data.map { it[GITHUB_USERNAME] ?: "" }
    val githubRepo: Flow<String> = context.dataStore.data.map { it[GITHUB_REPO] ?: "" }
    val defaultModel: Flow<String> = context.dataStore.data.map { it[DEFAULT_MODEL] ?: "google/gemini-2.0-flash-exp:free" }
    val firebaseConfig: Flow<String> = context.dataStore.data.map { it[FIREBASE_CONFIG] ?: "" }

    suspend fun setOpenrouterKey(value: String) = context.dataStore.edit { it[OPENROUTER_KEY] = value }
    suspend fun setGithubToken(value: String) = context.dataStore.edit { it[GITHUB_TOKEN] = value }
    suspend fun setGithubUsername(value: String) = context.dataStore.edit { it[GITHUB_USERNAME] = value }
    suspend fun setGithubRepo(value: String) = context.dataStore.edit { it[GITHUB_REPO] = value }
    suspend fun setDefaultModel(value: String) = context.dataStore.edit { it[DEFAULT_MODEL] = value }
    suspend fun setFirebaseConfig(value: String) = context.dataStore.edit { it[FIREBASE_CONFIG] = value }
}
