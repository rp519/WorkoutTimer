package com.stopwatch.app.screen.planedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.stopwatch.app.data.model.Exercise

/**
 * Dialog for adding custom exercises to a CUSTOM mode workout.
 * Supports two modes:
 * 1. Library Exercise - Select from library and specify custom duration
 * 2. Custom Exercise - Create fully custom exercise with name and duration
 */
@Composable
fun AddCustomExerciseDialog(
    availableExercises: List<Exercise>,
    onAddFromLibrary: (exercise: Exercise, durationSeconds: Int) -> Unit,
    onAddCustom: (name: String, durationSeconds: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Library, 1 = Custom
    var exerciseName by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("30") }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    var showExerciseSelector by remember { mutableStateOf(false) }

    val tabs = listOf("From Library", "Custom")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Exercise") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Tab selector
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> {
                        // Library Exercise Tab
                        Text(
                            text = "Select an exercise from the library and set a custom duration",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Exercise selector button
                        OutlinedTextField(
                            value = selectedExercise?.name ?: "Tap to select exercise",
                            onValueChange = {},
                            label = { Text("Exercise") },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            trailingIcon = {
                                TextButton(onClick = { showExerciseSelector = true }) {
                                    Text("Select")
                                }
                            }
                        )

                        OutlinedTextField(
                            value = durationText,
                            onValueChange = { durationText = it },
                            label = { Text("Duration (seconds)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    1 -> {
                        // Custom Exercise Tab
                        Text(
                            text = "Create a completely custom exercise",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = exerciseName,
                            onValueChange = { exerciseName = it },
                            label = { Text("Exercise Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = durationText,
                            onValueChange = { durationText = it },
                            label = { Text("Duration (seconds)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val duration = durationText.toIntOrNull() ?: 30
                    when (selectedTab) {
                        0 -> {
                            // Add from library
                            selectedExercise?.let { exercise ->
                                onAddFromLibrary(exercise, duration)
                                onDismiss()
                            }
                        }
                        1 -> {
                            // Add custom
                            if (exerciseName.isNotBlank()) {
                                onAddCustom(exerciseName.trim(), duration)
                                onDismiss()
                            }
                        }
                    }
                },
                enabled = when (selectedTab) {
                    0 -> selectedExercise != null && durationText.toIntOrNull() != null
                    1 -> exerciseName.isNotBlank() && durationText.toIntOrNull() != null
                    else -> false
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    // Exercise selection bottom sheet
    if (showExerciseSelector) {
        ExerciseSelectionSheet(
            exercises = availableExercises,
            categories = availableExercises.map { it.category }.distinct(),
            onExerciseSelected = { exercise ->
                selectedExercise = exercise
                showExerciseSelector = false
            },
            onDismiss = { showExerciseSelector = false }
        )
    }
}
