package com.stopwatch.app.screen.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stopwatch.app.data.UserPreferencesRepository
import kotlinx.coroutines.launch

class EmailOnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = UserPreferencesRepository(application)

    fun saveEmail(email: String) {
        viewModelScope.launch {
            preferencesRepository.setUserEmail(email)
            preferencesRepository.setOnboardingCompleted(true)
        }
    }

    fun skipOnboarding() {
        viewModelScope.launch {
            preferencesRepository.setOnboardingCompleted(true)
        }
    }
}
