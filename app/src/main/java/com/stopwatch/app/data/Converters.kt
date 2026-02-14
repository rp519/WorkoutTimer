package com.stopwatch.app.data

import androidx.room.TypeConverter
import com.stopwatch.app.data.model.WorkoutMode

/**
 * Room type converters for custom data types
 */
class Converters {

    /**
     * Convert WorkoutMode enum to String for database storage
     */
    @TypeConverter
    fun fromWorkoutMode(mode: WorkoutMode): String {
        return mode.name
    }

    /**
     * Convert String back to WorkoutMode enum
     */
    @TypeConverter
    fun toWorkoutMode(value: String): WorkoutMode {
        return try {
            WorkoutMode.valueOf(value)
        } catch (e: IllegalArgumentException) {
            // Default to SIMPLE if unknown mode
            WorkoutMode.SIMPLE
        }
    }
}
