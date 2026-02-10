package com.stopwatch.app.screen.planlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stopwatch.app.data.AppDatabase
import com.stopwatch.app.data.model.WorkoutPlan
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlanListViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).workoutPlanDao()

    val plans = dao.getAllPlans().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun deletePlan(plan: WorkoutPlan) {
        viewModelScope.launch {
            dao.delete(plan)
        }
    }
}
