package com.posopensrc.core.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pos_session")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val USER_ID = longPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val FULL_NAME = stringPreferencesKey("full_name")
        val ROLE = stringPreferencesKey("role")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val LANGUAGE = stringPreferencesKey("language")
    }

    val userId: Flow<Long> = context.dataStore.data.map { it[Keys.USER_ID] ?: 0L }
    val currentUserId: Flow<Long> = context.dataStore.data.map { it[Keys.USER_ID] ?: 0L }
    val username: Flow<String> = context.dataStore.data.map { it[Keys.USERNAME] ?: "" }
    val fullName: Flow<String> = context.dataStore.data.map { it[Keys.FULL_NAME] ?: "" }
    val role: Flow<String> = context.dataStore.data.map { it[Keys.ROLE] ?: "kasir" }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[Keys.IS_LOGGED_IN] ?: false }
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { it[Keys.IS_DARK_MODE] ?: false }
    val language: Flow<String> = context.dataStore.data.map { it[Keys.LANGUAGE] ?: "id" }

    suspend fun saveSession(
        userId: Long,
        username: String,
        fullName: String,
        role: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_ID] = userId
            prefs[Keys.USERNAME] = username
            prefs[Keys.FULL_NAME] = fullName
            prefs[Keys.ROLE] = role
            prefs[Keys.IS_LOGGED_IN] = true
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = false
            prefs.remove(Keys.USER_ID)
            prefs.remove(Keys.USERNAME)
            prefs.remove(Keys.FULL_NAME)
            prefs.remove(Keys.ROLE)
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_DARK_MODE] = enabled
        }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = lang
        }
    }
}
