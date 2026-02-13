package com.stopwatch.app.screen.planedit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stopwatch.app.R
import com.stopwatch.app.data.model.WorkoutMode
import com.stopwatch.app.ui.components.ExerciseChip
import com.stopwatch.app.ui.theme.Gradients
import com.stopwatch.app.ui.theme.BackgroundLightBlue
import androidx.compose.material3.TopAppBarDefaults

@Composable
private fun ModeSelectionCard(
    title: String,
    description: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }
                )
            }
        }
    }
}

@Composable
private fun NumericTextField(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(value.toString()))
    }
    var hasFocus by remember { mutableStateOf(false) }

    // Sync external value changes only when not focused
    LaunchedEffect(value) {
        val currentNumber = textFieldValue.text.toIntOrNull()
        if (currentNumber != value && !hasFocus) {
            textFieldValue = TextFieldValue(value.toString())
        }
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            textFieldValue = newValue
            newValue.text.toIntOrNull()?.let(onValueChange)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.onFocusChanged { focusState ->
            if (focusState.isFocused && !hasFocus) {
                // Select all on first focus
                textFieldValue = textFieldValue.copy(
                    selection = TextRange(0, textFieldValue.text.length)
                )
                hasFocus = true
            } else if (!focusState.isFocused) {
                hasFocus = false
            }
        }
    )
}

