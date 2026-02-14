package com.stopwatch.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.stopwatch.app.data.model.ExerciseCategoryBreakdown
import com.stopwatch.app.data.model.ExerciseStats
import com.stopwatch.app.data.model.MonthlyStats
import com.stopwatch.app.data.model.WorkoutBreakdown
import com.stopwatch.app.data.model.WorkoutHistory
import com.stopwatch.app.data.model.YearlyStats
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutHistoryDao {

    @Query("SELECT * FROM workout_history ORDER BY completedAt DESC")
    fun getAllHistory(): Flow<List<WorkoutHistory>>

    @Query(
        """
        SELECT strftime('%Y-%m', completedAt / 1000, 'unixepoch', 'localtime') AS yearMonth,
               COUNT(*) AS count,
               SUM(totalDurationSeconds) AS totalSeconds,
               SUM(roundsCompleted) AS totalRounds
        FROM workout_history
        GROUP BY yearMonth
        ORDER BY yearMonth DESC
        """
    )
    fun getMonthlyStats(): Flow<List<MonthlyStats>>

    @Query(
        """
        SELECT planName,
               COUNT(*) AS count,
               SUM(totalDurationSeconds) AS totalSeconds
        FROM workout_history
        WHERE strftime('%Y-%m', completedAt / 1000, 'unixepoch', 'localtime') = :yearMonth
        GROUP BY planName
        ORDER BY count DESC
        """
    )
    fun getMonthlyBreakdown(yearMonth: String): Flow<List<WorkoutBreakdown>>

    @Query(
        """
        SELECT e.category,
               COUNT(DISTINCT we.exerciseId) AS exerciseCount,
               COUNT(DISTINCT wh.id) AS sessionCount
        FROM workout_history wh
        INNER JOIN workout_exercises we ON wh.planId = we.workoutPlanId
        INNER JOIN exercises e ON we.exerciseId = e.id
        WHERE strftime('%Y-%m', wh.completedAt / 1000, 'unixepoch', 'localtime') = :yearMonth
        GROUP BY e.category
        ORDER BY exerciseCount DESC
        """
    )
    fun getMonthlyExerciseCategoryBreakdown(yearMonth: String): Flow<List<ExerciseCategoryBreakdown>>

    @Query(
        """
        SELECT e.name AS exerciseName,
               e.category,
               COUNT(DISTINCT wh.id) AS sessionCount,
               e.imagePath
        FROM workout_history wh
        INNER JOIN workout_exercises we ON wh.planId = we.workoutPlanId
        INNER JOIN exercises e ON we.exerciseId = e.id
        WHERE strftime('%Y-%m', wh.completedAt / 1000, 'unixepoch', 'localtime') = :yearMonth
        GROUP BY e.id
        ORDER BY sessionCount DESC
        LIMIT 10
        """
    )
    fun getTopExercises(yearMonth: String): Flow<List<ExerciseStats>>

    @Query(
        """
        SELECT strftime('%Y', completedAt / 1000, 'unixepoch', 'localtime') AS year,
               COUNT(*) AS count,
               SUM(totalDurationSeconds) AS totalSeconds,
               SUM(roundsCompleted) AS totalRounds,
               COUNT(DISTINCT strftime('%Y-%m-%d', completedAt / 1000, 'unixepoch', 'localtime')) AS activeDays
        FROM workout_history
        GROUP BY year
        ORDER BY year DESC
        """
    )
    fun getYearlyStats(): Flow<List<YearlyStats>>

    @Query(
        """
        SELECT planName,
               COUNT(*) AS count,
               SUM(totalDurationSeconds) AS totalSeconds
        FROM workout_history
        WHERE strftime('%Y', completedAt / 1000, 'unixepoch', 'localtime') = :year
        GROUP BY planName
        ORDER BY count DESC
        LIMIT 1
        """
    )
    fun getMostUsedWorkout(year: String): Flow<WorkoutBreakdown?>

    @Insert
    suspend fun insert(history: WorkoutHistory): Long
}
