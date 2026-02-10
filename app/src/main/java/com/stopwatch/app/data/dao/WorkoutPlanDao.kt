package com.stopwatch.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.stopwatch.app.data.model.WorkoutPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutPlanDao {

    @Query("SELECT * FROM workout_plans ORDER BY id DESC")
    fun getAllPlans(): Flow<List<WorkoutPlan>>

    @Query("SELECT * FROM workout_plans WHERE id = :planId")
    suspend fun getById(planId: Long): WorkoutPlan?

    @Insert
    suspend fun insert(plan: WorkoutPlan): Long

    @Update
    suspend fun update(plan: WorkoutPlan)

    @Delete
    suspend fun delete(plan: WorkoutPlan)

    @Query("DELETE FROM workout_plans WHERE id = :planId")
    suspend fun deleteById(planId: Long)
}
