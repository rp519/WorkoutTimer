package com.stopwatch.app.data.model

/**
 * Represents a main exercise category
 * Used for navigation and filtering
 */
data class ExerciseCategory(
    val name: String,
    val displayName: String,
    val exerciseCount: Int = 0,
    val subcategories: List<String> = emptyList()
)

/**
 * Represents a subcategory within a main category
 */
data class ExerciseSubcategory(
    val name: String,
    val parentCategory: String,
    val exerciseCount: Int = 0
)

/**
 * Main categories for exercises
 */
object ExerciseCategories {
    const val WEIGHT_TRAINING = "WEIGHT_TRAINING"
    const val CARDIO = "CARDIO"
    const val HIIT = "HIIT"
    const val RESISTANCE_TRAINING = "RESISTANCE_TRAINING"
    const val YOGA_FLEXIBILITY = "YOGA_FLEXIBILITY"

    /**
     * Get display name for category
     */
    fun getDisplayName(category: String): String = when (category) {
        WEIGHT_TRAINING -> "Weight Training"
        CARDIO -> "Cardio"
        HIIT -> "HIIT"
        RESISTANCE_TRAINING -> "Resistance Training"
        YOGA_FLEXIBILITY -> "Yoga & Flexibility"
        else -> category
    }

    /**
     * Get all categories
     */
    fun getAllCategories(): List<String> = listOf(
        WEIGHT_TRAINING,
        CARDIO,
        HIIT,
        RESISTANCE_TRAINING,
        YOGA_FLEXIBILITY
    )

    /**
     * Get subcategories for a given category
     */
    fun getSubcategories(category: String): List<String> = when (category) {
        WEIGHT_TRAINING -> listOf("Legs", "Chest", "Back", "Shoulders", "Arms", "Core")
        CARDIO -> listOf("Steady State", "High Impact", "Low Impact", "Full Body")
        HIIT -> listOf("Bodyweight", "With Equipment", "Interval Training")
        RESISTANCE_TRAINING -> listOf("Bands", "Bodyweight", "Other")
        YOGA_FLEXIBILITY -> listOf("Yoga", "Flexibility", "Pilates")
        else -> emptyList()
    }
}
