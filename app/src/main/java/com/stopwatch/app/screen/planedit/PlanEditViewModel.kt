package com.stopwatch.app.screen.planedit

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stopwatch.app.data.AppDatabase
import com.stopwatch.app.data.model.WorkoutPlan
import kotlinx.coroutines.launch

class PlanEditViewModel(application: Application) : AndroidViewModel(application) {

    private val planDao = AppDatabase.getInstance(application).workoutPlanDao()

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

    var isEditing by mutableStateOf(false)
        private set
    private var editingPlanId: Long = -1L

    var isSaved by mutableStateOf(false)
        private set

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
        }
    }

    fun updatePlanName(name: String) { planName = name }
    fun updateRounds(r: Int) { rounds = r.coerceAtLeast(1) }
    fun updateExerciseCount(c: Int) { exerciseCount = c.coerceAtLeast(1) }
    fun updateWorkSeconds(s: Int) { workSeconds = s.coerceAtLeast(1) }
    fun updateRestSeconds(s: Int) { restSeconds = s.coerceAtLeast(0) }

    fun save() {
        if (planName.isBlank()) return

        viewModelScope.launch {
            val plan = WorkoutPlan(
                id = if (isEditing) editingPlanId else 0,
                name = planName.trim(),
                rounds = rounds,
                exerciseCount = exerciseCount,
                workSeconds = workSeconds,
                restSeconds = restSeconds
            )
            if (isEditing) planDao.update(plan) else planDao.insert(plan)
            isSaved = true
        }
    }
}