@Composable
private fun CustomExerciseCard(
    customExercise: com.stopwatch.app.data.model.CustomExercise,
    onRemove: () -> Unit,
    onDurationChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = customExercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (customExercise.isFromLibrary) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Library",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${customExercise.durationSeconds} seconds",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Remove button
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Close,
                        contentDescription = "Remove exercise"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanEditScreen(
    planId: Long,
    onBack: () -> Unit,
    viewModel: PlanEditViewModel = viewModel()
) {
    // Load all exercises from ViewModel
    val allExercises by viewModel.allExercises.collectAsState(initial = emptyList())
    var showExerciseSheet by remember { mutableStateOf(false) }
    var showCustomExerciseDialog by remember { mutableStateOf(false) }

    LaunchedEffect(planId) {
        viewModel.loadPlan(planId)
    }

    LaunchedEffect(viewModel.isSaved) {
        if (viewModel.isSaved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (viewModel.isEditing) stringResource(R.string.edit_workout)
                        else stringResource(R.string.create_workout),
                        color =   Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.background(Gradients.Primary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLightBlue)
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = viewModel.planName,
                onValueChange = { viewModel.updatePlanName(it) },
                label = { Text(stringResource(R.string.workout_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Workout Type", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // Mode Selection Cards
            ModeSelectionCard(
                title = "Simple Timer",
                description = "Just rounds & exercises - no library",
                icon = Icons.Default.Timer,
                selected = viewModel.workoutMode == WorkoutMode.SIMPLE,
                onClick = { viewModel.updateWorkoutMode(WorkoutMode.SIMPLE) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ModeSelectionCard(
                title = "Exercise Library",
                description = "Select exercises with images",
                icon = Icons.Default.FitnessCenter,
                selected = viewModel.workoutMode == WorkoutMode.LIBRARY,
                onClick = { viewModel.updateWorkoutMode(WorkoutMode.LIBRARY) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ModeSelectionCard(
                title = "Custom Mix",
                description = "Combine library + custom exercises",
                icon = Icons.Default.Build,
                selected = viewModel.workoutMode == WorkoutMode.CUSTOM,
                onClick = { viewModel.updateWorkoutMode(WorkoutMode.CUSTOM) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Conditional UI based on workout mode
            when (viewModel.workoutMode) {
                WorkoutMode.SIMPLE -> {
                    // SIMPLE MODE: Show exercise count input
                    Text("Exercises", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    NumericTextField(
                        value = viewModel.exerciseCount,
                        onValueChange = viewModel::updateExerciseCount,
                        label = "Number of Exercises",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                WorkoutMode.LIBRARY -> {
                    // LIBRARY MODE: Show exercise selection
                    Text(stringResource(R.string.exercises), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Selected exercises chips
                    if (viewModel.selectedExercises.isEmpty()) {
                        Text(
                            text = "No exercises selected. Tap 'Add Exercise' to get started.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(viewModel.selectedExercises, key = { it.id }) { exercise ->
                                ExerciseChip(
                                    exercise = exercise,
                                    onRemove = { viewModel.removeExercise(exercise) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showExerciseSheet = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Add Exercise")
                    }
                }

                WorkoutMode.CUSTOM -> {
                    // CUSTOM MODE: Show custom exercises with individual durations
                    Text("Custom Exercises", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (viewModel.customExercises.isEmpty()) {
                        Text(
                            text = "No exercises added. Mix library exercises with custom durations or create your own.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            viewModel.customExercises.forEachIndexed { index, customExercise ->
                                CustomExerciseCard(
                                    customExercise = customExercise,
                                    onRemove = { viewModel.removeCustomExercise(customExercise) },
                                    onDurationChange = { newDuration ->
                                        viewModel.updateCustomExerciseDuration(index, newDuration)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showCustomExerciseDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Add Exercise")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.structure), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            NumericTextField(
                value = viewModel.rounds,
                onValueChange = viewModel::updateRounds,
                label = stringResource(R.string.rounds),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.timings), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NumericTextField(
                    value = viewModel.workSeconds,
                    onValueChange = viewModel::updateWorkSeconds,
                    label = stringResource(R.string.work_seconds),
                    modifier = Modifier.weight(1f)
                )
                NumericTextField(
                    value = viewModel.restSeconds,
                    onValueChange = viewModel::updateRestSeconds,
                    label = stringResource(R.string.rest_seconds),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            NumericTextField(
                value = viewModel.prepTimeSeconds,
                onValueChange = viewModel::updatePrepTimeSeconds,
                label = "Prep Time (sec)",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Summary: rounds × exercises × work + rounds × (exercises-1) × rest + (rounds-1) × rest
            val exerciseCount = viewModel.selectedExercises.size
            val totalWork = viewModel.rounds * exerciseCount * viewModel.workSeconds
            val totalRestBetweenExercises = viewModel.rounds * (exerciseCount - 1).coerceAtLeast(0) * viewModel.restSeconds
            val totalRestBetweenRounds = (viewModel.rounds - 1).coerceAtLeast(0) * viewModel.restSeconds
            val totalSeconds = totalWork + totalRestBetweenExercises + totalRestBetweenRounds
            val mins = totalSeconds / 60
            val secs = totalSeconds % 60
            Text(
                text = stringResource(R.string.estimated_duration, mins, secs),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth(),
                enabled = when (viewModel.workoutMode) {
                    WorkoutMode.SIMPLE -> viewModel.planName.isNotBlank()
                    WorkoutMode.LIBRARY -> viewModel.planName.isNotBlank() && viewModel.selectedExercises.isNotEmpty()
                    WorkoutMode.CUSTOM -> viewModel.planName.isNotBlank() && viewModel.customExercises.isNotEmpty()
                }
            ) {
                Text(stringResource(R.string.save_workout))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Exercise selection bottom sheet (for LIBRARY mode)
        if (showExerciseSheet) {
            ExerciseSelectionSheet(
                exercises = allExercises,
                categories = viewModel.categories,
                onExerciseSelected = { exercise ->
                    viewModel.addExercise(exercise)
                },
                onDismiss = { showExerciseSheet = false }
            )
        }

        // Custom exercise dialog (for CUSTOM mode)
        if (showCustomExerciseDialog) {
            AddCustomExerciseDialog(
                availableExercises = allExercises,
                onAddFromLibrary = { exercise, durationSeconds ->
                    viewModel.addCustomExerciseFromLibrary(exercise, durationSeconds)
                },
                onAddCustom = { name, durationSeconds ->
                    viewModel.addCustomExercise(name, durationSeconds)
                },
                onDismiss = { showCustomExerciseDialog = false }
            )
        }
    }
}
