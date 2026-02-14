package com.stopwatch.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Intensity levels for exercises
enum class Intensity {
    LOW, MEDIUM, HIGH, VERY_HIGH
}

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val subcategory: String = "",
    val imagePath: String = "",  // e.g., "exercises/abs/bicycle_crunch.webp"
    val durationMin: Int = 0,  // Minimum duration in seconds
    val durationMax: Int = 0,  // Maximum duration in seconds
    val intensity: String = Intensity.MEDIUM.name,  // LOW, MEDIUM, HIGH, VERY_HIGH
    val targetMuscles: String = "",  // Comma-separated: "Quads, Glutes, Hamstrings"
    val equipment: String = "",  // "Barbell, Dumbbells, Bodyweight"
    val caloriesPerMin: Double = 0.0,  // Estimated calories burned per minute
    val description: String = "",  // Full description of the exercise
    val tier: String = ""  // Optional tier/level classification
)
