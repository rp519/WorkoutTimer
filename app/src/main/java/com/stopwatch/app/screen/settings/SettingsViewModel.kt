package com.stopwatch.app.screen.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.stopwatch.app.data.UserPreferencesRepository
import com.stopwatch.app.email.EmailDebugHelper
import com.stopwatch.app.email.EmailService
import com.stopwatch.app.email.EmailSummaryWorker
import com.stopwatch.app.notification.DailyReminderWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = UserPreferencesRepository(application)

    val keepScreenOn = preferencesRepository.keepScreenOn.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    val userEmail = preferencesRepository.userEmail.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    val remindersEnabled = preferencesRepository.remindersEnabled.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    val reminderHour = preferencesRepository.reminderHour.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        18
    )

    val reminderMinute = preferencesRepository.reminderMinute.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    val achievementNotificationsEnabled = preferencesRepository.achievementNotificationsEnabled.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    val emailSummaryEnabled = preferencesRepository.emailSummaryEnabled.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val emailFrequency = preferencesRepository.emailFrequency.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "weekly"
    )

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setKeepScreenOn(enabled)
        }
    }

    fun setUserEmail(email: String?) {
        viewModelScope.launch {
            preferencesRepository.setUserEmail(email)
        }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setRemindersEnabled(enabled)
            if (enabled) {
                DailyReminderWorker.schedule(
                    getApplication(),
                    reminderHour.value,
                    reminderMinute.value
                )
            } else {
                DailyReminderWorker.cancel(getApplication())
            }
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            preferencesRepository.setReminderTime(hour, minute)
            if (remindersEnabled.value) {
                DailyReminderWorker.schedule(getApplication(), hour, minute)
            }
        }
    }

    fun setAchievementNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAchievementNotificationsEnabled(enabled)
        }
    }

    fun setEmailSummaryEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setEmailSummaryEnabled(enabled)
            if (enabled) {
                EmailSummaryWorker.schedule(getApplication())
            } else {
                EmailSummaryWorker.cancel(getApplication())
            }
        }
    }

    fun setEmailFrequency(frequency: String) {
        viewModelScope.launch {
            preferencesRepository.setEmailFrequency(frequency)
        }
    }
    // Debug function - shows what would be in the email
    fun getDebugInfo(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val info = EmailDebugHelper.debugEmailData(getApplication())
            onResult(info)
        }
    }
}
