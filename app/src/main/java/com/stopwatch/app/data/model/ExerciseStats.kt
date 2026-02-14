package com.stopwatch.app.data.model

data class ExerciseStats(
    val exerciseName: String,
    val category: String,
    val sessionCount: Int,  // Number of workout sessions that included this exercise
    val imagePath: String?
)
