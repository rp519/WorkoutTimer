package com.stopwatch.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.stopwatch.app.data.ExerciseLibraryInitializer
import com.stopwatch.app.data.UserPreferencesRepository
import com.stopwatch.app.navigation.AppNavGraph
import com.stopwatch.app.notification.DailyReminderWorker
import com.stopwatch.app.notification.WorkoutNotificationManager
import com.stopwatch.app.ui.theme.WorkoutTimerTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scheduleRemindersIfEnabled()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize notification channels
        WorkoutNotificationManager(this)

        // Initialize exercise library from assets
        lifecycleScope.launch {
            ExerciseLibraryInitializer(applicationContext).initializeIfNeeded()
        }

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                scheduleRemindersIfEnabled()
            }
        } else {
            scheduleRemindersIfEnabled()
        }

        setContent {
            WorkoutTimerTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }

    private fun scheduleRemindersIfEnabled() {
        lifecycleScope.launch {
            val preferencesRepository = UserPreferencesRepository(this@MainActivity)
            val remindersEnabled = preferencesRepository.remindersEnabled.first()
            if (remindersEnabled) {
                val hour = preferencesRepository.reminderHour.first()
                val minute = preferencesRepository.reminderMinute.first()
                DailyReminderWorker.schedule(this@MainActivity, hour, minute)
            }
        }
    }
}
