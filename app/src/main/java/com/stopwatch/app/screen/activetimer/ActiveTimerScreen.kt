package com.stopwatch.app.screen.activetimer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stopwatch.app.R
import com.stopwatch.app.data.UserPreferencesRepository
import com.stopwatch.app.data.model.WorkoutMode
import com.stopwatch.app.ui.components.ExerciseImage
import com.stopwatch.app.ui.theme.Gradients
import kotlinx.coroutines.flow.first

// Hardcoded phase colors (independent of theme)
private val CountdownColor = Color(0xFFFFA726)  // Amber
private val PrepColor = Color(0xFF42A5F5)        // Blue - for preparation phase
private val WorkColor = Color(0xFFEF5350)        // Red
private val RestColor = Color(0xFF66BB6A)         // Green
private val FinishedColor = Color(0xFFFFD54F)     // Gold

@Composable
fun ActiveTimerScreen(
    planId: Long,
    onFinished: (durationSeconds: Int) -> Unit,
    onAbandon: () -> Unit,
    viewModel: ActiveTimerViewModel = viewModel()
) {
    val context = LocalContext.current
    val view = LocalView.current
    var showAbandonDialog by remember { mutableStateOf(false) }

    // Keep screen on during workout if enabled in settings
    LaunchedEffect(Unit) {
        val preferencesRepository = UserPreferencesRepository(context)
        val keepScreenOn = preferencesRepository.keepScreenOn.first()

        if (keepScreenOn) {
            view.keepScreenOn = true
        }
    }

    // Release screen on when leaving
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            view.keepScreenOn = false
        }
    }

    LaunchedEffect(planId) {
        viewModel.load(planId)
    }

    LaunchedEffect(viewModel.isFinished) {
        if (viewModel.isFinished) {
            onFinished(viewModel.getTotalDurationSeconds())
        }
    }

    BackHandler {
        showAbandonDialog = true
    }

    if (showAbandonDialog) {
        AlertDialog(
            onDismissRequest = { showAbandonDialog = false },
            title = { Text(stringResource(R.string.abandon_workout)) },
            text = { Text(stringResource(R.string.abandon_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.abandon()
                    onAbandon()
                }) {
                    Text(stringResource(R.string.abandon))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAbandonDialog = false }) {
                    Text(stringResource(R.string.continue_workout))
                }
            }
        )
    }

    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val phaseLabel = when (viewModel.phase) {
        TimerPhase.COUNTDOWN -> stringResource(R.string.get_ready)
        TimerPhase.PREP -> "Preparation Time"
        TimerPhase.WORK -> stringResource(R.string.work)
        TimerPhase.REST -> stringResource(R.string.rest)
        TimerPhase.FINISHED -> stringResource(R.string.done)
    }
    val phaseColor = when (viewModel.phase) {
        TimerPhase.COUNTDOWN -> CountdownColor
        TimerPhase.PREP -> PrepColor
        TimerPhase.WORK -> WorkColor
        TimerPhase.REST -> RestColor
        TimerPhase.FINISHED -> FinishedColor
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFF0)) // Ivory white background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top 50%: Exercise Image or Simple Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // SIMPLE MODE: Clean timer display without images
                if (viewModel.workoutMode == WorkoutMode.SIMPLE) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = viewModel.planName,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.87f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (viewModel.phase != TimerPhase.COUNTDOWN && viewModel.phase != TimerPhase.FINISHED) {
                            Text(
                                text = when (viewModel.phase) {
                                    TimerPhase.PREP -> "GET READY!"
                                    TimerPhase.WORK -> "WORK"
                                    TimerPhase.REST -> "REST"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = phaseColor
                            )

                            if (viewModel.phase == TimerPhase.WORK || viewModel.phase == TimerPhase.REST) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Round ${viewModel.currentRound}/${viewModel.totalRounds}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.Black.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "Exercise ${viewModel.currentExercise}/${viewModel.totalExercises}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.Black.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                } else {
                    // LIBRARY/CUSTOM MODE: Show exercise images
                    when (viewModel.phase) {
                        TimerPhase.PREP -> {
                        // PREP PHASE: Show first exercise or "Get Ready!"
                        if (viewModel.currentExerciseImagePath != null) {
                            // Show first exercise image with "GET READY!" overlay
                            ExerciseImage(
                                imagePath = viewModel.currentExerciseImagePath,
                                contentDescription = viewModel.currentExerciseName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                cornerRadius = 0.dp
                            )

                            // Gradient overlay for better contrast
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Gradients.TopOverlay)
                            )

                            // "GET READY!" text positioned 30% from top (200dp) - fully transparent
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 200.dp, start = 24.dp, end = 24.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "GET READY!",
                                        style = TextStyle(
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            letterSpacing = 0.1.sp,
                                            shadow = Shadow(
                                                color = Color.Black,
                                                offset = Offset(3f, 3f),
                                                blurRadius = 12f
                                            )
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "First: ${viewModel.currentExerciseName}",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            color = Color.White,
                                            shadow = Shadow(
                                                color = Color.Black,
                                                offset = Offset(2f, 2f),
                                                blurRadius = 8f
                                            )
                                        )
                                    )
                                }
                            }
                        } else {
                            // No exercises - show simple "GET READY!" text
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "GET READY!",
                                    style = MaterialTheme.typography.displayLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PrepColor
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = viewModel.planName,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.Black.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    TimerPhase.WORK -> {
                        // WORK PHASE: Show current exercise
                        ExerciseImage(
                            imagePath = viewModel.currentExerciseImagePath,
                            contentDescription = viewModel.currentExerciseName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            cornerRadius = 0.dp
                        )

                        // Gradient overlay for text readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Gradients.BottomOverlay)
                        )

                        // Exercise info with proper bottom margin
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Column {
                                Text(
                                    text = viewModel.currentExerciseName,
                                    style = TextStyle(
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        shadow = Shadow(
                                            color = Color(0x80000000),
                                            offset = Offset(2f, 2f),
                                            blurRadius = 8f
                                        )
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Exercise ${viewModel.currentExercise}/${viewModel.totalExercises}",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = Color.White.copy(alpha = 0.95f),
                                        shadow = Shadow(
                                            color = Color(0x80000000),
                                            offset = Offset(1f, 1f),
                                            blurRadius = 4f
                                        )
                                    )
                                )
                            }
                        }
                    }

                    TimerPhase.REST -> {
                        // REST PHASE: Show NEXT exercise preview
                        ExerciseImage(
                            imagePath = viewModel.nextExerciseImagePath,
                            contentDescription = viewModel.nextExerciseName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            cornerRadius = 0.dp
                        )

                        // Gradient overlay for text readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Gradients.BottomOverlay)
                        )

                        // "NEXT UP" info with proper bottom margin
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Column {
                                Text(
                                    text = "NEXT UP:",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = Color.White.copy(alpha = 0.8f),
                                        letterSpacing = 0.1.sp,
                                        shadow = Shadow(
                                            color = Color(0x80000000),
                                            offset = Offset(1f, 1f),
                                            blurRadius = 4f
                                        )
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = viewModel.nextExerciseName,
                                    style = TextStyle(
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        shadow = Shadow(
                                            color = Color(0x80000000),
                                            offset = Offset(2f, 2f),
                                            blurRadius = 8f
                                        )
                                    )
                                )
                            }
                        }
                    }

                        else -> {
                            // Show plan name during countdown and finished phases
                            Text(
                                text = viewModel.planName,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black.copy(alpha = 0.87f)
                            )
                        }
                    }
                }
            }

            // Bottom 50%: Timer and Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // Circular timer ring with content inside
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(200.dp)
                ) {
                    val progress = if (viewModel.totalSecondsInPhase > 0) {
                        viewModel.secondsRemaining.toFloat() / viewModel.totalSecondsInPhase.toFloat()
                    } else {
                        0f
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 12.dp.toPx()
                        val arcSize = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                        // Background track
                        drawArc(
                            color = phaseColor.copy(alpha = 0.15f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Foreground arc
                        drawArc(
                            color = phaseColor,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Content inside the circle
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = phaseLabel,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = phaseColor
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Timer text
                        val minutes = viewModel.secondsRemaining / 60
                        val seconds = viewModel.secondsRemaining % 60
                        val timeText = if (viewModel.phase == TimerPhase.COUNTDOWN) {
                            "${viewModel.secondsRemaining}"
                        } else {
                            "%02d:%02d".format(minutes, seconds)
                        }
                        Text(
                            text = timeText,
                            fontSize = if (viewModel.phase == TimerPhase.COUNTDOWN) 56.sp else 42.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = FontFamily.Monospace,
                            color = phaseColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Round info below the ring
                if (viewModel.phase != TimerPhase.COUNTDOWN) {
                    Text(
                        text = stringResource(R.string.round_info, viewModel.currentRound, viewModel.totalRounds),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Controls (hidden during initial countdown and finished)
                if (viewModel.phase != TimerPhase.COUNTDOWN && viewModel.phase != TimerPhase.FINISHED) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pause / Resume
                        Button(
                            onClick = { viewModel.togglePause() },
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = if (viewModel.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = if (viewModel.isPaused) stringResource(R.string.resume) else stringResource(R.string.pause),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        // Abandon
                        OutlinedButton(
                            onClick = { showAbandonDialog = true },
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = stringResource(R.string.abandon),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
