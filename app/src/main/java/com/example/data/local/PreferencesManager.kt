package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "taka_reward_prefs")

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

enum class LanguageCode {
    EN, BN
}

class PreferencesManager(private val context: Context) {

    private object Keys {
        val LOGGED_IN_USER_ID = longPreferencesKey("logged_in_user_id")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val LAST_CHECKIN_DATE = stringPreferencesKey("last_checkin_date")
        val CHECKIN_STREAK = longPreferencesKey("checkin_streak")
    }

    val loggedInUserIdFlow: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[Keys.LOGGED_IN_USER_ID] ?: 1L // Default to seeded user for instant usability
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val raw = preferences[Keys.THEME_MODE] ?: ThemeMode.DARK.name
        try {
            ThemeMode.valueOf(raw)
        } catch (_: Exception) {
            ThemeMode.DARK
        }
    }

    val languageFlow: Flow<LanguageCode> = context.dataStore.data.map { preferences ->
        val raw = preferences[Keys.APP_LANGUAGE] ?: LanguageCode.BN.name
        try {
            LanguageCode.valueOf(raw)
        } catch (_: Exception) {
            LanguageCode.BN
        }
    }

    val checkInStreakFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[Keys.CHECKIN_STREAK] ?: 0L
    }

    val lastCheckInDateFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[Keys.LAST_CHECKIN_DATE]
    }

    suspend fun setLoggedInUserId(userId: Long?) {
        context.dataStore.edit { preferences ->
            if (userId != null) {
                preferences[Keys.LOGGED_IN_USER_ID] = userId
            } else {
                preferences.remove(Keys.LOGGED_IN_USER_ID)
            }
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setLanguage(lang: LanguageCode) {
        context.dataStore.edit { preferences ->
            preferences[Keys.APP_LANGUAGE] = lang.name
        }
    }

    suspend fun recordCheckIn(dateStr: String, newStreak: Long) {
        context.dataStore.edit { preferences ->
            preferences[Keys.LAST_CHECKIN_DATE] = dateStr
            preferences[Keys.CHECKIN_STREAK] = newStreak
        }
    }
}
