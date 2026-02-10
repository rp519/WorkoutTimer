package com.stopwatch.app.screen.planedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stopwatch.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanEditScreen(
    planId: Long,
    onBack: () -> Unit,
    viewModel: PlanEditViewModel = viewModel()
) {
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
                        if (viewModel.isEditing) stringResource(R.string.edit_workout)
                        else stringResource(R.string.create_workout)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
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
            Text(stringResource(R.string.structure), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.rounds.toString(),
                    onValueChange = { viewModel.updateRounds(it.toIntOrNull() ?: 1) },
                    label = { Text(stringResource(R.string.rounds)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = viewModel.exerciseCount.toString(),
                    onValueChange = { viewModel.updateExerciseCount(it.toIntOrNull() ?: 1) },
                    label = { Text(stringResource(R.string.exercises)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.timings), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.workSeconds.toString(),
                    onValueChange = { viewModel.updateWorkSeconds(it.toIntOrNull() ?: 1) },
                    label = { Text(stringResource(R.string.work_seconds)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = viewModel.restSeconds.toString(),
                    onValueChange = { viewModel.updateRestSeconds(it.toIntOrNull() ?: 0) },
                    label = { Text(stringResource(R.string.rest_seconds)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Summary: rounds × exercises × work + rounds × (exercises-1) × rest + (rounds-1) × rest
            val totalWork = viewModel.rounds * viewModel.exerciseCount * viewModel.workSeconds
            val totalRestBetweenExercises = viewModel.rounds * (viewModel.exerciseCount - 1).coerceAtLeast(0) * viewModel.restSeconds
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
                enabled = viewModel.planName.isNotBlank()
            ) {
                Text(stringResource(R.string.save_workout))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
