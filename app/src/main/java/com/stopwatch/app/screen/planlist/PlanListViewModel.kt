package com.stopwatch.app.screen.planlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stopwatch.app.data.AppDatabase
import com.stopwatch.app.data.UserPreferencesRepository
import com.stopwatch.app.data.model.WorkoutPlan
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlanListViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).workoutPlanDao()
    private val preferencesRepository = UserPreferencesRepository(application)

    val plans = dao.getAllPlans().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val showQuickTimer = preferencesRepository.showQuickTimer.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    fun deletePlan(plan: WorkoutPlan) {
        viewModelScope.launch {
            dao.delete(plan)
        }
    }
}
