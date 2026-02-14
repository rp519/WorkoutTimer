package com.stopwatch.app.screen.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stopwatch.app.data.AppDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val historyDao = AppDatabase.getInstance(application).workoutHistoryDao()

    val history = historyDao.getAllHistory().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val monthlyStats = historyDao.getMonthlyStats().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val currentYearMonth: String = run {
        val now = LocalDate.now()
        "%04d-%02d".format(now.year, now.monthValue)
    }

    private val currentYear: String = LocalDate.now().year.toString()

    val currentMonthBreakdown = historyDao.getMonthlyBreakdown(currentYearMonth).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val yearlyStats = historyDao.getYearlyStats().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val mostUsedWorkout = historyDao.getMostUsedWorkout(currentYear).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    val exerciseCategoryBreakdown = historyDao.getMonthlyExerciseCategoryBreakdown(currentYearMonth).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val topExercises = historyDao.getTopExercises(currentYearMonth).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
}
