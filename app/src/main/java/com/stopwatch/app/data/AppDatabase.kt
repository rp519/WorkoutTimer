package com.stopwatch.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.stopwatch.app.data.dao.WorkoutHistoryDao
import com.stopwatch.app.data.dao.WorkoutPlanDao
import com.stopwatch.app.data.model.WorkoutHistory
import com.stopwatch.app.data.model.WorkoutPlan

@Database(
    entities = [WorkoutPlan::class, WorkoutHistory::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun workoutHistoryDao(): WorkoutHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "workout_timer.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
