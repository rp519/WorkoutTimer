package com.stopwatch.app.email

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.stopwatch.app.data.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class EmailSummaryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val preferencesRepository = UserPreferencesRepository(applicationContext)
            val emailService = EmailService(applicationContext)

            // Check if email summaries are enabled
            val emailEnabled = preferencesRepository.emailSummaryEnabled.first()
            if (!emailEnabled) {
                Log.d(TAG, "Email summaries disabled")
                return Result.success()
            }

            // Check if user has email configured
            val userEmail = preferencesRepository.userEmail.first()
            if (userEmail.isNullOrBlank()) {
                Log.d(TAG, "No email configured")
                return Result.success()
            }

            // Check last email sent time and frequency to enforce once-per-week limit
            val lastSent = preferencesRepository.lastEmailSent.first()
            val frequency = preferencesRepository.emailFrequency.first()
            val currentTime = System.currentTimeMillis()

            val minDaysBetweenEmails = when (frequency) {
                "weekly" -> 7
                "biweekly" -> 14
                "monthly" -> 30
                else -> 7
            }

            val daysSinceLastEmail = (currentTime - lastSent) / (1000 * 60 * 60 * 24)

            // Enforce minimum 7 days between emails (once per week limit)
            if (daysSinceLastEmail < 7) {
                Log.d(TAG, "Email sent too recently (${daysSinceLastEmail} days ago)")
                return Result.success()
            }

            // Check if it's time to send based on frequency
            if (daysSinceLastEmail < minDaysBetweenEmails) {
                Log.d(TAG, "Not time to send yet based on frequency ($frequency)")
                return Result.success()
            }

            // Send email summary
            val success = emailService.sendWorkoutSummary()

            if (success) {
                // Update last sent timestamp
                preferencesRepository.setLastEmailSent(currentTime)
                Log.d(TAG, "Email summary sent successfully")
            } else {
                Log.w(TAG, "Failed to send email summary")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Email worker failed", e)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "EmailSummaryWorker"
        private const val WORK_NAME = "email_summary_worker"

        fun schedule(context: Context) {
            // Check every week, but actual sending is controlled by user preferences
            val workRequest = PeriodicWorkRequestBuilder<EmailSummaryWorker>(
                7, TimeUnit.DAYS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )

            Log.d(TAG, "Email summary worker scheduled")
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Email summary worker cancelled")
        }
    }
}
