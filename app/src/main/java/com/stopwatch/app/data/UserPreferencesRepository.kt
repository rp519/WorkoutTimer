package com.stopwatch.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
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
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val ACHIEVEMENT_NOTIFICATIONS_ENABLED = booleanPreferencesKey("achievement_notifications_enabled")
        val EMAIL_SUMMARY_ENABLED = booleanPreferencesKey("email_summary_enabled")
        val EMAIL_FREQUENCY = stringPreferencesKey("email_frequency") // "weekly", "biweekly", "monthly"
        val LAST_EMAIL_SENT = longPreferencesKey("last_email_sent")
        val SHOW_QUICK_TIMER = booleanPreferencesKey("show_quick_timer")
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

    val remindersEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMINDERS_ENABLED] ?: true
    }

    val reminderHour: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMINDER_HOUR] ?: 18 // Default 6 PM
    }

    val reminderMinute: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMINDER_MINUTE] ?: 0
    }

    val achievementNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ACHIEVEMENT_NOTIFICATIONS_ENABLED] ?: true
    }

    val emailSummaryEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.EMAIL_SUMMARY_ENABLED] ?: false
    }

    val emailFrequency: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.EMAIL_FREQUENCY] ?: "weekly"
    }

    val lastEmailSent: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_EMAIL_SENT] ?: 0L
    }

    val showQuickTimer: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_QUICK_TIMER] ?: true // Default ON for existing users
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

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDERS_ENABLED] = enabled
        }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_HOUR] = hour
            preferences[PreferencesKeys.REMINDER_MINUTE] = minute
        }
    }

    suspend fun setAchievementNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACHIEVEMENT_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setEmailSummaryEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EMAIL_SUMMARY_ENABLED] = enabled
        }
    }

    suspend fun setEmailFrequency(frequency: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EMAIL_FREQUENCY] = frequency
        }
    }

    suspend fun setLastEmailSent(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_EMAIL_SENT] = timestamp
        }
    }

    suspend fun setShowQuickTimer(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_QUICK_TIMER] = enabled
        }
    }
}
