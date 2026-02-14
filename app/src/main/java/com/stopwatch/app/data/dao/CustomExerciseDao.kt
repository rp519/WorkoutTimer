package com.stopwatch.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.stopwatch.app.data.model.CustomExercise

/**
 * DAO for managing custom exercises in CUSTOM mode workouts.
 * Handles retrieval, insertion, and deletion of custom exercise entries.
 */
@Dao
interface CustomExerciseDao {

    /**
     * Get all custom exercises for a specific workout, ordered by their position
     */
    @Query("""
        SELECT * FROM custom_exercises
        WHERE workoutPlanId = :workoutPlanId
        ORDER BY orderIndex
    """)
    suspend fun getCustomExercisesForWorkout(workoutPlanId: Long): List<CustomExercise>

    /**
     * Insert multiple custom exercises
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<CustomExercise>)

    /**
     * Insert a single custom exercise
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: CustomExercise): Long

    /**
     * Replace all custom exercises for a workout.
     * Deletes existing custom exercises and inserts new ones with proper ordering.
     */
    @Transaction
    suspend fun replaceCustomExercisesForWorkout(
        workoutPlanId: Long,
        exercises: List<CustomExercise>
    ) {
        deleteCustomExercisesForWorkout(workoutPlanId)
        if (exercises.isNotEmpty()) {
            val indexed = exercises.mapIndexed { index, exercise ->
                exercise.copy(
                    orderIndex = index,
                    workoutPlanId = workoutPlanId
                )
            }
            insertAll(indexed)
        }
    }

    /**
     * Delete all custom exercises for a specific workout
     */
    @Query("DELETE FROM custom_exercises WHERE workoutPlanId = :workoutPlanId")
    suspend fun deleteCustomExercisesForWorkout(workoutPlanId: Long)

    /**
     * Get the count of custom exercises for a workout
     */
    @Query("SELECT COUNT(*) FROM custom_exercises WHERE workoutPlanId = :workoutPlanId")
    suspend fun getCustomExerciseCount(workoutPlanId: Long): Int
}
