package com.stopwatch.app.screen.quicktimer

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stopwatch.app.ui.theme.Gradients
import com.stopwatch.app.ui.theme.AccentAmber
import com.stopwatch.app.ui.theme.BackgroundLightBlue
import com.stopwatch.app.ui.theme.PrimaryGradientEnd
import com.stopwatch.app.ui.theme.PrimaryGradientStart

private val TimerColor = PrimaryGradientEnd  // Blue from theme
private val FinishedColor = Color(0xFFFFD54F)  // Gold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTimerScreen(
    onBack: () -> Unit,
    viewModel: QuickTimerViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quick Timer", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.background(Gradients.Primary)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundLightBlue),  // Light blue background
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (!viewModel.isRunning && !viewModel.isFinished) {
                    // TIME INPUT MODE
                    Text(
                        text = "Set Timer",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.87f)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Time input fields
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = viewModel.inputMinutes.toString(),
                            onValueChange = { value ->
                                value.toIntOrNull()?.let { viewModel.updateInputMinutes(it) }
                            },
                            label = { Text("Minutes") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(120.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = ":",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.Black.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        OutlinedTextField(
                            value = viewModel.inputSeconds.toString(),
                            onValueChange = { value ->
                                value.toIntOrNull()?.let { viewModel.updateInputSeconds(it) }
                            },
                            label = { Text("Seconds") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(120.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = { viewModel.start() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = viewModel.inputMinutes > 0 || viewModel.inputSeconds > 0
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Timer", fontSize = 18.sp)
                    }
                } else {
                    // TIMER RUNNING/FINISHED MODE
                    val minutes = viewModel.totalSecondsRemaining / 60
                    val seconds = viewModel.totalSecondsRemaining % 60
                    val progress = viewModel.getProgress()
                    val ringColor = if (viewModel.isFinished) FinishedColor else TimerColor

                    // Circular timer ring
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(280.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 16.dp.toPx()
                            val arcSize = size.minDimension - strokeWidth
                            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                            // Background track
                            drawArc(
                                color = ringColor.copy(alpha = 0.15f),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcSize, arcSize),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            // Foreground arc
                            drawArc(
                                color = ringColor,
                                startAngle = -90f,
                                sweepAngle = 360f * progress,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcSize, arcSize),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        // Time display
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (viewModel.isFinished) {
                                Text(
                                    text = "DONE!",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FinishedColor
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Text(
                                text = "%02d:%02d".format(minutes, seconds),
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Light,
                                fontFamily = FontFamily.Monospace,
                                color = ringColor
                            )

                            if (viewModel.isPaused && !viewModel.isFinished) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "PAUSED",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Control buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!viewModel.isFinished) {
                            // Pause/Resume button
                            Button(
                                onClick = {
                                    if (viewModel.isPaused) viewModel.resume()
                                    else viewModel.pause()
                                },
                                modifier = Modifier.size(72.dp),
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = if (viewModel.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = if (viewModel.isPaused) "Resume" else "Pause",
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(24.dp))
                        }

                        // Reset button
                        OutlinedButton(
                            onClick = { viewModel.reset() },
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Reset",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
