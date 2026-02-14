package com.stopwatch.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.stopwatch.app.data.dao.CustomExerciseDao
import com.stopwatch.app.data.dao.ExerciseDao
import com.stopwatch.app.data.dao.WorkoutExerciseDao
import com.stopwatch.app.data.dao.WorkoutHistoryDao
import com.stopwatch.app.data.dao.WorkoutPlanDao
import com.stopwatch.app.data.model.CustomExercise
import com.stopwatch.app.data.model.Exercise
import com.stopwatch.app.data.model.WorkoutExercise
import com.stopwatch.app.data.model.WorkoutHistory
import com.stopwatch.app.data.model.WorkoutPlan

@Database(
    entities = [
        WorkoutPlan::class,
        WorkoutHistory::class,
        Exercise::class,
        WorkoutExercise::class,
        CustomExercise::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun workoutHistoryDao(): WorkoutHistoryDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
    abstract fun customExerciseDao(): CustomExerciseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create exercises table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS exercises (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        category TEXT NOT NULL,
                        imagePath TEXT NOT NULL
                    )
                """)

                // Create workout_exercises junction table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS workout_exercises (
                        workoutPlanId INTEGER NOT NULL,
                        exerciseId INTEGER NOT NULL,
                        orderIndex INTEGER NOT NULL,
                        PRIMARY KEY(workoutPlanId, orderIndex),
                        FOREIGN KEY(workoutPlanId) REFERENCES workout_plans(id) ON DELETE CASCADE,
                        FOREIGN KEY(exerciseId) REFERENCES exercises(id) ON DELETE CASCADE
                    )
                """)

                // Create indices for foreign keys
                db.execSQL("CREATE INDEX IF NOT EXISTS index_workout_exercises_workoutPlanId ON workout_exercises(workoutPlanId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_workout_exercises_exerciseId ON workout_exercises(exerciseId)")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to workout_plans table for workout modes feature
                db.execSQL("ALTER TABLE workout_plans ADD COLUMN workoutMode TEXT NOT NULL DEFAULT 'SIMPLE'")
                db.execSQL("ALTER TABLE workout_plans ADD COLUMN prepTimeSeconds INTEGER NOT NULL DEFAULT 30")
                db.execSQL("ALTER TABLE workout_plans ADD COLUMN hasExercises INTEGER NOT NULL DEFAULT 0")

                // Migrate existing workouts: if they have associated exercises, set to LIBRARY mode
                db.execSQL("""
                    UPDATE workout_plans
                    SET workoutMode = 'LIBRARY', hasExercises = 1
                    WHERE id IN (SELECT DISTINCT workoutPlanId FROM workout_exercises)
                """)

                // Create custom_exercises table for CUSTOM mode workouts
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS custom_exercises (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        workoutPlanId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        durationSeconds INTEGER NOT NULL,
                        orderIndex INTEGER NOT NULL,
                        imagePath TEXT,
                        isFromLibrary INTEGER NOT NULL DEFAULT 0,
                        libraryExerciseId INTEGER,
                        FOREIGN KEY(workoutPlanId) REFERENCES workout_plans(id) ON DELETE CASCADE
                    )
                """)

                // Create index for custom_exercises foreign key
                db.execSQL("CREATE INDEX IF NOT EXISTS index_custom_exercises_workoutPlanId ON custom_exercises(workoutPlanId)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "workout_timer.db"
                )
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
