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
     * Handles nested subcategory structure: exercises/CATEGORY/Subcategory/exercise.webp
     */
    private fun scanExercisesFromAssets(): List<Exercise> {
        val exercises = mutableListOf<Exercise>()
        val assetManager = context.assets

        try {
            // List all category folders in assets/exercises/
            val categories = assetManager.list("exercises") ?: emptyArray()

            for (categoryFolder in categories) {
                // Skip non-directories
                if (categoryFolder.contains(".")) continue

                // List items in category folder (could be subcategories or direct files)
                val categoryItems = assetManager.list("exercises/$categoryFolder") ?: emptyArray()

                for (item in categoryItems) {
                    if (item.endsWith(".webp", ignoreCase = true)) {
                        // Direct exercise file in category folder (no subcategory)
                        val exerciseName = formatExerciseName(item)
                        val imagePath = "exercises/$categoryFolder/$item"

                        exercises.add(
                            Exercise(
                                name = exerciseName,
                                category = categoryFolder,
                                subcategory = "",
                                imagePath = imagePath
                            )
                        )
                    } else {
                        // Check if it's a subcategory folder
                        try {
                            val subcategoryFiles = assetManager.list("exercises/$categoryFolder/$item") ?: emptyArray()

                            for (fileName in subcategoryFiles) {
                                if (fileName.endsWith(".webp", ignoreCase = true)) {
                                    val exerciseName = formatExerciseName(fileName)
                                    val imagePath = "exercises/$categoryFolder/$item/$fileName"

                                    exercises.add(
                                        Exercise(
                                            name = exerciseName,
                                            category = categoryFolder,
                                            subcategory = item,
                                            imagePath = imagePath
                                        )
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            // Not a directory, skip
                        }
                    }
                }
            }

            Log.d(TAG, "Scanned exercises: ${exercises.size} found")
            Log.d(TAG, "Categories: ${exercises.map { it.category }.distinct().sorted()}")
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning assets folder", e)
        }

        return exercises.sortedWith(compareBy({ it.category }, { it.subcategory }, { it.name }))
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
