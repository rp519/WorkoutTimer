package com.stopwatch.app.email

import android.content.Context
import android.util.Log
import com.stopwatch.app.data.AppDatabase
import com.stopwatch.app.data.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class EmailService(private val context: Context) {

    private val historyDao = AppDatabase.getInstance(context).workoutHistoryDao()
    private val preferencesRepository = UserPreferencesRepository(context)

    companion object {
        private const val API_ENDPOINT = "https://fpkkxfinek.execute-api.us-east-1.amazonaws.com/prod/send-email"
        private const val TAG = "EmailService"
    }

    suspend fun sendWorkoutSummary(): Boolean {
        return try {
            val userEmail = preferencesRepository.userEmail.first()
            if (userEmail.isNullOrBlank()) {
                Log.w(TAG, "No email configured")
                return false
            }

            // Get workout data
            val allHistory = historyDao.getAllHistory().first()
            val monthlyStats = historyDao.getMonthlyStats().first()
            val yearlyStats = historyDao.getYearlyStats().first()
            val mostUsedWorkout = historyDao.getMostUsedWorkout(LocalDate.now().year.toString()).first()

            // Get current month data
            val currentMonthData = monthlyStats.firstOrNull()
            val currentYearData = yearlyStats.firstOrNull()

            if (currentMonthData == null) {
                Log.w(TAG, "No workout data available")
                return false
            }

            // Calculate current streak
            val currentStreak = calculateCurrentStreak(allHistory)

            // Format duration
            val monthlyDuration = formatDuration(currentMonthData.totalSeconds)
            val ytdDuration = formatDuration(currentYearData?.totalSeconds ?: 0)

            // Get current month name
            val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"))

            // Extract user name from email (optional)
            val userName = userEmail.substringBefore("@").split(".", "_", "-")
                .joinToString(" ") { it.capitalize() }

            // Generate email HTML
            val emailHtml = EmailTemplate.generateWorkoutSummaryEmail(
                userName = userName,
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

            // Generate subject line
            val subject = EmailTemplate.generateSubjectLine(
                monthlyWorkouts = currentMonthData.count,
                monthlyStreak = currentStreak
            )

            // Send email via API
            sendEmailViaAPI(userEmail, subject, emailHtml)

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send email summary", e)
            false
        }
    }

    private fun sendEmailViaAPI(toEmail: String, subject: String, htmlBody: String): Boolean {
        return try {
            val url = URL(API_ENDPOINT)
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val jsonBody = JSONObject().apply {
                put("to", toEmail)
                put("subject", subject)
                put("html", htmlBody)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            Log.d(TAG, "Email API response: $responseCode")

            responseCode in 200..299
        } catch (e: Exception) {
            Log.e(TAG, "Failed to call email API", e)
            false
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
