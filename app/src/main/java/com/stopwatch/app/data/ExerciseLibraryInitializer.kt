package com.stopwatch.app.data

import android.content.Context
import android.util.Log
import com.stopwatch.app.data.model.Exercise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExerciseLibraryInitializer(private val context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val exerciseDao = database.exerciseDao()

    /**
     * Initialize the exercise library from assets if not already populated
     */
    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        try {
            // Check if exercises already exist
            val existingCount = exerciseDao.getExerciseCount()
            if (existingCount > 0) {
                Log.d(TAG, "Exercise library already initialized with $existingCount exercises")
                return@withContext
            }

            // Scan assets folder and populate exercises
            val exercises = scanExercisesFromAssets()
            if (exercises.isNotEmpty()) {
                exerciseDao.insertAll(exercises)
                Log.d(TAG, "Initialized exercise library with ${exercises.size} exercises")
            } else {
                Log.w(TAG, "No exercises found in assets/exercises folder")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize exercise library", e)
        }
    }

    /**
     * Scan the assets/exercises folder and build Exercise objects
     */
    private fun scanExercisesFromAssets(): List<Exercise> {
        val exercises = mutableListOf<Exercise>()
        val assetManager = context.assets

        try {
            // List all category folders in assets/exercises/
            val categories = assetManager.list("exercises")?.filter { folder ->
                // Filter out non-folder items (like README.txt)
                try {
                    val files = assetManager.list("exercises/$folder")
                    !files.isNullOrEmpty()
                } catch (e: Exception) {
                    false
                }
            } ?: emptyList()

            // For each category, scan for .webp files
            for (categoryFolder in categories) {
                val category = formatCategoryName(categoryFolder)
                val files = assetManager.list("exercises/$categoryFolder") ?: emptyArray()

                for (fileName in files) {
                    if (fileName.endsWith(".webp", ignoreCase = true)) {
                        val exerciseName = formatExerciseName(fileName)
                        val imagePath = "exercises/$categoryFolder/$fileName"

                        exercises.add(
                            Exercise(
                                name = exerciseName,
                                category = category,
                                imagePath = imagePath
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning assets folder", e)
        }

        return exercises.sortedWith(compareBy({ it.category }, { it.name }))
    }

    /**
     * Convert folder name to formatted category name
     * Example: "abs" → "Abs", "upper_body" → "Upper Body"
     */
    private fun formatCategoryName(folderName: String): String {
        return folderName
            .replace('_', ' ')
            .split(' ')
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
    }

    /**
     * Convert filename to formatted exercise name
     * Example: "bicycle_crunch.webp" → "Bicycle Crunch"
     */
    private fun formatExerciseName(fileName: String): String {
        return fileName
            .removeSuffix(".webp")
            .replace('_', ' ')
            .split(' ')
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
    }

    companion object {
        private const val TAG = "ExerciseLibraryInit"
    }
}
