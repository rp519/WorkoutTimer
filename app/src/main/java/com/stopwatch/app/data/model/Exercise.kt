package com.stopwatch.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val imagePath: String  // e.g., "exercises/abs/bicycle_crunch.webp"
)
