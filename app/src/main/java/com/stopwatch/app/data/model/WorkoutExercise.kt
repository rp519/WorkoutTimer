package com.stopwatch.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "workout_exercises",
    primaryKeys = ["workoutPlanId", "orderIndex"],
    foreignKeys = [
        ForeignKey(
            entity = WorkoutPlan::class,
            parentColumns = ["id"],
            childColumns = ["workoutPlanId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workoutPlanId"]),
        Index(value = ["exerciseId"])
    ]
)
data class WorkoutExercise(
    val workoutPlanId: Long,
    val exerciseId: Long,
    val orderIndex: Int
)
