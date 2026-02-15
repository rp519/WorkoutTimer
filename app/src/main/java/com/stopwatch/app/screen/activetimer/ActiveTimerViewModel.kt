package com.stopwatch.app.screen.activetimer

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stopwatch.app.data.AppDatabase
import com.stopwatch.app.data.model.CustomExercise
import com.stopwatch.app.data.model.Exercise
import com.stopwatch.app.data.model.WorkoutHistory
import com.stopwatch.app.data.model.WorkoutMode
import com.stopwatch.app.notification.AchievementTracker
import com.stopwatch.app.notification.WorkoutNotificationManager
import com.stopwatch.app.sound.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class TimerPhase {
    COUNTDOWN,  // Initial 3-second countdown
    PREP,       // Preparation time before workout starts (configurable, default 30s)
    WORK,       // Work/exercise phase
    REST,       // Rest phase between exercises
    FINISHED    // Workout complete
}

class ActiveTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val planDao = db.workoutPlanDao()
    private val historyDao = db.workoutHistoryDao()
    private val workoutExerciseDao = db.workoutExerciseDao()
    private val customExerciseDao = db.customExerciseDao()
    private val soundManager = SoundManager(application)
    private val notificationManager = WorkoutNotificationManager(application)
    private val achievementTracker = AchievementTracker(application)

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
    var currentExerciseName by mutableStateOf("")
        private set
    var currentExerciseImagePath by mutableStateOf<String?>(null)
        private set
    var nextExerciseName by mutableStateOf("")
        private set
    var nextExerciseImagePath by mutableStateOf<String?>(null)
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

    var workoutMode by mutableStateOf(WorkoutMode.SIMPLE)
        private set

    private var workSeconds = 0
    private var restSeconds = 0
    private var prepTimeSeconds = 30  // Preparation time before workout starts
    private var orderedExercises = listOf<Exercise>()
    private var orderedCustomExercises = listOf<CustomExercise>()
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
            workSeconds = plan.workSeconds
            restSeconds = plan.restSeconds
            prepTimeSeconds = plan.prepTimeSeconds
            workoutMode = plan.workoutMode

            // Load exercises based on workout mode
            when (workoutMode) {
                WorkoutMode.LIBRARY -> {
                    orderedExercises = workoutExerciseDao.getExercisesForWorkout(planId)
                    totalExercises = if (orderedExercises.isNotEmpty()) {
                        orderedExercises.size
                    } else {
                        // Fallback to exerciseCount for backward compatibility
                        plan.exerciseCount
                    }
                }
                WorkoutMode.CUSTOM -> {
                    orderedCustomExercises = customExerciseDao.getCustomExercisesForWorkout(planId)
                    totalExercises = orderedCustomExercises.size
                }
                WorkoutMode.SIMPLE -> {
                    // SIMPLE mode: use exerciseCount directly
                    totalExercises = plan.exerciseCount
                    orderedExercises = emptyList()
                }
            }

            isLoading = false
            startWorkout()
        }
    }

    private fun startWorkout() {
        timerJob = viewModelScope.launch {
            // Initial 3-second countdown
            phase = TimerPhase.COUNTDOWN
            totalSecondsInPhase = 3
            secondsRemaining = 3
            soundManager.speakGetReady()
            soundManager.playPrepBeep()
            countdown(3)

            // PREP PHASE - Preparation time before workout starts
            phase = TimerPhase.PREP
            totalSecondsInPhase = prepTimeSeconds

            // Show first exercise during prep if available
            when (workoutMode) {
                WorkoutMode.LIBRARY -> {
                    if (orderedExercises.isNotEmpty()) {
                        val firstExercise = orderedExercises[0]
                        currentExerciseName = firstExercise.name
                        currentExerciseImagePath = firstExercise.imagePath
                    } else {
                        currentExerciseName = "Get Ready!"
                        currentExerciseImagePath = null
                    }
                }
                WorkoutMode.CUSTOM -> {
                    if (orderedCustomExercises.isNotEmpty()) {
                        val firstExercise = orderedCustomExercises[0]
                        currentExerciseName = firstExercise.name
                        currentExerciseImagePath = firstExercise.imagePath
                    } else {
                        currentExerciseName = "Get Ready!"
                        currentExerciseImagePath = null
                    }
                }
                WorkoutMode.SIMPLE -> {
                    currentExerciseName = "Get Ready!"
                    currentExerciseImagePath = null
                }
            }

            soundManager.speakGetReady()
            countdown(prepTimeSeconds)
            soundManager.playGoSound()

            workoutStartTimeMs = System.currentTimeMillis()

            for (round in 1..totalRounds) {
                currentRound = round

                for (exercise in 1..totalExercises) {
                    currentExercise = exercise

                    // Set current exercise name, image, and duration based on mode
                    val currentWorkDuration = when (workoutMode) {
                        WorkoutMode.LIBRARY -> {
                            if (orderedExercises.isNotEmpty() && exercise <= orderedExercises.size) {
                                val exerciseData = orderedExercises[exercise - 1]
                                currentExerciseName = exerciseData.name
                                currentExerciseImagePath = exerciseData.imagePath
                            } else {
                                currentExerciseName = "Exercise $exercise"
                                currentExerciseImagePath = null
                            }
                            workSeconds
                        }
                        WorkoutMode.CUSTOM -> {
                            if (orderedCustomExercises.isNotEmpty() && exercise <= orderedCustomExercises.size) {
                                val customExercise = orderedCustomExercises[exercise - 1]
                                currentExerciseName = customExercise.name
                                currentExerciseImagePath = customExercise.imagePath
                                customExercise.durationSeconds
                            } else {
                                currentExerciseName = "Exercise $exercise"
                                currentExerciseImagePath = null
                                workSeconds
                            }
                        }
                        WorkoutMode.SIMPLE -> {
                            currentExerciseName = "Exercise $exercise"
                            currentExerciseImagePath = null
                            workSeconds
                        }
                    }

                    // Calculate NEXT exercise for preview during REST
                    when (workoutMode) {
                        WorkoutMode.LIBRARY -> {
                            if (orderedExercises.isNotEmpty()) {
                                if (exercise < totalExercises && exercise < orderedExercises.size) {
                                    val nextExerciseData = orderedExercises[exercise]
                                    nextExerciseName = nextExerciseData.name
                                    nextExerciseImagePath = nextExerciseData.imagePath
                                } else if (exercise == totalExercises && round < totalRounds) {
                                    val firstExercise = orderedExercises[0]
                                    nextExerciseName = firstExercise.name
                                    nextExerciseImagePath = firstExercise.imagePath
                                } else {
                                    nextExerciseName = "Final Exercise!"
                                    nextExerciseImagePath = null
                                }
                            } else {
                                if (exercise < totalExercises) {
                                    nextExerciseName = "Exercise ${exercise + 1}"
                                    nextExerciseImagePath = null
                                } else {
                                    nextExerciseName = "Final Exercise!"
                                    nextExerciseImagePath = null
                                }
                            }
                        }
                        WorkoutMode.CUSTOM -> {
                            if (orderedCustomExercises.isNotEmpty()) {
                                if (exercise < totalExercises && exercise < orderedCustomExercises.size) {
                                    val nextCustomExercise = orderedCustomExercises[exercise]
                                    nextExerciseName = nextCustomExercise.name
                                    nextExerciseImagePath = nextCustomExercise.imagePath
                                } else if (exercise == totalExercises && round < totalRounds) {
                                    val firstExercise = orderedCustomExercises[0]
                                    nextExerciseName = firstExercise.name
                                    nextExerciseImagePath = firstExercise.imagePath
                                } else {
                                    nextExerciseName = "Final Exercise!"
                                    nextExerciseImagePath = null
                                }
                            } else {
                                if (exercise < totalExercises) {
                                    nextExerciseName = "Exercise ${exercise + 1}"
                                    nextExerciseImagePath = null
                                } else {
                                    nextExerciseName = "Final Exercise!"
                                    nextExerciseImagePath = null
                                }
                            }
                        }
                        WorkoutMode.SIMPLE -> {
                            if (exercise < totalExercises) {
                                nextExerciseName = "Exercise ${exercise + 1}"
                                nextExerciseImagePath = null
                            } else {
                                nextExerciseName = "Final Exercise!"
                                nextExerciseImagePath = null
                            }
                        }
                    }

                    // WORK phase - use currentWorkDuration which may vary for CUSTOM mode
                    phase = TimerPhase.WORK
                    totalSecondsInPhase = currentWorkDuration
                    soundManager.speakStart()
                    countdown(currentWorkDuration)
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

            // Finished — save and navigate to completion screen
            saveHistory()
            soundManager.speakWorkoutComplete()
            soundManager.playWorkoutComplete()
            // Small delay to let sound start and stabilize before navigation
            delay(500L)
            isFinished = true
        }
    }

    private suspend fun countdown(totalSeconds: Int) {
        secondsRemaining = totalSeconds
        val isBeforeVoiceOver = phase == TimerPhase.WORK

        while (secondsRemaining > 0) {
            // Play prep beep exactly at 3 seconds before "Stop" voice-over
            if (isBeforeVoiceOver && secondsRemaining == 3) {
                soundManager.playPrepBeep()
            }
            if (secondsRemaining <= 3) {
                soundManager.playWarningBeep()
            }

            // Split 1-second delay into 100ms chunks for responsive pause
            repeat(10) {
                if (!isPaused) {
                    delay(100L)
                } else {
                    // If paused, wait until resumed
                    waitWhilePaused()
                    return@repeat  // Exit the repeat loop
                }
            }

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

        // Show completion notification
        val totalDuration = getTotalDurationSeconds()
        val minutes = totalDuration / 60
        val seconds = totalDuration % 60
        notificationManager.showWorkoutCompletionNotification(
            workoutName = planName,
            durationMinutes = minutes,
            durationSeconds = seconds,
            roundsCompleted = totalRounds,
            exercisesCompleted = totalExercises
        )

        // Check for achievements
        achievementTracker.checkAndNotifyAchievements()
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
