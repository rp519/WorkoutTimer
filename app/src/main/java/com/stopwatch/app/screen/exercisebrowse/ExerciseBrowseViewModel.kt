package com.stopwatch.app.screen.exercisebrowse

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stopwatch.app.data.AppDatabase
import com.stopwatch.app.data.model.Exercise
import com.stopwatch.app.data.model.ExerciseCategories
import com.stopwatch.app.data.model.ExerciseCategory
import com.stopwatch.app.data.model.ExerciseSubcategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for browsing exercises by category, subcategory, and search
 */
class ExerciseBrowseViewModel(application: Application) : AndroidViewModel(application) {

    private val exerciseDao = AppDatabase.getInstance(application).exerciseDao()

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // All exercises
    val allExercises = exerciseDao.getAllExercises().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Search results
    val searchResults = combine(
        _searchQuery,
        allExercises
    ) { query, exercises ->
        if (query.isBlank()) {
            emptyList()
        } else {
            exercises.filter { exercise ->
                exercise.name.contains(query, ignoreCase = true) ||
                exercise.category.contains(query, ignoreCase = true) ||
                exercise.subcategory.contains(query, ignoreCase = true) ||
                exercise.targetMuscles.contains(query, ignoreCase = true) ||
                exercise.equipment.contains(query, ignoreCase = true) ||
                exercise.description.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    /**
     * Get all categories with exercise counts
     */
    suspend fun getCategories(): List<ExerciseCategory> {
        return ExerciseCategories.getAllCategories().map { category ->
            ExerciseCategory(
                name = category,
                displayName = ExerciseCategories.getDisplayName(category),
                exerciseCount = exerciseDao.getExerciseCountByCategory(category),
                subcategories = ExerciseCategories.getSubcategories(category)
            )
        }
    }

    /**
     * Get subcategories for a specific category with exercise counts
     */
    suspend fun getSubcategories(category: String): List<ExerciseSubcategory> {
        val subcategoryNames = ExerciseCategories.getSubcategories(category)
        return subcategoryNames.map { subcategory ->
            ExerciseSubcategory(
                name = subcategory,
                parentCategory = category,
                exerciseCount = exerciseDao.getExerciseCountBySubcategory(category, subcategory)
            )
        }
    }

    /**
     * Get exercises for a specific subcategory
     */
    fun getExercisesBySubcategory(category: String, subcategory: String): StateFlow<List<Exercise>> {
        return exerciseDao.getExercisesBySubcategory(category, subcategory).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    /**
     * Get single exercise by ID
     */
    suspend fun getExerciseById(id: Long): Exercise? {
        return exerciseDao.getExerciseById(id)
    }

    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Clear search query
     */
    fun clearSearch() {
        _searchQuery.value = ""
    }
}
