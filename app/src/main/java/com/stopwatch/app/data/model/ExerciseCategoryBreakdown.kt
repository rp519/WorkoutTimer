package com.stopwatch.app.data.model

data class ExerciseCategoryBreakdown(
    val category: String,
    val exerciseCount: Int,
    val sessionCount: Int  // Number of workout sessions that included this category
)
