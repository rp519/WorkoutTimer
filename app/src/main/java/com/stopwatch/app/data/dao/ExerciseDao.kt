package com.stopwatch.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stopwatch.app.data.model.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercises ORDER BY category, subcategory, name")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE category = :category ORDER BY subcategory, name")
    fun getExercisesByCategory(category: String): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE category = :category AND subcategory = :subcategory ORDER BY name")
    fun getExercisesBySubcategory(category: String, subcategory: String): Flow<List<Exercise>>

    @Query("SELECT DISTINCT category FROM exercises ORDER BY category")
    suspend fun getAllCategories(): List<String>

    @Query("SELECT DISTINCT subcategory FROM exercises WHERE category = :category ORDER BY subcategory")
    suspend fun getSubcategoriesForCategory(category: String): List<String>

    @Query("SELECT COUNT(*) FROM exercises WHERE category = :category")
    suspend fun getExerciseCountByCategory(category: String): Int

    @Query("SELECT COUNT(*) FROM exercises WHERE category = :category AND subcategory = :subcategory")
    suspend fun getExerciseCountBySubcategory(category: String, subcategory: String): Int

    /**
     * Search exercises by multiple criteria
     * Searches: name, category, subcategory, target muscles, equipment, description
     */
    @Query("""
        SELECT * FROM exercises
        WHERE name LIKE '%' || :query || '%'
        OR category LIKE '%' || :query || '%'
        OR subcategory LIKE '%' || :query || '%'
        OR targetMuscles LIKE '%' || :query || '%'
        OR equipment LIKE '%' || :query || '%'
        OR description LIKE '%' || :query || '%'
        ORDER BY
            CASE
                WHEN name LIKE :query || '%' THEN 1
                WHEN name LIKE '%' || :query || '%' THEN 2
                ELSE 3
            END,
            name
    """)
    fun searchExercises(query: String): Flow<List<Exercise>>

    /**
     * Filter exercises by intensity
     */
    @Query("SELECT * FROM exercises WHERE intensity = :intensity ORDER BY category, subcategory, name")
    fun getExercisesByIntensity(intensity: String): Flow<List<Exercise>>

    /**
     * Filter exercises by equipment
     */
    @Query("SELECT * FROM exercises WHERE equipment LIKE '%' || :equipment || '%' ORDER BY category, subcategory, name")
    fun getExercisesByEquipment(equipment: String): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<Exercise>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: Exercise): Long

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: Long): Exercise?
}
