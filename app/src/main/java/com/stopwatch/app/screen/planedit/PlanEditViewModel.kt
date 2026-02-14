package com.stopwatch.app.screen.planedit

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
import com.stopwatch.app.data.model.WorkoutMode
import com.stopwatch.app.data.model.WorkoutPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PlanEditViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val planDao = database.workoutPlanDao()
    private val exerciseDao = database.exerciseDao()
    private val workoutExerciseDao = database.workoutExerciseDao()
    private val customExerciseDao = database.customExerciseDao()

    var planName by mutableStateOf("")
        private set
    var rounds by mutableIntStateOf(3)
        private set
    var exerciseCount by mutableIntStateOf(5)
        private set
    var workSeconds by mutableIntStateOf(30)
        private set
    var restSeconds by mutableIntStateOf(10)
        private set
    var prepTimeSeconds by mutableIntStateOf(30)
        private set

    var workoutMode by mutableStateOf(WorkoutMode.SIMPLE)
        private set

    var selectedExercises by mutableStateOf<List<Exercise>>(emptyList())
        private set

    var customExercises by mutableStateOf<List<CustomExercise>>(emptyList())
        private set

    var categories by mutableStateOf<List<String>>(emptyList())
        private set

    val allExercises: Flow<List<Exercise>> = exerciseDao.getAllExercises()

    var isEditing by mutableStateOf(false)
        private set
    private var editingPlanId: Long = -1L

    var isSaved by mutableStateOf(false)
        private set

    init {
        // Load categories on init
        viewModelScope.launch {
            categories = exerciseDao.getAllCategories()
        }
    }

    fun loadPlan(planId: Long) {
        if (planId <= 0) return
        viewModelScope.launch {
            val plan = planDao.getById(planId) ?: return@launch
            editingPlanId = planId
            isEditing = true
            planName = plan.name
            rounds = plan.rounds
            exerciseCount = plan.exerciseCount
            workSeconds = plan.workSeconds
            restSeconds = plan.restSeconds
            prepTimeSeconds = plan.prepTimeSeconds
            workoutMode = plan.workoutMode

            // Load associated exercises based on workout mode
            when (plan.workoutMode) {
                WorkoutMode.LIBRARY -> {
                    val exercises = workoutExerciseDao.getExercisesForWorkout(planId)
                    selectedExercises = exercises
                }
                WorkoutMode.CUSTOM -> {
                    val customs = customExerciseDao.getCustomExercisesForWorkout(planId)
                    customExercises = customs
                }
                WorkoutMode.SIMPLE -> {
                    // No exercises to load for SIMPLE mode
                }
            }
        }
    }

    fun updatePlanName(name: String) { planName = name }
    fun updateRounds(r: Int) { rounds = r.coerceAtLeast(1) }
    fun updateExerciseCount(c: Int) { exerciseCount = c.coerceAtLeast(1) }
    fun updateWorkSeconds(s: Int) { workSeconds = s.coerceAtLeast(1) }
    fun updateRestSeconds(s: Int) { restSeconds = s.coerceAtLeast(0) }
    fun updatePrepTimeSeconds(s: Int) { prepTimeSeconds = s.coerceAtLeast(0) }
    fun updateWorkoutMode(mode: WorkoutMode) { workoutMode = mode }

    fun addExercise(exercise: Exercise) {
        if (!selectedExercises.contains(exercise)) {
            selectedExercises = selectedExercises + exercise
        }
    }

    fun removeExercise(exercise: Exercise) {
        selectedExercises = selectedExercises - exercise
    }

    fun reorderExercises(from: Int, to: Int) {
        val mutableList = selectedExercises.toMutableList()
        val item = mutableList.removeAt(from)
        mutableList.add(to, item)
        selectedExercises = mutableList
    }

    // Custom exercise management for CUSTOM mode
    fun addCustomExerciseFromLibrary(exercise: Exercise, durationSeconds: Int) {
        val customExercise = CustomExercise(
            workoutPlanId = 0, // Will be set when saving
            name = exercise.name,
            durationSeconds = durationSeconds,
            orderIndex = customExercises.size,
            imagePath = exercise.imagePath,
            isFromLibrary = true,
            libraryExerciseId = exercise.id
        )
        customExercises = customExercises + customExercise
    }

    fun addCustomExercise(name: String, durationSeconds: Int, imagePath: String? = null) {
        val customExercise = CustomExercise(
            workoutPlanId = 0, // Will be set when saving
            name = name,
            durationSeconds = durationSeconds,
            orderIndex = customExercises.size,
            imagePath = imagePath,
            isFromLibrary = false,
            libraryExerciseId = null
        )
        customExercises = customExercises + customExercise
    }

    fun removeCustomExercise(customExercise: CustomExercise) {
        customExercises = customExercises - customExercise
    }

    fun updateCustomExerciseDuration(index: Int, newDuration: Int) {
        if (index in customExercises.indices) {
            val updated = customExercises[index].copy(durationSeconds = newDuration)
            customExercises = customExercises.toMutableList().apply {
                set(index, updated)
            }
        }
    }

    fun reorderCustomExercises(from: Int, to: Int) {
        val mutableList = customExercises.toMutableList()
        val item = mutableList.removeAt(from)
        mutableList.add(to, item)
        customExercises = mutableList
    }

    fun save() {
        // Validation depends on workout mode
        val isValid = when (workoutMode) {
            WorkoutMode.SIMPLE -> planName.isNotBlank()
            WorkoutMode.LIBRARY -> planName.isNotBlank() && selectedExercises.isNotEmpty()
            WorkoutMode.CUSTOM -> planName.isNotBlank() && customExercises.isNotEmpty()
        }

        if (!isValid) return

        viewModelScope.launch {
            val plan = WorkoutPlan(
                id = if (isEditing) editingPlanId else 0,
                name = planName.trim(),
                rounds = rounds,
                exerciseCount = when (workoutMode) {
                    WorkoutMode.SIMPLE -> exerciseCount      // From input field
                    WorkoutMode.LIBRARY -> selectedExercises.size  // From library selection
                    WorkoutMode.CUSTOM -> customExercises.size     // From custom exercises
                },
                workSeconds = workSeconds,
                restSeconds = restSeconds,
                prepTimeSeconds = prepTimeSeconds,
                workoutMode = workoutMode,
                hasExercises = selectedExercises.isNotEmpty() || customExercises.isNotEmpty()
            )

            val planId = if (isEditing) {
                planDao.update(plan)
                editingPlanId
            } else {
                planDao.insert(plan)
            }

            // Save exercise associations based on workout mode
            when (workoutMode) {
                WorkoutMode.LIBRARY -> {
                    if (selectedExercises.isNotEmpty()) {
                        workoutExerciseDao.replaceExercisesForWorkout(planId, selectedExercises)
                    }
                }
                WorkoutMode.CUSTOM -> {
                    if (customExercises.isNotEmpty()) {
                        customExerciseDao.replaceCustomExercisesForWorkout(planId, customExercises)
                    }
                }
                WorkoutMode.SIMPLE -> {
                    // No exercises to save for SIMPLE mode
                }
            }

            isSaved = true
        }
    }
}
