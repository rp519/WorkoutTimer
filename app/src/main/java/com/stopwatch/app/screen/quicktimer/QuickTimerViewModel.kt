package com.stopwatch.app.screen.quicktimer

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stopwatch.app.sound.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel for Quick Timer - a simple standalone countdown timer
 * without rounds or exercises. Just set time and start countdown.
 */
class QuickTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val soundManager = SoundManager(application)

    // Input state (editable before timer starts)
    var inputMinutes by mutableIntStateOf(1)
        private set
    var inputSeconds by mutableIntStateOf(0)
        private set

    // Timer state
    var totalSecondsRemaining by mutableIntStateOf(0)
        private set
    var isRunning by mutableStateOf(false)
        private set
    var isPaused by mutableStateOf(false)
        private set
    var isFinished by mutableStateOf(false)
        private set

    private var timerJob: Job? = null
    private var totalSecondsInTimer = 0

    fun updateInputMinutes(minutes: Int) {
        inputMinutes = minutes.coerceIn(0, 99)
    }

    fun updateInputSeconds(seconds: Int) {
        inputSeconds = seconds.coerceIn(0, 59)
    }

    fun start() {
        if (inputMinutes == 0 && inputSeconds == 0) return

        val totalSeconds = inputMinutes * 60 + inputSeconds
        totalSecondsInTimer = totalSeconds
        totalSecondsRemaining = totalSeconds
        isRunning = true
        isPaused = false
        isFinished = false

        startCountdown()
    }

    private fun startCountdown() {
        timerJob = viewModelScope.launch {
            while (totalSecondsRemaining > 0 && isRunning) {
                // Play warning beeps in last 3 seconds
                if (totalSecondsRemaining <= 3) {
                    soundManager.playWarningBeep()
                }

                delay(1000L)

                // Wait while paused
                while (isPaused) {
                    delay(100L)
                }

                if (isRunning) {
                    totalSecondsRemaining--
                }
            }

            // Timer finished
            if (totalSecondsRemaining == 0 && isRunning) {
                isRunning = false
                isFinished = true
                soundManager.playWorkoutComplete()
                soundManager.speakWorkoutComplete()
            }
        }
    }

    fun pause() {
        isPaused = true
    }

    fun resume() {
        isPaused = false
    }

    fun reset() {
        timerJob?.cancel()
        timerJob = null
        isRunning = false
        isPaused = false
        isFinished = false
        totalSecondsRemaining = 0
        totalSecondsInTimer = 0
    }

    fun getProgress(): Float {
        return if (totalSecondsInTimer > 0) {
            totalSecondsRemaining.toFloat() / totalSecondsInTimer.toFloat()
        } else {
            1f
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
