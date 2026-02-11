package com.stopwatch.app.notification

import android.content.Context
import com.stopwatch.app.data.AppDatabase
import com.stopwatch.app.data.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

class AchievementTracker(private val context: Context) {

    private val historyDao = AppDatabase.getInstance(context).workoutHistoryDao()
    private val notificationManager = WorkoutNotificationManager(context)
    private val preferencesRepository = UserPreferencesRepository(context)

    suspend fun checkAndNotifyAchievements() {
        // Only show achievements if enabled
        val achievementsEnabled = preferencesRepository.achievementNotificationsEnabled.first()
        if (!achievementsEnabled) return

        val allHistory = historyDao.getAllHistory().first()
        val totalWorkouts = allHistory.size

        // Check milestone achievements
        when (totalWorkouts) {
            10 -> notificationManager.showAchievementNotification(
                "🎯 10 Workouts Complete!",
                "You've completed 10 workouts! Keep up the momentum!"
            )
            25 -> notificationManager.showAchievementNotification(
                "🔥 25 Workouts Complete!",
                "A quarter century of workouts! You're on fire!"
            )
            50 -> notificationManager.showAchievementNotification(
                "💪 50 Workouts Complete!",
                "Half a hundred! You're a fitness champion!"
            )
            100 -> notificationManager.showAchievementNotification(
                "🏆 100 Workouts Complete!",
                "Century club! You're a true workout warrior!"
            )
        }

        // Check streak achievements
        val currentStreak = calculateCurrentStreak(allHistory)
        when (currentStreak) {
            7 -> notificationManager.showAchievementNotification(
                "🔥 7-Day Streak!",
                "One week of consistent workouts! Don't break the chain!"
            )
            14 -> notificationManager.showAchievementNotification(
                "⚡ 2-Week Streak!",
                "Two weeks strong! Your dedication is inspiring!"
            )
            30 -> notificationManager.showAchievementNotification(
                "🌟 30-Day Streak!",
                "A full month! You've built an incredible habit!"
            )
        }
    }

    private fun calculateCurrentStreak(history: List<com.stopwatch.app.data.model.WorkoutHistory>): Int {
        if (history.isEmpty()) return 0

        // Group workouts by date
        val workoutDates = history.map { workout ->
            LocalDate.ofInstant(
                java.time.Instant.ofEpochMilli(workout.completedAt),
                ZoneId.systemDefault()
            )
        }.distinct().sortedDescending()

        if (workoutDates.isEmpty()) return 0

        var streak = 0
        var expectedDate = LocalDate.now()

        for (date in workoutDates) {
            // Check if this date is expected (today or consecutive day)
            if (date == expectedDate || date == expectedDate.minusDays(1)) {
                streak++
                expectedDate = date.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }
}
