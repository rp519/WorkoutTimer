package com.stopwatch.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val keepScreenOn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.KEEP_SCREEN_ON] ?: true // Default ON
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_EMAIL]
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEEP_SCREEN_ON] = enabled
        }
    }

    suspend fun setUserEmail(email: String?) {
        context.dataStore.edit { preferences ->
            if (email != null) {
                preferences[PreferencesKeys.USER_EMAIL] = email
            } else {
                preferences.remove(PreferencesKeys.USER_EMAIL)
            }
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }
}
