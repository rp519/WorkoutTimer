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
    val restSeconds: Int
)
