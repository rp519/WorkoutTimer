package com.stopwatch.app.screen.activetimer

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stopwatch.app.data.AppDatabase
import com.stopwatch.app.data.model.WorkoutHistory
import com.stopwatch.app.sound.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class TimerPhase { COUNTDOWN, WORK, REST, FINISHED }

class ActiveTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val planDao = db.workoutPlanDao()
    private val historyDao = db.workoutHistoryDao()
    private val soundManager = SoundManager(application)

    var planName by mutableStateOf("")
        private set
    var currentRound by mutableIntStateOf(1)
        private set
    var totalRounds by mutableIntStateOf(1)
        private set
    var currentExercise by mutableIntStateOf(1)
        private set
    var totalExercises by mutableIntStateOf(1)
        private set
    var phase by mutableStateOf(TimerPhase.COUNTDOWN)
        private set
    var secondsRemaining by mutableIntStateOf(3)
        private set
    var totalSecondsInPhase by mutableIntStateOf(3)
        private set
    var isPaused by mutableStateOf(false)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var isFinished by mutableStateOf(false)
        private set

    private var workSeconds = 0
    private var restSeconds = 0
    private var timerJob: Job? = null
    private var workoutStartTimeMs = 0L
    private var totalPausedMs = 0L
    private var pauseStartMs = 0L
    private var planId = 0L

    fun load(planId: Long) {
        this.planId = planId
        viewModelScope.launch {
            val plan = planDao.getById(planId) ?: return@launch
            planName = plan.name
            totalRounds = plan.rounds
            totalExercises = plan.exerciseCount
            workSeconds = plan.workSeconds
            restSeconds = plan.restSeconds
            isLoading = false
            startWorkout()
        }
    }

    private fun startWorkout() {
        timerJob = viewModelScope.launch {
            // 3-second countdown
            phase = TimerPhase.COUNTDOWN
            totalSecondsInPhase = 3
            secondsRemaining = 3
            soundManager.speakGetReady()
            while (secondsRemaining > 0) {
                soundManager.playCountdownBeep()
                delay(1000L)
                waitWhilePaused()
                secondsRemaining--
            }
            soundManager.playGoSound()
            workoutStartTimeMs = System.currentTimeMillis()

            for (round in 1..totalRounds) {
                currentRound = round

                for (exercise in 1..totalExercises) {
                    currentExercise = exercise

                    // WORK phase
                    phase = TimerPhase.WORK
                    totalSecondsInPhase = workSeconds
                    soundManager.speakStart()
                    countdown(workSeconds)
                    soundManager.speakStop()

                    // REST between exercises (skip after last exercise of the round)
                    if (restSeconds > 0 && exercise < totalExercises) {
                        phase = TimerPhase.REST
                        totalSecondsInPhase = restSeconds
                        countdown(restSeconds)
                    }
                }

                // REST between rounds (skip after last round)
                if (restSeconds > 0 && round < totalRounds) {
                    soundManager.playRoundEnd()
                    soundManager.speakRoundBreak(round, totalRounds)
                    phase = TimerPhase.REST
                    totalSecondsInPhase = restSeconds
                    countdown(restSeconds)
                }
            }

            // Finished — play sounds and save before navigating away
            phase = TimerPhase.FINISHED
            saveHistory()
            soundManager.speakWorkoutComplete()
            soundManager.playWorkoutComplete()
            delay(3000L) // let TTS finish before navigation
            isFinished = true
        }
    }

    private suspend fun countdown(totalSeconds: Int) {
        secondsRemaining = totalSeconds
        while (secondsRemaining > 0) {
            if (secondsRemaining <= 3) {
                soundManager.playWarningBeep()
            }
            delay(1000L)
            waitWhilePaused()
            secondsRemaining--
        }
    }

    private suspend fun waitWhilePaused() {
        while (isPaused) {
            delay(100L)
        }
    }

    fun togglePause() {
        if (isPaused) {
            totalPausedMs += System.currentTimeMillis() - pauseStartMs
            isPaused = false
        } else {
            pauseStartMs = System.currentTimeMillis()
            isPaused = true
        }
    }

    fun abandon() {
        timerJob?.cancel()
        timerJob = null
    }

    fun getTotalDurationSeconds(): Int {
        val paused = if (isPaused) System.currentTimeMillis() - pauseStartMs else 0L
        val elapsed = System.currentTimeMillis() - workoutStartTimeMs - totalPausedMs - paused
        return (elapsed / 1000).toInt().coerceAtLeast(0)
    }

    private suspend fun saveHistory() {
        historyDao.insert(
            WorkoutHistory(
                planId = planId,
                planName = planName,
                completedAt = System.currentTimeMillis(),
                totalDurationSeconds = getTotalDurationSeconds(),
                roundsCompleted = totalRounds
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
