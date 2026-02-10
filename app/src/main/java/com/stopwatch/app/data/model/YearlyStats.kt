package com.stopwatch.app.data.model

data class YearlyStats(
    val year: String,
    val count: Int,
    val totalSeconds: Int,
    val activeDays: Int
)
