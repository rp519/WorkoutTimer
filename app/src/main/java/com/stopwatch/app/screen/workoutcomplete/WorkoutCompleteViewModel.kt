package com.stopwatch.app.screen.workoutcomplete

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stopwatch.app.data.AppDatabase
import kotlinx.coroutines.launch

class WorkoutCompleteViewModel(application: Application) : AndroidViewModel(application) {

    private val planDao = AppDatabase.getInstance(application).workoutPlanDao()

    var planName by mutableStateOf("")
        private set

    fun load(planId: Long) {
        viewModelScope.launch {
            val plan = planDao.getById(planId)
            planName = plan?.name ?: "Workout"
        }
    }
}
