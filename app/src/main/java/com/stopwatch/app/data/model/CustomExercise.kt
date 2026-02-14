package com.stopwatch.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a custom exercise in a CUSTOM mode workout.
 * Can be either:
 * 1. A library exercise with custom duration (isFromLibrary = true)
 * 2. A fully custom user-defined exercise (isFromLibrary = false)
 *
 * This allows mixing library exercises with custom timed intervals in a single workout.
 */
@Entity(
    tableName = "custom_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutPlan::class,
            parentColumns = ["id"],
            childColumns = ["workoutPlanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workoutPlanId"])
    ]
)
data class CustomExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    val workoutPlanId: Long,              // The workout this exercise belongs to

    val name: String,                     // Exercise name (from library or user-defined)

    val durationSeconds: Int,             // Custom duration for this specific exercise

    val orderIndex: Int,                  // Position in the workout (0-based)

    val imagePath: String? = null,        // Image path (from library or custom uploaded)

    val isFromLibrary: Boolean = false,   // True if added from exercise library, false if custom

    val libraryExerciseId: Long? = null   // Reference to library exercise if isFromLibrary = true
)
