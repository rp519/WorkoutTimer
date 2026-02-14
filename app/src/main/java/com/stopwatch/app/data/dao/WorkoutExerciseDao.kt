package com.stopwatch.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.stopwatch.app.data.model.WorkoutExercise
import com.stopwatch.app.data.model.Exercise

@Dao
interface WorkoutExerciseDao {

    @Query("""
        SELECT e.* FROM exercises e
        INNER JOIN workout_exercises we ON e.id = we.exerciseId
        WHERE we.workoutPlanId = :workoutPlanId
        ORDER BY we.orderIndex
    """)
    suspend fun getExercisesForWorkout(workoutPlanId: Long): List<Exercise>

    @Transaction
    suspend fun replaceExercisesForWorkout(workoutPlanId: Long, exercises: List<Exercise>) {
        deleteExercisesForWorkout(workoutPlanId)
        val workoutExercises = exercises.mapIndexed { index, exercise ->
            WorkoutExercise(
                workoutPlanId = workoutPlanId,
                exerciseId = exercise.id,
                orderIndex = index
            )
        }
        insertWorkoutExercises(workoutExercises)
    }

    @Query("DELETE FROM workout_exercises WHERE workoutPlanId = :workoutPlanId")
    suspend fun deleteExercisesForWorkout(workoutPlanId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercises(workoutExercises: List<WorkoutExercise>)
}
