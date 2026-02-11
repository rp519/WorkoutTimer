package com.stopwatch.app.email

import android.content.Context
import android.util.Log
import com.stopwatch.app.data.AppDatabase
import com.stopwatch.app.data.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object EmailDebugHelper {
    private const val TAG = "EmailDebug"

    suspend fun debugEmailData(context: Context): String {
        val log = StringBuilder()

        try {
            log.append("=== EMAIL DEBUG INFO ===\n\n")

            // Check email configuration
            val preferencesRepository = UserPreferencesRepository(context)
            val userEmail = preferencesRepository.userEmail.first()
            log.append("1. Email Configuration:\n")
            log.append("   Email: ${userEmail ?: "NOT SET"}\n")
            log.append("   Status: ${if (userEmail.isNullOrBlank()) "❌ NO EMAIL" else "✅ EMAIL SET"}\n\n")

            // Check workout data
            val historyDao = AppDatabase.getInstance(context).workoutHistoryDao()
            val allHistory = historyDao.getAllHistory().first()
            log.append("2. Workout Data:\n")
            log.append("   Total workouts: ${allHistory.size}\n")

            if (allHistory.isEmpty()) {
                log.append("   Status: ❌ NO WORKOUTS - Complete a workout first!\n\n")
            } else {
                log.append("   Status: ✅ HAS DATA\n")
                log.append("   Latest workout: ${allHistory.firstOrNull()?.planName ?: "N/A"}\n\n")

                // Monthly stats
                val monthlyStats = historyDao.getMonthlyStats().first()
                val currentMonth = monthlyStats.firstOrNull()
                log.append("3. Current Month Stats:\n")
                if (currentMonth != null) {
                    log.append("   Month: ${currentMonth.yearMonth}\n")
                    log.append("   Workouts: ${currentMonth.count}\n")
                    log.append("   Rounds: ${currentMonth.totalRounds}\n")
                    log.append("   Duration: ${currentMonth.totalSeconds}s\n")
                    log.append("   Status: ✅ HAS MONTHLY DATA\n\n")
                } else {
                    log.append("   Status: ❌ NO MONTHLY DATA\n\n")
                }

                // Yearly stats
                val yearlyStats = historyDao.getYearlyStats().first()
                val currentYear = yearlyStats.firstOrNull()
                log.append("4. Year-to-Date Stats:\n")
                if (currentYear != null) {
                    log.append("   Year: ${currentYear.year}\n")
                    log.append("   Workouts: ${currentYear.count}\n")
                    log.append("   Rounds: ${currentYear.totalRounds}\n")
                    log.append("   Active days: ${currentYear.activeDays}\n")
                    log.append("   Status: ✅ HAS YEARLY DATA\n\n")
                } else {
                    log.append("   Status: ❌ NO YEARLY DATA\n\n")
                }

                // Streak calculation
                val streak = calculateStreak(allHistory)
                log.append("5. Current Streak:\n")
                log.append("   Days: $streak\n")
                log.append("   Status: ${if (streak > 0) "✅" else "⚠️"}\n\n")
            }

            // Check network/API readiness
            log.append("6. API Configuration:\n")
            log.append("   Endpoint: https://fpkkxfinek.execute-api.us-east-1.amazonaws.com/prod/send-email\n")
            log.append("   Method: POST\n")
            log.append("   Content-Type: application/json\n\n")

            // Overall readiness
            log.append("7. Overall Status:\n")
            val canSend = !userEmail.isNullOrBlank() && allHistory.isNotEmpty()
            log.append("   Can Send Email: ${if (canSend) "✅ YES" else "❌ NO"}\n")

            if (!canSend) {
                log.append("\n❌ CANNOT SEND EMAIL:\n")
                if (userEmail.isNullOrBlank()) {
                    log.append("   - Set your email in Settings\n")
                }
                if (allHistory.isEmpty()) {
                    log.append("   - Complete at least one workout\n")
                }
            } else {
                log.append("   ✅ Ready to send!\n")
            }

        } catch (e: Exception) {
            log.append("\n❌ ERROR:\n")
            log.append("   ${e.message}\n")
            log.append("   ${e.stackTraceToString()}\n")
            Log.e(TAG, "Debug failed", e)
        }

        val result = log.toString()
        Log.d(TAG, result)
        return result
    }

    private fun calculateStreak(history: List<com.stopwatch.app.data.model.WorkoutHistory>): Int {
        if (history.isEmpty()) return 0

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
            if (date == expectedDate || date == expectedDate.minusDays(1)) {
                streak++
                expectedDate = date.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }

    suspend fun generateSampleEmailHtml(context: Context): String {
        return try {
            val historyDao = AppDatabase.getInstance(context).workoutHistoryDao()
            val allHistory = historyDao.getAllHistory().first()
            val monthlyStats = historyDao.getMonthlyStats().first()
            val yearlyStats = historyDao.getYearlyStats().first()
            val mostUsedWorkout = historyDao.getMostUsedWorkout(LocalDate.now().year.toString()).first()

            val currentMonthData = monthlyStats.firstOrNull()
            val currentYearData = yearlyStats.firstOrNull()
            val currentStreak = calculateStreak(allHistory)

            if (currentMonthData == null) {
                return "<h1>No workout data available</h1><p>Complete at least one workout to see the email preview.</p>"
            }

            val monthlyDuration = formatDuration(currentMonthData.totalSeconds)
            val ytdDuration = formatDuration(currentYearData?.totalSeconds ?: 0)
            val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"))

            EmailTemplate.generateWorkoutSummaryEmail(
                userName = "Test User",
                currentMonth = currentMonth,
                monthlyWorkouts = currentMonthData.count,
                monthlyRounds = currentMonthData.totalRounds,
                monthlyDuration = monthlyDuration,
                monthlyStreak = currentStreak,
                ytdWorkouts = currentYearData?.count ?: 0,
                ytdRounds = currentYearData?.totalRounds ?: 0,
                ytdDuration = ytdDuration,
                ytdActiveDays = currentYearData?.activeDays ?: 0,
                mostUsedWorkout = mostUsedWorkout?.planName
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate sample HTML", e)
            "<h1>Error</h1><p>${e.message}</p>"
        }
    }

    private fun formatDuration(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "${totalSeconds}s"
        }
    }
}
