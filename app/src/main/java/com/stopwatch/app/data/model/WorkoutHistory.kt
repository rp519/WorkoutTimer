package com.stopwatch.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_history",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutPlan::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("planId")]
)
data class WorkoutHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long?,
    val planName: String,
    val completedAt: Long,
    val totalDurationSeconds: Int,
    val roundsCompleted: Int
)
