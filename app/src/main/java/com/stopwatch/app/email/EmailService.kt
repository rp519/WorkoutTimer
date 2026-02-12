package com.stopwatch.app.email

import android.content.Context
import android.util.Log
import com.stopwatch.app.data.AppDatabase
import com.stopwatch.app.data.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
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
            Log.d(TAG, "Starting email send process...")

            val userEmail = preferencesRepository.userEmail.first()
            if (userEmail.isNullOrBlank()) {
                Log.e(TAG, "❌ FAILED: No email configured")
                return false
            }
            Log.d(TAG, "✓ Email configured: $userEmail")

            // Get workout data
            Log.d(TAG, "Fetching workout data...")
            val allHistory = historyDao.getAllHistory().first()
            Log.d(TAG, "✓ Total workouts: ${allHistory.size}")

            val monthlyStats = historyDao.getMonthlyStats().first()
            Log.d(TAG, "✓ Monthly stats: ${monthlyStats.size} months")

            val yearlyStats = historyDao.getYearlyStats().first()
            Log.d(TAG, "✓ Yearly stats: ${yearlyStats.size} years")

            val mostUsedWorkout = historyDao.getMostUsedWorkout(LocalDate.now().year.toString()).first()

            // Get current month data
            val currentMonthData = monthlyStats.firstOrNull()
            val currentYearData = yearlyStats.firstOrNull()

            if (currentMonthData == null) {
                Log.e(TAG, "❌ FAILED: No workout data available - complete a workout first!")
                return false
            }
            Log.d(TAG, "✓ Current month: ${currentMonthData.yearMonth}, workouts: ${currentMonthData.count}")

            // Calculate current streak
            val currentStreak = calculateCurrentStreak(allHistory)
            Log.d(TAG, "✓ Current streak: $currentStreak days")

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
            Log.d(TAG, "✓ Subject: $subject")
            Log.d(TAG, "✓ HTML generated: ${emailHtml.length} characters")

            // Send email via API with structured workout data
            Log.d(TAG, "Calling API endpoint...")
            val success = sendEmailViaAPI(
                userEmail = userEmail,
                subject = subject,
                htmlBody = emailHtml,
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

            if (success) {
                Log.i(TAG, "✅ SUCCESS: Email sent to $userEmail")
            } else {
                Log.e(TAG, "❌ FAILED: API call failed")
            }

            success
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send email summary", e)
            false
        }
    }

    private suspend fun sendEmailViaAPI(
        userEmail: String,
        subject: String,
        htmlBody: String,
        userName: String,
        currentMonth: String,
        monthlyWorkouts: Int,
        monthlyRounds: Int,
        monthlyDuration: String,
        monthlyStreak: Int,
        ytdWorkouts: Int,
        ytdRounds: Int,
        ytdDuration: String,
        ytdActiveDays: Int,
        mostUsedWorkout: String?
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "API Endpoint: $API_ENDPOINT")
                Log.d(TAG, "Recipient: $userEmail")

                val url = URL(API_ENDPOINT)
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 30000 // 30 seconds
                connection.readTimeout = 30000

                // Match the API's expected format with structured workout data
                val jsonBody = JSONObject().apply {
                    put("userEmail", userEmail)
                    put("subject", subject)
                    put("html", htmlBody)
                    // Include structured workout data for the Lambda function
                    put("workoutData", JSONObject().apply {
                        put("userName", userName)
                        put("currentMonth", currentMonth)
                        put("monthlyWorkouts", monthlyWorkouts)
                        put("monthlyRounds", monthlyRounds)
                        put("monthlyDuration", monthlyDuration)
                        put("monthlyStreak", monthlyStreak)
                        put("ytdWorkouts", ytdWorkouts)
                        put("ytdRounds", ytdRounds)
                        put("ytdDuration", ytdDuration)
                        put("ytdActiveDays", ytdActiveDays)
                        put("mostUsedWorkout", mostUsedWorkout ?: "None")
                    })
                }

                Log.d(TAG, "Request body: ${jsonBody.toString().take(200)}...")

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(jsonBody.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                Log.d(TAG, "API Response Code: $responseCode")

                // Read response body
                val responseBody = try {
                    if (responseCode in 200..299) {
                        connection.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error body"
                    }
                } catch (e: Exception) {
                    "Failed to read response: ${e.message}"
                }

                Log.d(TAG, "API Response Body: $responseBody")

                if (responseCode in 200..299) {
                    Log.i(TAG, "✅ API Success: $responseCode")
                    true
                } else {
                    Log.e(TAG, "❌ API Error: $responseCode - $responseBody")
                    false
                }
            } catch (e: java.net.UnknownHostException) {
                Log.e(TAG, "❌ Network Error: Cannot reach API (check internet connection)", e)
                false
            } catch (e: java.net.SocketTimeoutException) {
                Log.e(TAG, "❌ Timeout Error: API took too long to respond", e)
                false
            } catch (e: Exception) {
                Log.e(TAG, "❌ Unexpected Error: ${e.javaClass.simpleName}: ${e.message}", e)
                false
            }
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
