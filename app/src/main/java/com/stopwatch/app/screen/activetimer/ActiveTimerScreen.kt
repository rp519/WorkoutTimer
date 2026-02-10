package com.stopwatch.app.screen.activetimer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stopwatch.app.R

// Hardcoded phase colors (independent of theme)
private val CountdownColor = Color(0xFFFFA726)  // Amber
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
    var showAbandonDialog by remember { mutableStateOf(false) }

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
        TimerPhase.WORK -> stringResource(R.string.work)
        TimerPhase.REST -> stringResource(R.string.rest)
        TimerPhase.FINISHED -> stringResource(R.string.done)
    }
    val phaseColor = when (viewModel.phase) {
        TimerPhase.COUNTDOWN -> CountdownColor
        TimerPhase.WORK -> WorkColor
        TimerPhase.REST -> RestColor
        TimerPhase.FINISHED -> FinishedColor
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Plan name
            Text(
                text = viewModel.planName,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Circular timer ring with content inside
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(250.dp)
            ) {
                val progress = if (viewModel.totalSecondsInPhase > 0) {
                    viewModel.secondsRemaining.toFloat() / viewModel.totalSecondsInPhase.toFloat()
                } else {
                    0f
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
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
                        fontSize = 20.sp,
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
                        fontSize = if (viewModel.phase == TimerPhase.COUNTDOWN) 64.sp else 48.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.Monospace,
                        color = phaseColor
                    )

                    // Exercise info inside the ring
                    if (viewModel.phase == TimerPhase.WORK || viewModel.phase == TimerPhase.REST) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Exercise ${viewModel.currentExercise}/${viewModel.totalExercises}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Round info below the ring
            if (viewModel.phase != TimerPhase.COUNTDOWN) {
                Text(
                    text = stringResource(R.string.round_info, viewModel.currentRound, viewModel.totalRounds),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

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
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = if (viewModel.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (viewModel.isPaused) stringResource(R.string.resume) else stringResource(R.string.pause),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    // Abandon
                    OutlinedButton(
                        onClick = { showAbandonDialog = true },
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = stringResource(R.string.abandon),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
