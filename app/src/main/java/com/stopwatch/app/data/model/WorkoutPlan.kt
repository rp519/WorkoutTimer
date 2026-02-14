package com.stopwatch.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_plans")
data class WorkoutPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val rounds: Int,
    val exerciseCount: Int,
    val workSeconds: Int,
    val restSeconds: Int,

    // New fields for workout modes feature
    val workoutMode: WorkoutMode = WorkoutMode.SIMPLE,  // Workout type: SIMPLE, LIBRARY, or CUSTOM
    val prepTimeSeconds: Int = 30,                      // Preparation time before workout starts
    val hasExercises: Boolean = false                   // Quick flag indicating if workout has selected exercises
)
